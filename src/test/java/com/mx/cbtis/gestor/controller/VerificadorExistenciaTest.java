package com.mx.cbtis.gestor.controller;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mx.cbtis.gestor.repository.UsuarioRepository;

class VerificadorExistenciaTest {

	@Test
	@DisplayName("JU-37 Con validaciones activas exige que el id exista; desactivadas (gestor.validaciones.activas=false) no consulta la base")
	void activasYDesactivadas() {
		UsuarioRepository repo = mock(UsuarioRepository.class);
		when(repo.existsById(196)).thenReturn(true);

		VerificadorExistencia activo = new VerificadorExistencia();
		ReflectionTestUtils.setField(activo, "activas", true);
		assertThatCode(() -> activo.exigir(repo, 196, "usuario")).doesNotThrowAnyException();
		assertThatThrownBy(() -> activo.exigir(repo, 999, "usuario"))
			.isInstanceOf(VerificadorExistencia.RegistroNoEncontradoException.class)
			.hasMessage("No existe el usuario con id 999");

		UsuarioRepository otroRepo = mock(UsuarioRepository.class);
		VerificadorExistencia desactivado = new VerificadorExistencia();
		ReflectionTestUtils.setField(desactivado, "activas", false);
		assertThatCode(() -> desactivado.exigir(otroRepo, 999, "usuario")).doesNotThrowAnyException();
		verifyNoInteractions(otroRepo);
	}
}
