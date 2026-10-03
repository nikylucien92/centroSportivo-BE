package it.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalException {

	// CREDENZIALI ERRATE (login: email inesistente O password sbagliata)
	// UsernameNotFoundException e BadCredentialsException estendono
	// entrambe AuthenticationException, quindi finiscono qui con lo
	// STESSO messaggio generico: è una scelta di sicurezza voluta,
	// per non rivelare a chi tenta il login quali email sono
	// registrate nel sistema (email enumeration).

	@ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
	public ResponseEntity<Map<String,Object>> handleExceptionAuthentication(){
		return buildResponse(HttpStatus.UNAUTHORIZED, "CREDENZIALI NON VALIDE ! ");

	}

	// ARGOMENTI NON VALIDI (es. "Email già registrata" in fase di
	// registrazione)
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
		return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
	}

	// ECCEZIONI "DI DOMINIO" LANCIATE DAI SERVICE
	// (es. PrenotazioneService: "Utente non trovato",
	// "Disponibilità non trovata", "Prenotazione non trovata")
	// Distinguiamo i messaggi "non trovato" (404) dagli altri (500),
	// così il frontend può reagire in modo diverso ai due casi.
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

		String messaggio = ex.getMessage() != null ? ex.getMessage() : "Errore interno del server";
		HttpStatus status = messaggio.toLowerCase().contains("non trovato")
				? HttpStatus.NOT_FOUND
				: HttpStatus.INTERNAL_SERVER_ERROR;
		return buildResponse(status, messaggio);
	}

	private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String messaggio) {

		Map<String, Object> body = new LinkedHashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("status", status.value());
		body.put("error", status.getReasonPhrase());
		body.put("message", messaggio);

		return ResponseEntity.status(status).body(body);
	}

}
