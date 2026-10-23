package com.mx.cbtis.gestor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Credenciales del inicio de sesión: solo correo y contraseña. */
public record LoginDTO(
		@JsonProperty("correo_electronico") String correoElectronico,
		@JsonProperty("contrasena") String contrasena) {
}
