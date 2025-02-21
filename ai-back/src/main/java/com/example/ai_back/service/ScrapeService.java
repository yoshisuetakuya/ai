package com.example.ai_back.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ScrapeService {

	@Value("${firecrawl.api.key}")
	private String apiKey;

	private final RestTemplate restTemplate;

	@Async
	public CompletableFuture<String> scrapeWebsiteAsync(String url) {
		String apiUrl = "https://api.firecrawl.dev/v1/scrape";

		HttpHeaders headers = new HttpHeaders();
		headers.set("Authorization", "Bearer " + apiKey);
		headers.set("Content-Type", "application/json");

		String requestBody = "{\"url\": \"" + url + "\", \"formats\": [\"markdown\"]}";
		HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
			return CompletableFuture.completedFuture(response.getBody());
		} catch (HttpClientErrorException e) {
			e.printStackTrace();
			return CompletableFuture.completedFuture("Error: " + e.getMessage());
		}
	}
}
