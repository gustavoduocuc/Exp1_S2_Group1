package com.minimarket.controller;

import com.minimarket.security.model.AuthResponse;
import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.model.RegistroRequest;
import com.minimarket.security.util.JwtUtil;
import com.minimarket.entity.Usuario;
import com.minimarket.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UsuarioService usuarioService;

    // ENDPOINT 1: LOGIN (Devuelve el JWT)
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 1. Spring Security valida las credenciales contra la Base de Datos
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Credenciales incorrectas");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        // 2. Si la contraseña es correcta, cargamos los datos del usuario
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        
        // 3. Generamos el token usando nuestro motor JwtUtil
        final String jwt = jwtUtil.generateToken(userDetails);

        // 4. Devolvemos el token en la respuesta
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    // ENDPOINT 2: REGISTRO DE NUEVOS USUARIOS
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroRequest registroRequest) {
        try {
            // Pasamos los datos validados del DTO a la Entidad
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setUsername(registroRequest.getUsername());
            nuevoUsuario.setPassword(registroRequest.getPassword());
            
           
            
            usuarioService.save(nuevoUsuario); 
            
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Usuario registrado con éxito");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "No se pudo registrar el usuario: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}