package com.mx.cbtis.gestor.modelo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Las entidades viajan como JSON entre el frontend y la API. Se comprueba que cada campo se lee
 * (JSON → objeto, con los setters) y se escribe (objeto → JSON, con los getters) con el nombre que usa el frontend.
 */
class ModeloJsonTest {

	final ObjectMapper json = new ObjectMapper();

	@Test
	@DisplayName("JU-34 Usuario se convierte desde y hacia el JSON del frontend")
	void usuario() throws Exception {
		Usuario u = json.readValue("{\"id_usuario\":196,\"nombre\":\"Perla\",\"correo_electronico\":\"perla22522@cbtis75.edu.mx\","
				+ "\"contrasena\":\"12345\",\"rol\":\"Estudiante\",\"imagen_usuario\":\"#805611\"}", Usuario.class);
		assertThat(u.getId_usuario()).isEqualTo(196);
		assertThat(u.getNombre()).isEqualTo("Perla");
		assertThat(u.getCorreo_electronico()).isEqualTo("perla22522@cbtis75.edu.mx");
		assertThat(u.getContrasena()).isEqualTo("12345");
		assertThat(u.getRol()).isEqualTo("Estudiante");
		assertThat(u.getImagen_usuario()).isEqualTo("#805611");

		JsonNode salida = json.valueToTree(new Usuario(1, "Ana", "a@x.mx", "1", "Docente", "#000000"));
		assertThat(salida.get("imagen_usuario").asText()).isEqualTo("#000000");
		assertThat(salida.get("rol").asText()).isEqualTo("Docente");
	}

	@Test
	@DisplayName("JU-35 Tareas se convierte desde y hacia el JSON del frontend")
	void tareas() throws Exception {
		Tareas t = json.readValue("{\"id_tarea\":199,\"nombre\":\"Base de datos\",\"descripcion\":\"Estructura de tablas\","
				+ "\"fecha_vencimiento\":\"2024-06-13\",\"lista\":\"terminado\",\"estado\":\"danger\",\"id_proyecto\":197}", Tareas.class);
		assertThat(t.getId_tarea()).isEqualTo(199);
		assertThat(t.getNombre()).isEqualTo("Base de datos");
		assertThat(t.getDescripcion()).isEqualTo("Estructura de tablas");
		assertThat(t.getFecha_vencimiento()).isEqualTo("2024-06-13");
		assertThat(t.getLista()).isEqualTo("terminado");
		assertThat(t.getEstado()).isEqualTo("danger");
		assertThat(t.getId_proyecto()).isEqualTo(197);

		JsonNode salida = json.valueToTree(new Tareas(3, "Nueva", "d", "2026-01-01", "pendiente", "primary", 197));
		assertThat(salida.get("lista").asText()).isEqualTo("pendiente");
		assertThat(salida.get("estado").asText()).isEqualTo("primary");
	}

	@Test
	@DisplayName("JU-36 Proyectos, Comentarios y Asignaciones se convierten desde y hacia JSON")
	void otrasEntidades() throws Exception {
		Proyectos p = json.readValue("{\"id_proyecto\":197,\"nombre\":\"My API Rest\",\"descripcion\":\"desc\",\"fecha_inicio\":\"2024-05-31\","
				+ "\"fecha_finalizacion\":\"2024-06-29\",\"estado_proyecto\":\"En proceso\",\"fondo\":\"/Images/a.gif\"}", Proyectos.class);
		assertThat(p.getFondo()).isEqualTo("/Images/a.gif");
		assertThat(p.getEstado_proyecto()).isEqualTo("En proceso");
		assertThat(p.getFecha_finalizacion()).isEqualTo("2024-06-29");

		Comentarios c = json.readValue("{\"id_comentario\":201,\"id_usuario\":\"Perla\",\"id_tarea\":199,"
				+ "\"texto_comentario\":\"hola\",\"fecha_hora\":\"2024-06-26 13:48:00\"}", Comentarios.class);
		assertThat(c.getTexto_comentario()).isEqualTo("hola");
		assertThat(c.getFecha_hora()).isEqualTo("2024-06-26 13:48:00");

		Asignaciones a = json.readValue("{\"id_asignacion\":20,\"id_usuario\":131,\"id_tarea\":199,\"fecha_asignacion\":\"2024-06-20\"}",
				Asignaciones.class);
		assertThat(a.getId_asignacion()).isEqualTo(20);
		assertThat(a.getFecha_asignacion()).isEqualTo("2024-06-20");
	}
}
