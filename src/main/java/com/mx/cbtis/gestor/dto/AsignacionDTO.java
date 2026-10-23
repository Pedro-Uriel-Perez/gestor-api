package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Asignaciones;

/** Datos de asignación que acepta la API (en lugar de recibir directamente la entidad JPA). */
public record AsignacionDTO(
		@JsonProperty("id_asignacion") Integer idAsignacion,
		@JsonProperty("id_usuario") Integer idUsuario,
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("fecha_asignacion") String fechaAsignacion) {

	public Asignaciones aEntidad() {
		return new Asignaciones(idAsignacion, idUsuario, idTarea, fechaAsignacion);
	}
}
