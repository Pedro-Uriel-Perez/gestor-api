package com.mx.cbtis.gestor.monitoreo;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

/** Endpoints de monitoreo que usan Prometheus (métricas) y el blackbox exporter (health) en la nube. */
@SpringBootTest(properties = "metricas.token=token-de-prueba")
@AutoConfigureMockMvc
@AutoConfigureObservability
class MetricasPrometheusTest {

	@Autowired
	MockMvc mvc;

	@Test
	@DisplayName("JU-40 /actuator/health responde UP con la base conectada y /actuator/prometheus entrega métricas solo con token")
	void healthYMetricas() throws Exception {
		mvc.perform(get("/actuator/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("UP"));
		mvc.perform(get("/actuator/prometheus"))
			.andExpect(status().isUnauthorized());
		mvc.perform(get("/actuator/prometheus").header("Authorization", "Bearer token-de-prueba"))
			.andExpect(status().isOk())
			.andExpect(content().string(containsString("jvm_memory_used_bytes")))
			.andExpect(content().string(containsString("application=\"gestor-api\"")));
	}
}
