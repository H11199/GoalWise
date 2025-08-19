package com.goalwise.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class VonageVerifyService {

    private final String apiKey;
    private final String apiSecret;
    private final String brand;
    private final String baseUrl;
    private final RestTemplate rest;

    public VonageVerifyService(
            @Value("${vonage.apiKey}") String apiKey,
            @Value("${vonage.apiSecret}") String apiSecret,
            @Value("${vonage.brand:Goalwise}") String brand,
            @Value("${vonage.baseUrl:https://api.nexmo.com}") String baseUrl
    ) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        this.brand = brand;
        this.baseUrl = baseUrl;
        this.rest = new RestTemplate();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = new HttpHeaders();
        String basic = apiKey + ":" + apiSecret;
        h.set(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(basic.getBytes(StandardCharsets.UTF_8)));
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    public String startVerification(String phoneE164, String channel) {
        String ch = (channel==null) ? "sms" : channel.toLowerCase();
        Map<String, Object> body = Map.of(
                "brand", brand,
                "workflow", List.of(Map.of("channel", ch, "to", phoneE164))
        );
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, authHeaders());
        ResponseEntity<Map> resp = rest.postForEntity(baseUrl + "/v2/verify", req, Map.class);
        if (!resp.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Verify start failed: " + resp.getStatusCode());
        }
        Object id = resp.getBody().get("request_id");
        if (id == null) throw new RuntimeException("No request_id from Vonage");
        return id.toString();
    }

    public boolean checkCode(String requestId, String code) {
        Map<String, Object> body = Map.of("code", code);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, authHeaders());
        try {
            ResponseEntity<Map> resp = rest.postForEntity(baseUrl + "/v2/verify/" + requestId, req, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful()) return false;
            Object status = resp.getBody().get("status");
            // Accept if status equals "completed" or "approved"; fallback true if 2xx
            if (status != null) {
                String s = status.toString().toLowerCase();
                return s.contains("complete") || s.contains("approved") || s.contains("succeeded");
            }
            return true;
        } catch (HttpClientErrorException e) {
            return false;
        }
    }
}
