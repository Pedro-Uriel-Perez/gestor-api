package com.mx.cbtis.gestor.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mx.cbtis.gestor.dto.LoginDTO;
import com.mx.cbtis.gestor.dto.UsuarioDTO;
import com.mx.cbtis.gestor.modelo.Usuario;
import com.mx.cbtis.gestor.repository.UsuarioRepository;

@RestController
public class UsuarioController {

	private final UsuarioRepository usuariotable;
	private final VerificadorExistencia verificador;

	public UsuarioController(UsuarioRepository usuariotable, VerificadorExistencia verificador) {
		this.usuariotable = usuariotable;
		this.verificador = verificador;
	}

	@PostMapping("/getUsuarioLogin")
	public Usuario getUsuarioLogin(@RequestBody LoginDTO credenciales) {
		return usuariotable.findByCorreoPass(credenciales.correoElectronico(), credenciales.contrasena())
				.orElseGet(() -> {
					// Credenciales incorrectas: el frontend espera un usuario con nombre "000"
					Usuario noEncontrado = new Usuario();
					noEncontrado.setNombre("000");
					return noEncontrado;
				});
	}

	@GetMapping("/buscarUsuario")
	public Usuario buscar(@RequestParam int id) {
		return usuariotable.findById(id).orElseGet(Usuario::new);
	}

	@PostMapping("/addUsuario")
	public int agregar(@RequestBody UsuarioDTO datos) {
		usuariotable.save(datos.aEntidad());
		return 1;
	}

	@PostMapping("/updateUsuario")
	public int actualizar(@RequestBody UsuarioDTO datos) {
		verificador.exigir(usuariotable, datos.idUsuario(), "usuario");
		usuariotable.save(datos.aEntidad());
		return 1;
	}

	@DeleteMapping("/deleteUsuario")
	public int eliminar(@RequestBody UsuarioDTO datos) {
		verificador.exigir(usuariotable, datos.idUsuario(), "usuario");
		usuariotable.deleteById(datos.idUsuario());
		return 1;
	}

	@PostMapping("/buscarUsuarios")
	public List<Usuario> listar() {
		List<Usuario> lista = new ArrayList<>();
		usuariotable.findAll().forEach(lista::add);
		return lista;
	}
}
