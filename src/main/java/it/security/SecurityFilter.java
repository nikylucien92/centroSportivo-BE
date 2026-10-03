package it.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityFilter {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;


	@Bean
	public SecurityFilterChain securityFilterChain(
			HttpSecurity http
	) throws Exception {

		http

				.csrf(AbstractHttpConfigurer::disable)

				/*
				 * CORS
				 */
				.cors(cors -> cors
						.configurationSource(
								corsConfigurationSource()
						)
				)

				.sessionManagement(session ->
						session.sessionCreationPolicy(
								SessionCreationPolicy.STATELESS
						)
				)


				/*
				 * AUTORIZZAZIONE
				 */
				.authorizeHttpRequests(auth -> auth

								.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

								.requestMatchers("/auth/**").permitAll()

								.requestMatchers("/admin/**").hasRole("ADMIN")

								.anyRequest().authenticated()

						/*
						 * TUTTI GLI ALTRI ENDPOINT
						 * richiedono autenticazione.
						 */
				)

				.addFilterBefore(
						jwtAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);


		return http.build();
	}


	/**
	 * PASSWORD ENCODER
	 *
	 * Le password vengono salvate nel database
	 * tramite BCrypt.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {

		return new BCryptPasswordEncoder();
	}


	/**
	 * AUTHENTICATION MANAGER
	 *
	 * Viene utilizzato durante il login.
	 */
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration configuration
	) throws Exception {

		return configuration.getAuthenticationManager();
	}


	/**
	 * CONFIGURAZIONE CORS
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {

		CorsConfiguration configuration =
				new CorsConfiguration();

		configuration.setAllowedOrigins(
				List.of(
						"http://localhost:4200",
						"http://127.0.0.1:4200"

				)
		);

		configuration.setAllowedMethods(
				List.of(
						"GET",
						"POST",
						"PUT",
						"DELETE",
						"OPTIONS"
				)
		);

		configuration.setAllowedHeaders(
				List.of("*")
		);

		configuration.setAllowCredentials(true);


		UrlBasedCorsConfigurationSource source =
				new UrlBasedCorsConfigurationSource();

		source.registerCorsConfiguration(
				"/**",
				configuration
		);

		return source;
	}
}
