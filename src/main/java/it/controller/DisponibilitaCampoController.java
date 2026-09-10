package it.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import it.dto.DisponibilitaCampoDto;
import it.service.DisponibilitaCampoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/disponibilitaCampo")
public class DisponibilitaCampoController {

    private final DisponibilitaCampoService disponibilitaCampoService;

    public DisponibilitaCampoController(DisponibilitaCampoService disponibilitaCampoService) {
        this.disponibilitaCampoService = disponibilitaCampoService;
    }

    @GetMapping("/listaDisponibilita")
    public ResponseEntity<List<DisponibilitaCampoDto>>
    getAllDisponibilita() {

        return ResponseEntity.ok(disponibilitaCampoService.getAllDisponibilita());
    }

    @GetMapping("/dataDisponibilita")
    public ResponseEntity<List<DisponibilitaCampoDto>>
    getDisponibilitaByData(
            @RequestParam LocalDate data) throws Exception {

        return ResponseEntity.ok(disponibilitaCampoService.getDisponibilitaByData(data));
    }

	@GetMapping("/campo/{idCampo}")
	public ResponseEntity<List<DisponibilitaCampoDto>>
	getDisponibilitaByCampo(
					@PathVariable Integer idCampo) throws Exception {

		return ResponseEntity.ok(
				disponibilitaCampoService
						.getDisponibilitaByCampo(idCampo)
		);
	}

	@GetMapping("/campo/{idCampo}/data")
	public ResponseEntity<List<DisponibilitaCampoDto>>
	getDisponibilitaByCampoEData(
			@PathVariable Integer idCampo,
			@RequestParam LocalDate data) throws Exception {

		return ResponseEntity.ok(
				disponibilitaCampoService
						.getDisponibilitaByCampoEData(
								idCampo,
								data
						)
		);
	}
}
