package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.ComentarioDTO;
import com.mx.cbtis.gestor.modelo.Comentarios;
import com.mx.cbtis.gestor.repository.ComentariosRepository;

@RestController
public class ComentariosController {

	private final ComentariosRepository comentariostable;
	private final VerificadorExistencia verificador;

	public ComentariosController(ComentariosRepository comentariostable, VerificadorExistencia verificador) {
		this.comentariostable = comentariostable;
		this.verificador = verificador;
	}

	@PostMapping("/getComentario")
	public List<Comentarios> getComentario(@RequestBody ComentarioDTO filtro) {
		return comentariostable.findByTarea(filtro.idTarea());
	}

	@GetMapping("/buscarComentario")
	public Comentarios buscar(@RequestParam int id) {
		return comentariostable.findById(id).orElseGet(Comentarios::new);
	}

	@PostMapping("/addComentario")
	public int agregar(@RequestBody ComentarioDTO datos) {
		comentariostable.save(datos.aEntidad());
		return 1;
	}

	@PostMapping("/updateComentario")
	public int actualizar(@RequestBody ComentarioDTO datos) {
		verificador.exigir(comentariostable, datos.idComentario(), "comentario");
		comentariostable.save(datos.aEntidad());
		return 1;
	}

	@DeleteMapping("/deleteComentario")
	public int eliminar(@RequestBody ComentarioDTO datos) {
		verificador.exigir(comentariostable, datos.idComentario(), "comentario");
		comentariostable.deleteById(datos.idComentario());
		return 1;
	}

	@PostMapping("/buscarComentarios")
	public List<Comentarios> listar() {
		List<Comentarios> lista = new ArrayList<>();
		comentariostable.findAll().forEach(lista::add);
		return lista;
	}
}
