package com.example.ai_back.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_back.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping("/search")
	@CrossOrigin(origins = "http://localhost:3000")
	public CompletableFuture<String> search(@RequestParam(name = "query") String query) {
		// 検索キーワードを受け取って検索を実行
		return searchService.search(query);
	}
}
