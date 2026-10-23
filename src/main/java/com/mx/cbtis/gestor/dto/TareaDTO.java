package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Tareas;

/** Datos de tarea que acepta la API (en lugar de recibir directamente la entidad JPA). */
public record TareaDTO(
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("nombre") String nombre,
		@JsonProperty("descripcion") String descripcion,
		@JsonProperty("fecha_vencimiento") String fechaVencimiento,
		@JsonProperty("lista") String lista,
		@JsonProperty("estado") String estado,
		@JsonProperty("id_proyecto") Integer idProyecto) {

	public Tareas aEntidad() {
		return new Tareas(idTarea, nombre, descripcion, fechaVencimiento, lista, estado, idProyecto);
	}
}
