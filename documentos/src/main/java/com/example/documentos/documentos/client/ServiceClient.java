package com.example.documentos.documentos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ServiceClient {

    private final RestTemplate http;
    private final String key;

    public ServiceClient(RestTemplate http, @Value("${app.internal-key}") String key) {
        this.http = http;
        this.key = key;
    }

    public <T> T call(String url, HttpMethod method, Object body, String token, Class<T> type) {
        var h = new HttpHeaders();
        h.set("X-Internal-Key", key);
        if (token != null) h.set("Authorization", token);
        try {
            return http.exchange(url, method, new HttpEntity<>(body, h), type).getBody();
        } catch (HttpClientErrorException e) {
            throw new ResponseStatusException(
                e.getStatusCode(),
                "Requisição recusada pelo serviço de origem"
            );
        } catch (RestClientException e) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Serviço necessário indisponível"
            );
        }
    }

    public String user(String token) {
        return call(
            "http://identidade/interno/sessao",
            HttpMethod.GET,
            null,
            token,
            User.class
        ).id();
    }

    public record User(String id, String nome, String email) {}
}
