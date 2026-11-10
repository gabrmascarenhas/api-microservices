package com.example.documentos.identidade.repository;

import com.example.documentos.identidade.model.Usuario;
import java.util.*;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioRepository {

    private final ArquivoRegistros<Usuario> arquivo;

    public UsuarioRepository(
        com.fasterxml.jackson.databind.ObjectMapper mapper,
        @org.springframework.beans.factory.annotation.Value(
            "${app.data-dir:.dados/identidade}"
        ) String pasta
    ) {
        arquivo = new ArquivoRegistros<>(mapper, pasta, "identidade.json", Usuario.class);
        for (var registro : arquivo.ler()) usuarios.put(registro.email(), registro);
    }

    private void gravar(Usuario registro) {
        var copia = new HashMap<>(usuarios);
        copia.put(registro.email(), registro);
        arquivo.salvar(copia.values());
        usuarios.put(registro.email(), registro);
    }

    private final Map<String, Usuario> usuarios = new java.util.concurrent.ConcurrentHashMap<>();

    public synchronized boolean salvar(Usuario u) {
        if (usuarios.containsKey(u.email())) return false;
        gravar(u);
        return true;
    }

    public Optional<Usuario> email(String email) {
        return Optional.ofNullable(usuarios.get(email));
    }

    public synchronized Optional<Usuario> bloquear(String id, boolean bloqueado) {
        var usuario = usuarios
            .values()
            .stream()
            .filter(u -> u.id().equals(id))
            .findFirst();
        if (usuario.isEmpty() || usuario.get().administrador()) return Optional.empty();
        var u = usuario.get();
        var atualizado = new Usuario(
            u.id(),
            u.nome(),
            u.email(),
            u.senhaHash(),
            u.perfil(),
            bloqueado
        );
        gravar(atualizado);
        return Optional.of(atualizado);
    }

    public List<Usuario> listar() {
        return usuarios.values().stream().sorted(Comparator.comparing(Usuario::email)).toList();
    }
}
