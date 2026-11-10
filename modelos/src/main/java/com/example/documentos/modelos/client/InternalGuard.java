package com.example.documentos.modelos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InternalGuard {

    private final String key;

    public InternalGuard(@Value("${app.internal-key}") String key) {
        this.key = key;
    }

    public void check(String supplied) {
        if (
            supplied == null ||
            !java.security.MessageDigest.isEqual(
                key.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                supplied.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            )
        ) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    }
}
