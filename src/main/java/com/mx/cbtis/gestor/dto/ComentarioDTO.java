package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Comentarios;

/** Datos de comentario que acepta la API (en lugar de recibir directamente la entidad JPA). */
public record ComentarioDTO(
		@JsonProperty("id_comentario") Integer idComentario,
		@JsonProperty("id_usuario") String idUsuario,
		@JsonProperty("id_tarea") Integer idTarea,
		@JsonProperty("texto_comentario") String textoComentario,
		@JsonProperty("fecha_hora") String fechaHora) {

	public Comentarios aEntidad() {
		return new Comentarios(idComentario, idUsuario, idTarea, textoComentario, fechaHora);
	}
}
