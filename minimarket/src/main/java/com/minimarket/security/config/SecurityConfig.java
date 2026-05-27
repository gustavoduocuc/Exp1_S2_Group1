package com.minimarket.security.config;

import com.minimarket.security.audit.SecurityAuditHandler;
import com.minimarket.security.filter.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityAuditHandler securityAuditHandler;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // EXPOSITOR DEL AUTENTICADOR: Necesario para que AuthController pueda validar credenciales
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Transición a Seguridad Sin Estado (Stateless) y Desactivación de CSRF
            .csrf(csrf -> csrf.disable())
            
            // 2. Desactivar políticas de sesión (100% Sin Estado)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. Registro centralizado de auditoría (Excepciones 401 y 403)
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(securityAuditHandler)
                .accessDeniedHandler(securityAuditHandler)
            )
            
            // 4. Endurecimiento de Cabeceras de Seguridad (Feedback del Profesor)
            .headers(headers -> headers
                .frameOptions(frame -> frame.deny()) // Mitiga Clickjacking
                .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // Defensa adicional
            )
            
            // 5. Matriz de Autorización por Roles (RBAC)
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos (Acceso libre perimetral)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/productos/**", "/api/categorias/**").permitAll()
                
                // Reglas Granulares por Rol
                .requestMatchers("/api/carrito/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers("/api/ventas/**", "/api/detalle-ventas/**").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/inventario/**").hasAnyRole("EMPLEADO", "GERENTE", "ADMIN")
                
                // Privilegios de Modificación (Gerencia y Administración)
                .requestMatchers(HttpMethod.PUT, "/api/inventario/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**", "/api/categorias/**").hasAnyRole("GERENTE", "ADMIN")
                
                // Privilegios Exclusivos de Infraestructura
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                
                // Cierre restrictivo por defecto
                .anyRequest().authenticated()
            );

        // 6. Inyección perimetral de nuestro filtro antes del validador por defecto
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}