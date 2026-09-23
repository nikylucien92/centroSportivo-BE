package it.security;

import it.enumerated.RuoloEnum;
import it.model.Utente;
import it.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

	private final UtenteRepository utenteRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;


	/**
	 * REGISTRAZIONE NUOVO UTENTE
	 */
	public AuthenticationResponse register(
			RegisterRequest registerRequest
	) {

		/*
		 * Controlliamo che l'email non sia già presente.
		 */
		if (utenteRepository
				.findByEmail(registerRequest.getEmail())
				.isPresent()) {

			throw new IllegalArgumentException(
					"Email già registrata"
			);
		}


		/*
		 * Creazione del nuovo utente.
		 */
		Utente utente = new Utente();

		utente.setNome(
				registerRequest.getNome()
		);

		utente.setCognome(
				registerRequest.getCognome()
		);

		utente.setTelefono(
				registerRequest.getTelefono()
		);

		utente.setEmail(
				registerRequest.getEmail()
		);

		utente.setPassword(
				passwordEncoder.encode(
						registerRequest.getPassword()
				)
		);

		if (registerRequest.getDataRegistrazione() != null
				&& !registerRequest
				.getDataRegistrazione()
				.isBlank()) {

			utente.setDataRegistrazione(
					LocalDateTime.parse(
							registerRequest
									.getDataRegistrazione()
					)
			);

		} else {

			utente.setDataRegistrazione(
					LocalDateTime.now()
			);
		}

		utente.setRuolo(
				RuoloEnum.USER
		);


		/*
		 * Salvataggio nel database.
		 */
		Utente savedUser =
				utenteRepository.save(utente);


		/*
		 * Generazione JWT.
		 */
		String jwtToken =
				jwtService.generateToken(savedUser);


		/*
		 * Restituzione del token.
		 */
		return new AuthenticationResponse(
				jwtToken , utente.getNome() ,
				utente.getId()
		);
	}


	/**
	 * LOGIN
	 */
	public AuthenticationResponse authenticate(
			LoginRequest request
	) {

		authenticationManager.authenticate(

				new UsernamePasswordAuthenticationToken(
						request.getEmail(),
						request.getPassword()
				)
		);

		Utente utente =
				utenteRepository
						.findByEmail(request.getEmail())
						.orElseThrow(() ->
								new IllegalArgumentException(
										"Utente non trovato"
								)
						);


		/*
		 * Generazione JWT.
		 */
		String jwtToken =
				jwtService.generateToken(utente);


		/*
		 * Restituzione del JWT.
		 */
		return new AuthenticationResponse(
				jwtToken , utente.getNome() ,utente.getId()

		);
	}
}

