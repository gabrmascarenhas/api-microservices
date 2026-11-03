package com.example.documentos.documentos.controller;
import com.example.documentos.documentos.service.DocumentoService;import com.example.documentos.documentos.dto.DocumentoDtos.*;import org.springframework.web.bind.annotation.*;import jakarta.validation.Valid;import java.util.List;
@RestController @RequestMapping("/api/documentos") public class DocumentoController {
 private final DocumentoService service;public DocumentoController(DocumentoService s){service=s;}
 @PostMapping @ResponseStatus(org.springframework.http.HttpStatus.CREATED) public Saida criar(@RequestHeader(value="Authorization",required=false) String t,@Valid @RequestBody Entrada e){return service.criar(t,e);}
 @GetMapping public List<Saida> listar(@RequestHeader(value="Authorization",required=false) String t){return service.listar(t);}
 @GetMapping("/{id}") public Saida buscar(@RequestHeader(value="Authorization",required=false) String t,@PathVariable String id){return service.buscar(t,id);}
}
