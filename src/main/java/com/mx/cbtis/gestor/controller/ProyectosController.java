package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.ProyectoDTO;
import com.mx.cbtis.gestor.modelo.Proyectos;
import com.mx.cbtis.gestor.repository.ProtectosRepository;

@RestController
public class ProyectosController {

	private final ProtectosRepository proyectostable;
	private final VerificadorExistencia verificador;

	public ProyectosController(ProtectosRepository proyectostable, VerificadorExistencia verificador) {
		this.proyectostable = proyectostable;
		this.verificador = verificador;
	}

	@GetMapping("/buscarProyecto")
	public Proyectos buscar(@RequestParam int id_proyecto) {
		return proyectostable.findById(id_proyecto).orElseGet(Proyectos::new);
	}

	@PostMapping("/addProyecto")
	public int agregar(@RequestBody ProyectoDTO datos) {
		proyectostable.save(datos.aEntidad());
		return 1;
	}

	@PostMapping("/updateProyecto")
	public int actualizar(@RequestBody ProyectoDTO datos) {
		verificador.exigir(proyectostable, datos.idProyecto(), "proyecto");
		proyectostable.save(datos.aEntidad());
		return 1;
	}

	@DeleteMapping("/deleteProyecto")
	public int eliminar(@RequestBody ProyectoDTO datos) {
		verificador.exigir(proyectostable, datos.idProyecto(), "proyecto");
		proyectostable.deleteById(datos.idProyecto());
		return 1;
	}

	@PostMapping("/buscarProyectos")
	public List<Proyectos> listar() {
		List<Proyectos> lista = new ArrayList<>();
		proyectostable.findAll().forEach(lista::add);
		return lista;
	}
}
