package com.mx.cbtis.gestor.monitoreo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * En la nube la API es pública, así que /actuator/prometheus solo responde si la petición trae
 * "Authorization: Bearer {metricas.token}" (el mismo token que usa Prometheus para leer las métricas).
 * Sin token configurado (desarrollo local) las métricas quedan abiertas.
 */
@Component
public class FiltroMetricas extends OncePerRequestFilter {

	static final String RUTA_METRICAS = "/actuator/prometheus";

	private final byte[] esperado;

	public FiltroMetricas(@Value("${metricas.token:}") String token) {
		this.esperado = token.isBlank() ? null : ("Bearer " + token).getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return esperado == null || !request.getRequestURI().startsWith(RUTA_METRICAS);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String recibido = request.getHeader("Authorization");
		// Comparación en tiempo constante para no dar pistas del token
		if (recibido != null && MessageDigest.isEqual(esperado, recibido.getBytes(StandardCharsets.UTF_8))) {
			chain.doFilter(request, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}
