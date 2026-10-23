package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.AsignacionDTO;
import com.mx.cbtis.gestor.modelo.Asignaciones;
import com.mx.cbtis.gestor.repository.AsignacionesRepository;

@RestController
public class AsignacionesController {

	private final AsignacionesRepository asignacionestable;
	private final VerificadorExistencia verificador;

	public AsignacionesController(AsignacionesRepository asignacionestable, VerificadorExistencia verificador) {
		this.asignacionestable = asignacionestable;
		this.verificador = verificador;
	}

	@GetMapping("/buscarAsignacion")
	public Asignaciones buscar(@RequestParam int id) {
		return asignacionestable.findById(id).orElseGet(Asignaciones::new);
	}

	@PostMapping("/addAsignacion")
	public int agregar(@RequestBody AsignacionDTO datos) {
		asignacionestable.save(datos.aEntidad());
		return 1;
	}

	@PostMapping("/updateAsignacion")
	public int actualizar(@RequestBody AsignacionDTO datos) {
		verificador.exigir(asignacionestable, datos.idAsignacion(), "asignacion");
		asignacionestable.save(datos.aEntidad());
		return 1;
	}

	@DeleteMapping("/deleteAsignacion")
	public int eliminar(@RequestBody AsignacionDTO datos) {
		verificador.exigir(asignacionestable, datos.idAsignacion(), "asignacion");
		asignacionestable.deleteById(datos.idAsignacion());
		return 1;
	}

	@PostMapping("/buscarAsignaciones")
	public List<Asignaciones> listar() {
		List<Asignaciones> lista = new ArrayList<>();
		asignacionestable.findAll().forEach(lista::add);
		return lista;
	}
}
