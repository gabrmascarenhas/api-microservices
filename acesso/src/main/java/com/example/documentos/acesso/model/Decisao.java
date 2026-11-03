package com.example.documentos.acesso.model;
public record Decisao(String usuarioId,String acao,boolean permitido,java.time.Instant instante){}
