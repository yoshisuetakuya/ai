package com.example.ai_back.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import com.example.ai_back.dto.GeminiAPIResponseDto;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SummaryService {

	@Value("${gemini.api.key}")
	private String apiKey;
	@Value("${gemini.api.url}")
	private String apiUrl;

	private final RestTemplate restTemplate;

	@Async
	public CompletableFuture<String> summarizeWithGeminiAsync(String text) {
		String prompt = "次のテキストを要約してください。重要な事実や数値データ、ポイントとなる情報を抜き出し、簡潔で分かりやすい文章にまとめてください。\n\n"
	               + "【要約のポイント】\n"
	               + "・内容の核心となる情報を抜き出し、冗長な部分を省略する。\n"
	               + "・事実やデータは正確に保持しつつ、簡潔な表現にする。\n"
	               + "・ユーザーが短時間で理解できるよう、明瞭で分かりやすい言葉を用いる。\n\n"
	               + "【要約対象のテキスト】\n" + text;
		return requestGeminiApi(prompt);
	}

	@Async
	public CompletableFuture<String> finalSummarizeWithGeminiAsync(String text) {
		String prompt = "以下の3つのテキストを統合し、1つの自然な流れの文章として要約してください。\n\n"
	               + "【要約の指針】\n"
	               + "・3つの内容を違和感なく組み合わせ、一貫性のある文章にする。\n"
	               + "・冗長な部分を省きつつ、重要な情報やポイントはしっかり含める。\n"
	               + "・単なる箇条書きではなく、論理的な流れを持たせたまとまりのある文章にする。\n"
	               + "・読者がすぐに理解できるよう、分かりやすく簡潔な表現を使う。\n\n"
	               + "【要約対象のテキスト】\n" + text;
		return requestGeminiApi(prompt);
	}

	private CompletableFuture<String> requestGeminiApi(String prompt) {
		Map<String, Object> requestBody = Map.of("contents",
				new Object[] { Map.of("parts", new Object[] { Map.of("text", prompt) }) });

		ResponseEntity<GeminiAPIResponseDto> response = restTemplate.postForEntity(apiUrl + "?key=" + apiKey, requestBody,
				GeminiAPIResponseDto.class);

		GeminiAPIResponseDto responseBody = response.getBody();
		if (responseBody != null && !responseBody.getCandidates().isEmpty()) {
			return CompletableFuture
					.completedFuture(responseBody.getCandidates().get(0).getContent().getParts().get(0).getText());
		}
		return CompletableFuture.completedFuture("要約を取得できませんでした。");
	}
}
