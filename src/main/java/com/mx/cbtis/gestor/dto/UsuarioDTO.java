package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mx.cbtis.gestor.modelo.Usuario;

/** Datos de usuario que acepta la API (en lugar de recibir directamente la entidad JPA). */
public record UsuarioDTO(
		@JsonProperty("id_usuario") Integer idUsuario,
		@JsonProperty("nombre") String nombre,
		@JsonProperty("correo_electronico") String correoElectronico,
		@JsonProperty("contrasena") String contrasena,
		@JsonProperty("rol") String rol,
		@JsonProperty("imagen_usuario") String imagenUsuario) {

	public Usuario aEntidad() {
		return new Usuario(idUsuario, nombre, correoElectronico, contrasena, rol, imagenUsuario);
	}
}
