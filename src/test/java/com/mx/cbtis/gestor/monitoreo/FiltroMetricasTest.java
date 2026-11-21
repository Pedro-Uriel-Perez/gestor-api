package com.mx.cbtis.gestor.monitoreo;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class FiltroMetricasTest {

	private MockFilterChain pasar(FiltroMetricas filtro, String ruta, String authorization) throws Exception {
		MockHttpServletRequest peticion = new MockHttpServletRequest("GET", ruta);
		if (authorization != null) {
			peticion.addHeader("Authorization", authorization);
		}
		MockFilterChain cadena = new MockFilterChain();
		filtro.doFilter(peticion, new MockHttpServletResponse(), cadena);
		return cadena;
	}

	@Test
	@DisplayName("JU-38 Con token configurado, /actuator/prometheus exige el token correcto")
	void exigeToken() throws Exception {
		FiltroMetricas filtro = new FiltroMetricas("secreto");

		assertThat(pasar(filtro, "/actuator/prometheus", "Bearer secreto").getRequest()).isNotNull();
		assertThat(pasar(filtro, "/actuator/prometheus", "Bearer otro").getRequest()).isNull();
		assertThat(pasar(filtro, "/actuator/prometheus", null).getRequest()).isNull();

		MockHttpServletResponse respuesta = new MockHttpServletResponse();
		filtro.doFilter(new MockHttpServletRequest("GET", "/actuator/prometheus"), respuesta, new MockFilterChain());
		assertThat(respuesta.getStatus()).isEqualTo(401);
	}

	@Test
	@DisplayName("JU-39 El filtro no afecta otras rutas ni el desarrollo local sin token")
	void otrasRutasYSinToken() throws Exception {
		assertThat(pasar(new FiltroMetricas("secreto"), "/actuator/health", null).getRequest()).isNotNull();
		assertThat(pasar(new FiltroMetricas("secreto"), "/listaProyectos", null).getRequest()).isNotNull();
		assertThat(pasar(new FiltroMetricas(""), "/actuator/prometheus", null).getRequest()).isNotNull();
	}
}
