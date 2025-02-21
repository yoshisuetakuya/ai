package com.example.ai_back.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import lombok.RequiredArgsConstructor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class SuggestService {

	// それぞれapplication.properties で設定
	@Value("${gemini.api.key}")
	private String apiKey;
	@Value("${gemini.api.url}")
	private String apiUrl;

	private final RestTemplate restTemplate;

	public ResponseEntity<Map<String, Object>> suggestService(String inputText) {
		// Gemini APIを呼び出し、要約プロンプトを設定
		String summaryPrompt = "次のテキストに関連するキーワードを3つ挙げてください。\n" + "それぞれ２０文字以内で詳細にキーワードを挙げて下さい。\n"
				+ "出力は必ず以下のフォーマットに従ってください。\n" + "1. ○○\n" + "2. △△\n" + "3. ××\n\n" + "対象のテキスト: " + inputText;
		String requestBody = "{\n" + "  \"contents\": [{\n" + "    \"parts\": [{\"text\": \"" + summaryPrompt + "\"}]\n"
				+ "  }]\n" + "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		String urlWithApiKey = apiUrl + "?key=" + apiKey;

		ResponseEntity<String> response = restTemplate.postForEntity(urlWithApiKey, entity, String.class);

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			JsonNode root = objectMapper.readTree(response.getBody());
			JsonNode textNode = root.path("candidates").get(0).path("content").path("parts").get(0).path("text");

			if (textNode.isMissingNode()) {
				// エラー時のレスポンス
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "関連する情報が見つかりませんでした。"));
			}

			// 成功時のレスポンス
			Map<String, Object> result = Map.of("inputText", inputText, "suggestedKeywords", textNode.asText());
			return ResponseEntity.ok(result);

		} catch (Exception e) {
			e.printStackTrace();
			// エラー時のレスポンス
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "エラーが発生しました。"));
		}
	}

}
