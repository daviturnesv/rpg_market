package com.programacao_web.rpg_market.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "rpg-market");
        return ResponseEntity.ok(status);
    }

    // Removido o mapeamento duplicado da rota raiz
    // O MarketController já gerencia a navegação principal
}
