package com.example.ai_back.service;

import com.example.ai_back.dto.SearchResultDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final ScrapeService scrapeService;
	private final SummaryService summaryService;

	@Value("${google.api.key}")
	private String googleApiKey;

	@Value("${google.custom.search.engine.id}")
	private String customSearchEngineId;

	@Async
	public CompletableFuture<String> search(String query) {
		String url = "https://www.googleapis.com/customsearch/v1?q={query}&cx={cx}&key={apiKey}&lr=lang_ja&num=3";
		Map<String, String> params = Map.of("query", query, "cx", customSearchEngineId, "apiKey", googleApiKey);

		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(url,
				org.springframework.http.HttpMethod.GET, null, new ParameterizedTypeReference<>() {
				}, params);

		Map<String, Object> responseBody = response.getBody();
		if (responseBody != null) {
			try {
				// ここでJSON の "items" 配列を、SearchResultDTO オブジェクトのリストに変換している
				List<SearchResultDTO> items = objectMapper.convertValue(responseBody.get("items"),
						objectMapper.getTypeFactory().constructCollectionType(List.class, SearchResultDTO.class));

				// 非同期処理でスクレイピングと要約を並列実行
				List<CompletableFuture<Map<String, String>>> futures = items
						.stream().map(
								item -> scrapeService.scrapeWebsiteAsync(item.getLink())
										.thenCompose(scrape -> summaryService.summarizeWithGeminiAsync(scrape)
												.thenApply(summary -> Map.of("title", item.getTitle(), "link",
														item.getLink(), "summary", summary))))
						.collect(Collectors.toList());

				// 各非同期処理の結果を取り出してリストとしてまとめている
				CompletableFuture<List<Map<String, String>>> allResultsFuture = CompletableFuture
						.allOf(futures.toArray(new CompletableFuture[0]))
						.thenApply(v -> futures.stream().map(CompletableFuture::join).collect(Collectors.toList()));

				// 最終的な要約を非同期処理で実行
				return allResultsFuture.thenCompose(result -> {
					String combinedSummaries = result.stream().map(r -> r.get("summary"))
							.collect(Collectors.joining("\n"));

					return summaryService.finalSummarizeWithGeminiAsync(combinedSummaries).thenApply(finalSummary -> {
						try {
							return objectMapper.writerWithDefaultPrettyPrinter()
									.writeValueAsString(Map.of("items", result, "finalSummary", finalSummary));
						} catch (Exception e) {
							e.printStackTrace();
							return "JSON変換エラー";
						}
					});
				});

			} catch (Exception e) {
				e.printStackTrace();
				return CompletableFuture.completedFuture("レスポンスの処理中にエラーが発生しました。");
			}
		} else {
			return CompletableFuture.completedFuture("APIからのレスポンスがありませんでした。");
		}
	}
}
