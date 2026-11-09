package com.example.documentos.documentos.controller;
import com.example.documentos.documentos.service.DocumentoService;import com.example.documentos.documentos.dto.DocumentoDtos.*;import org.springframework.web.bind.annotation.*;import jakarta.validation.Valid;import java.util.List;
@RestController @RequestMapping("/api/documentos") public class DocumentoController {
 private final DocumentoService service;public DocumentoController(DocumentoService s){service=s;}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Saida criar(@RequestHeader(value="Authorization",required=false) String t,@Valid @RequestBody Entrada e){return service.criar(t,e);}
 @GetMapping public List<Saida> listar(@RequestHeader(value="Authorization",required=false) String t){return service.listar(t);}
 @GetMapping("/{id}") public Saida buscar(@RequestHeader(value="Authorization",required=false) String t,@PathVariable String id){return service.buscar(t,id);}

 @PutMapping("/{id}")
 public Saida editar(@RequestHeader(value="Authorization",required=false) String token,
         @PathVariable String id,@Valid @RequestBody Edicao entrada) {
     return service.editar(token,id,entrada);
 }

 @GetMapping("/{id}/exportar")
 public org.springframework.http.ResponseEntity<byte[]> exportar(
         @RequestHeader(value="Authorization",required=false) String token,
         @PathVariable String id,@RequestParam String formato) throws java.io.IOException {
     var arquivo = service.exportar(token,id,formato.toLowerCase(java.util.Locale.ROOT));
     return org.springframework.http.ResponseEntity.ok()
             .header("Content-Disposition","attachment; filename=documento." + arquivo.extensao())
             .contentType(org.springframework.http.MediaType.parseMediaType(arquivo.tipo()))
             .body(arquivo.dados());
 }

 @PostMapping("/{id}/solicitacoes")
 public Object solicitar(@RequestHeader(value="Authorization",required=false) String token,
         @PathVariable String id) {
     return service.solicitacao(token, id, "solicitar");
 }

 @GetMapping("/{id}/solicitacoes")
 public Object listarSolicitacoes(@RequestHeader(value="Authorization",required=false) String token,
         @PathVariable String id) {
     return service.solicitacao(token, id, "solicitacoes");
 }

 @PutMapping("/{id}/solicitacoes/{usuario}")
 public Object decidir(@RequestHeader(value="Authorization",required=false) String token,
         @PathVariable String id, @PathVariable java.util.UUID usuario, @RequestParam boolean aprovar) {
     return service.solicitacao(token, id, "solicitacoes/" + usuario + "?aprovar=" + aprovar);
 }
}
