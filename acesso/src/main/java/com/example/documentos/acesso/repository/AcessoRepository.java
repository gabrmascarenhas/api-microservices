package com.example.documentos.acesso.repository;
import com.example.documentos.acesso.model.Decisao;import org.springframework.stereotype.Repository;import java.util.*;
@Repository public class AcessoRepository {private final Deque<Decisao> historico=new ArrayDeque<>();public synchronized void registrar(Decisao d){if(historico.size()>=1000)historico.removeFirst();historico.addLast(d);}}
