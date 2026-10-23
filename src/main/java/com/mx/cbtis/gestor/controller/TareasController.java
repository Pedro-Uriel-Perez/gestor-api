package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.TareaDTO;
import com.mx.cbtis.gestor.modelo.Tareas;
import com.mx.cbtis.gestor.repository.TareasRepository;

@RestController
public class TareasController {

	private final TareasRepository tareastable;
	private final VerificadorExistencia verificador;

	public TareasController(TareasRepository tareastable, VerificadorExistencia verificador) {
		this.tareastable = tareastable;
		this.verificador = verificador;
	}

	@PostMapping("/getTarea")
	public List<Tareas> getTarea(@RequestBody TareaDTO filtro) {
		return tareastable.findByIdProyecto(filtro.idProyecto());
	}

	@GetMapping("/buscarTarea")
	public Tareas buscar(@RequestParam int id_tarea) {
		return tareastable.findById(id_tarea).orElseGet(Tareas::new);
	}

	@PostMapping("/addTareas")
	public int agregar(@RequestBody TareaDTO datos) {
		tareastable.save(datos.aEntidad());
		return 1;
	}

	@PostMapping("/updateTareas")
	public int actualizar(@RequestBody TareaDTO datos) {
		verificador.exigir(tareastable, datos.idTarea(), "tarea");
		tareastable.save(datos.aEntidad());
		return 1;
	}

	@DeleteMapping("/deleteTareas")
	public int eliminar(@RequestBody TareaDTO datos) {
		verificador.exigir(tareastable, datos.idTarea(), "tarea");
		tareastable.deleteById(datos.idTarea());
		return 1;
	}

	@PostMapping("/buscarTareas")
	public List<Tareas> listar() {
		List<Tareas> lista = new ArrayList<>();
		tareastable.findAll().forEach(lista::add);
		return lista;
	}
}
