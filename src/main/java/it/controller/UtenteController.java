package it.controller;

import it.dto.UtenteDto;
import it.service.UtenteService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController

@RequestMapping("/utente")
@CrossOrigin(origins = "http://localhost:4200")
public class UtenteController extends AbstractController<UtenteDto> {


	private final UtenteService utenteService;

	public UtenteController(UtenteService utenteService) {

		super(utenteService);

		this.utenteService = utenteService;
	}


	@GetMapping("/email")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UtenteDto> getByEmail(
			@RequestParam("email") String email
	) throws Exception {

		return ResponseEntity.ok(
				utenteService.findByEmail(email)
		);
	}


	@PutMapping("/{idUtente}/telefono")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UtenteDto> cambiaTelefono(
			@PathVariable Integer idUtente,
			@RequestParam("telefono") String telefono
	) throws Exception {

		return ResponseEntity.ok(
				utenteService.cambioCell(
						idUtente,
						telefono
				)
		);
	}


	@PutMapping("/{idUtente}/email")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<UtenteDto> cambiaEmail(
			@PathVariable Integer idUtente,
			@RequestParam("email") String email
	) throws Exception {

		return ResponseEntity.ok(
				utenteService.cambioEmail(
						idUtente,
						email
				)
		);
	}


	@PutMapping("/{id}/promuovi")
	@PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
	public ResponseEntity<UtenteDto> promuoviAdAdmin(
			@PathVariable Integer id
	) throws Exception {

		return ResponseEntity.ok(
				utenteService.upgradeToAdmin(id)
		);
	}
}

