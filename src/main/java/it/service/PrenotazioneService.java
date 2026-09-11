package it.service;

import it.dto.DisponibilitaCampoDto;
import it.dto.PrenotazioneDto;
import it.security.PrenotazioneRequest;
import it.dto.UtenteDto;
import it.model.Campo;
import it.model.DisponibilitaCampo;
import it.model.Prenotazione;
import it.model.Utente;
import it.repository.DisponibilitaCampoRepository;
import it.repository.PrenotazioneRepository;
import it.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PrenotazioneService {

	private final PrenotazioneRepository prenotazioneRepository;
	private final DisponibilitaCampoRepository disponibilitaCampoRepository;
	private final UtenteRepository utenteRepository;


	@Transactional
	public PrenotazioneDto creaPrenotazione(
			PrenotazioneRequest request
	) {

		// =====================================================
		// 1. VALIDAZIONE NUMERO GIOCATORI
		// =====================================================

		if (request.getNumeroGiocatori() == null ||
				request.getNumeroGiocatori() <= 0) {

			throw new IllegalArgumentException(
					"Il numero dei giocatori deve essere maggiore di zero"
			);
		}

		// =====================================================
		// 2. RECUPERO DISPONIBILITÀ
		// =====================================================

		DisponibilitaCampo disponibilita =
				disponibilitaCampoRepository
						.findById(request.getDisponibilitaCampoId())
						.orElseThrow(() ->
								new RuntimeException(
										"Disponibilità non trovata"
								)
						);


		// =====================================================
		// 3. CONTROLLO DISPONIBILITÀ
		// =====================================================

		if (!Boolean.TRUE.equals(
				disponibilita.getDisponibilita()
		)) {

			throw new RuntimeException(
					"Il campo non è disponibile per questo orario"
			);
		}

		if (!"DISPONIBILE".equalsIgnoreCase(
				disponibilita.getStatoDisponibilita()
		)) {

			throw new RuntimeException(
					"Il campo non è disponibile per questo orario"
			);
		}


		// =====================================================
		// 4. RECUPERO UTENTE AUTENTICATO
		// =====================================================

		Authentication authentication =
				SecurityContextHolder
						.getContext()
						.getAuthentication();

		String email = authentication.getName();

		Utente utente =
				utenteRepository
						.findByEmail(email)
						.orElseThrow(() ->
								new RuntimeException(
										"Utente autenticato non trovato"
								)
						);


		// =====================================================
		// 5. RECUPERO CAMPO
		// =====================================================
		Campo campo = disponibilita.getCampo();

		if (campo == null) {

			throw new RuntimeException(
					"Il campo associato alla disponibilità non esiste"
			);
		}

		// =====================================================
		// 6. RECUPERO PREZZO DEL CAMPO
		// =====================================================

		BigDecimal prezzoCampo = campo.getPrezzo();

		if (prezzoCampo == null ||
				prezzoCampo.compareTo(BigDecimal.ZERO) < 0) {

			throw new RuntimeException(
					"Il prezzo del campo non è valido"
			);
		}


		// =====================================================
		// 7. CALCOLO COSTO TOTALE
		// =====================================================

		/*
		 * Ogni disponibilità rappresenta un'ora.
		 *
		 * Esempio:
		 *
		 * Padel = 25 €
		 * Giocatori = 4
		 *
		 * costo totale = 25 €
		 */

		BigDecimal costoTotale = prezzoCampo;


		// =====================================================
		// 8. CALCOLO QUOTA PER PERSONA
		// =====================================================

		BigDecimal quotaPersona =
				costoTotale.divide(
						BigDecimal.valueOf(
								request.getNumeroGiocatori()
						),
						2,
						RoundingMode.HALF_UP
				);


		// =====================================================
		// 9. CREAZIONE PRENOTAZIONE
		// =====================================================

		Prenotazione prenotazione =
				new Prenotazione();

		prenotazione.setDataPrenotazione(
				disponibilita.getOraInizio()
		);

		prenotazione.setNumeroGiocatori(
				request.getNumeroGiocatori()
		);

		prenotazione.setCostoTotale(
				costoTotale
		);

		prenotazione.setStatoPrenotazione(
				"CONFERMATA"
		);

		prenotazione.setDisponibilitaCampo(
				disponibilita
		);

		prenotazione.setUtenteCreato(
				utente
		);


		// =====================================================
		// 10. SALVATAGGIO PRENOTAZIONE
		// =====================================================

		Prenotazione prenotazioneSalvata =
				prenotazioneRepository.save(
						prenotazione
				);


		// =====================================================
		// 11. AGGIORNAMENTO DISPONIBILITÀ
		// =====================================================

		disponibilita.setDisponibilita(false);

		disponibilita.setStatoDisponibilita(
				"PRENOTATO"
		);

		disponibilitaCampoRepository.save(
				disponibilita
		);


		// =====================================================
		// 12. CONVERSIONE IN DTO
		// =====================================================

		return convertiInDto(
				prenotazioneSalvata,
				quotaPersona
		);
	}


	// =========================================================
	// CONVERSIONE ENTITY → DTO
	// =========================================================

	private PrenotazioneDto convertiInDto(
			Prenotazione prenotazione,
			BigDecimal quotaPersona
	) {

		DisponibilitaCampo disponibilita =
				prenotazione.getDisponibilitaCampo();


		DisponibilitaCampoDto disponibilitaDto =
				new DisponibilitaCampoDto();

		disponibilitaDto.setId(
				disponibilita.getId()
		);

		disponibilitaDto.setStatoDisponibilita(
				disponibilita.getStatoDisponibilita()
		);

		disponibilitaDto.setData(
				disponibilita.getData()
		);

		disponibilitaDto.setOraInizio(
				disponibilita.getOraInizio()
		);

		disponibilitaDto.setOraFine(
				disponibilita.getOraFine()
		);


		PrenotazioneDto dto =
				new PrenotazioneDto();

		dto.setId(
				prenotazione.getId()
		);

		dto.setDataPrenotazione(
				prenotazione.getDataPrenotazione()
		);

		dto.setNumeroGiocatori(
				prenotazione.getNumeroGiocatori()
		);

		dto.setCostoTotale(
				prenotazione.getCostoTotale()
		);

		dto.setQuotaPersona(
				quotaPersona
		);

		dto.setStatoPrenotazione(
				prenotazione.getStatoPrenotazione()
		);

		dto.setDisponibilitaCampo(
				disponibilitaDto
		);

		return dto;
	}
}