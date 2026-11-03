package com.example.documentos.documentos.model;
public record Documento(String id,String titulo,String conteudo,String proprietarioId,String modeloId,int modeloVersao,java.time.Instant criadoEm){}
