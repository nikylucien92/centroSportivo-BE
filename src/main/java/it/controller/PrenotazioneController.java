package it.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.dto.PrenotazioneDto;
import it.service.PrenotazioneService;
import it.service.ServiceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController

@RequestMapping("/prenotazione")
@CrossOrigin(origins = "http://localhost:4200")
public class PrenotazioneController extends AbstractController<PrenotazioneDto> {

	private final PrenotazioneService prenotazioneService;

	protected PrenotazioneController(ServiceDto<PrenotazioneDto> service, PrenotazioneService prenotazioneService) {
		super(service);
		this.prenotazioneService = prenotazioneService;
	}

	@PostMapping
	@Operation(
			summary = "Effettua una prenotazione",
			description = "Crea una nuova prenotazione associandola all'utente indicato"
	)
	public ResponseEntity<PrenotazioneDto> createPrenotazione(@RequestBody PrenotazioneDto prenotazioneDto,
	                                                          @RequestParam Integer utenteId) throws Exception {
		PrenotazioneDto createdPrenotazione = prenotazioneService.effetuaPrenotazione(prenotazioneDto, utenteId);
		return ResponseEntity.ok(createdPrenotazione);

	}


	@DeleteMapping("/{idUtente}")
	@Operation(
			summary = "Cancella una prenotazione",
			description = "Elimina una prenotazione appartenente all'utente indicato"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Prenotazione cancellata correttamente"),
			@ApiResponse(responseCode = "404", description = "Prenotazione o utente non trovato"),
			@ApiResponse(responseCode = "500", description = "Errore interno del server")
	})
	public ResponseEntity<List<PrenotazioneDto>> cancellaPrenotazione(@PathVariable Integer idUtente, @RequestParam Integer idPrenotazione) throws Exception {
		List<PrenotazioneDto> prenotazioneCancellata = prenotazioneService.cancellaPrenotazione(idUtente, idPrenotazione);
		return ResponseEntity.ok(prenotazioneCancellata);
	}


	@GetMapping("/{utenteId}/spesa-totale")
	@Operation(
			summary = "Calcola spesa totale utente",
			description = "Restituisce il totale speso dall'utente per tutte le prenotazioni effettuate"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Spesa totale calcolata correttamente"),
			@ApiResponse(responseCode = "404", description = "Utente non trovato"),
			@ApiResponse(responseCode = "500", description = "Errore interno del server")
	})
	public ResponseEntity<Double> spesaTotale(@PathVariable(name = "utenteId") Integer id) {
		return ResponseEntity.ok(prenotazioneService.calcolaSpesaTotale(id));
	}

	@GetMapping("/listaSingoloUtente/{id}")
	@Operation(
			summary = "Trova prenotazioni di un utente",
			description = "Restituisce la lista paginata delle prenotazioni effettuate dall'utente"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista prenotazioni recuperata correttamente"),
			@ApiResponse(responseCode = "404", description = "Utente non trovato"),
			@ApiResponse(responseCode = "500", description = "Errore interno del server")
	})
	public ResponseEntity<?> listaPrenotazioniSingoloUtente(
			@PathVariable Integer id,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) throws Exception {
		return ResponseEntity.ok(prenotazioneService.getListaPrenotazioniConPaginazione(id, page, size));
	}

	@GetMapping("/data")
	@Operation(
			summary = "Trova prenotazioni per data",
			description = "Restituisce tutte le prenotazioni effettuate nella data indicata"
	)
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Lista prenotazioni recuperata correttamente"),
			@ApiResponse(responseCode = "400", description = "Formato data non valido"),
			@ApiResponse(responseCode = "500", description = "Errore interno del server")
	})
	public ResponseEntity<List<PrenotazioneDto>> trovaPerData(
			// @DateTimeFormat indica a Spring come convertire la stringa "2026-07-16" in LocalDate
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {

		return ResponseEntity.ok(
				prenotazioneService.trovaPrenotazioniPerData(data)
		);
	}


}
