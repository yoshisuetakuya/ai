package com.example.ai_back.service;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Async;

import com.example.ai_back.dto.GeminiAPIResponse;
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
		String prompt = "以下のスクレイピングした結果のテキストから、主要な事実や重要なデータを抜き出して要約してください。\n" + text;
		return requestGeminiApi(prompt);
	}

	@Async
	public CompletableFuture<String> finalSummarizeWithGeminiAsync(String text) {
		String prompt = "以下の提供された3つのテキストから一つの内容の文章にまとめて、ユーザーが理解しやすい文章を心掛けて要約してください。\n" + text;
		return requestGeminiApi(prompt);
	}

	private CompletableFuture<String> requestGeminiApi(String prompt) {
		Map<String, Object> requestBody = Map.of("contents",
				new Object[] { Map.of("parts", new Object[] { Map.of("text", prompt) }) });

		ResponseEntity<GeminiAPIResponse> response = restTemplate.postForEntity(apiUrl + "?key=" + apiKey, requestBody,
				GeminiAPIResponse.class);

		GeminiAPIResponse responseBody = response.getBody();
		if (responseBody != null && !responseBody.getCandidates().isEmpty()) {
			return CompletableFuture
					.completedFuture(responseBody.getCandidates().get(0).getContent().getParts().get(0).getText());
		}
		return CompletableFuture.completedFuture("要約を取得できませんでした。");
	}
}
