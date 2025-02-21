package com.example.ai_back.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.ai_back.service.SuggestService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SuggestController {

	private final SuggestService suggestService;

	@GetMapping("/suggest")
	@CrossOrigin(origins = "http://localhost:3000")
	public ResponseEntity<Map<String, Object>> generateContent(@RequestParam(name = "text") String text) {
		return suggestService.suggestService(text);
	}
}
