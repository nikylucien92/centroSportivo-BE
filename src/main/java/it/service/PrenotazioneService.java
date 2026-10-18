
package it.service;

import it.dto.PrenotazioneDto;
import it.enumerated.StatoDisponibilita;
import it.enumerated.StatoPrenotazione;
import it.event.DisponibilitaEvent;
import it.exception.BusinessException;
import it.mapper.PrenotazioneMapper;
import it.mapper.Converter;
import it.model.Campo;
import it.model.DisponibilitaCampo;
import it.model.Prenotazione;
import it.model.Utente;
import it.repository.DisponibilitaCampoRepository;
import it.repository.PrenotazioneRepository;
import it.repository.UtenteRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Service
public class PrenotazioneService
		extends AbstractService<Prenotazione, PrenotazioneDto> {

	private final PrenotazioneRepository prenotazioneRepository;
	private final DisponibilitaCampoRepository disponibilitaCampoRepository;
	private final UtenteRepository utenteRepository;
	private final PrenotazioneMapper prenotazioneMapper;
	private final EmailService emailService;
	private final ApplicationEventPublisher eventPublisher;

	public PrenotazioneService(
			JpaRepository<Prenotazione, Integer> repository,
			Converter<Prenotazione, PrenotazioneDto> converter,
			PrenotazioneMapper prenotazioneMapper,
			PrenotazioneRepository prenotazioneRepository,
			UtenteRepository utenteRepository,
			DisponibilitaCampoRepository disponibilitaCampoRepository,
			EmailService emailService,
			ApplicationEventPublisher eventPublisher
	) {
		super(repository, converter);
		this.prenotazioneMapper = prenotazioneMapper;
		this.prenotazioneRepository = prenotazioneRepository;
		this.utenteRepository = utenteRepository;
		this.disponibilitaCampoRepository = disponibilitaCampoRepository;
		this.emailService = emailService;
		this.eventPublisher = eventPublisher;
	}

	// 1. CREA UNA PRENOTAZIONE

	@Transactional
	public PrenotazioneDto effetuaPrenotazione(
			PrenotazioneDto prenotazioneDto,
			Integer utenteId
	) {

		Utente utente = utenteRepository.findById(utenteId)
				.orElseThrow(
						() -> new BusinessException("Utente non trovato")
				);

		if (prenotazioneDto.getDisponibilitaCampo() == null
				|| prenotazioneDto.getDisponibilitaCampo().getId() == null) {
			throw new BusinessException(
					"Devi selezionare uno slot disponibile"
			);
		}

		Integer idDisponibilita =
				prenotazioneDto.getDisponibilitaCampo().getId();

		// Blocca lo slot per evitare prenotazioni concorrenti.
		DisponibilitaCampo disponibilita =
				disponibilitaCampoRepository
						.findByIdForUpdate(idDisponibilita)
						.orElseThrow(
								() -> new BusinessException(
										"Disponibilità non trovata"
								)
						);

		// Lo slot deve essere disponibile.
		if (disponibilita.getStatoDisponibilita()
				!= StatoDisponibilita.DISPONIBILE) {
			throw new BusinessException(
					"Lo slot selezionato non è più disponibile"
			);
		}

		// Verifica che l'orario non sia passato.
		if (disponibilita.getOraInizio() == null
				|| !disponibilita.getOraInizio()
				.isAfter(LocalDateTime.now())) {
			throw new BusinessException(
					"Non puoi prenotare uno slot passato"
			);
		}

		Campo campo = disponibilita.getCampo();

		if (campo == null) {
			throw new BusinessException(
					"Nessun campo associato alla disponibilità"
			);
		}

		if (campo.getPrezzo() == null) {
			throw new BusinessException(
					"Il prezzo del campo non è configurato"
			);
		}

		// Crea la prenotazione.
		Prenotazione prenotazione =
				prenotazioneMapper.toEntity(prenotazioneDto);

		prenotazione.setUtenteCreato(utente);
		prenotazione.setDisponibilitaCampo(disponibilita);
		prenotazione.setDataPrenotazione(LocalDateTime.now());
		prenotazione.setStatoPrenotazione(
				StatoPrenotazione.CONFERMATA
		);

		// Il costo viene calcolato esclusivamente dal backend.
		prenotazione.setCostoTotale(campo.getPrezzo());

		// Aggiorna l'unico attributo che gestisce lo stato dello slot.
		disponibilita.setStatoDisponibilita(
				StatoDisponibilita.PRENOTATO
		);

		disponibilitaCampoRepository.save(disponibilita);

		Prenotazione prenotazioneSalvata =
				prenotazioneRepository.save(prenotazione);

		eventPublisher.publishEvent(
				new DisponibilitaEvent(
						campo.getId(),
						disponibilita.getData().toLocalDate()
				)
		);

		// Invia l'email di conferma.
		String nomeCampo = campo.getNome() != null
				? campo.getNome()
				: "il campo prenotato";

		String corpoHtml = costruisciEmailConferma(
				utente,
				nomeCampo,
				prenotazioneSalvata
		);

		emailService.sendEmail(
				utente.getEmail(),
				"Conferma prenotazione - Centro Sportivo",
				corpoHtml
		);

		return prenotazioneMapper.toDTO(prenotazioneSalvata);
	}

	// 2. ANNULLA UNA PRENOTAZIONE

	@Transactional
	public List<PrenotazioneDto> cancellaPrenotazione(
			Integer idUtente,
			Integer idPrenotazione
	) {

		Prenotazione prenotazione =
				prenotazioneRepository.findById(idPrenotazione)
						.orElseThrow(
								() -> new BusinessException(
										"Prenotazione non trovata"
								)
						);

		if (prenotazione.getUtenteCreato() == null
				|| !Objects.equals(
				prenotazione.getUtenteCreato().getId(),
				idUtente
		)) {
			throw new BusinessException(
					"La prenotazione non appartiene all'utente selezionato"
			);
		}

		if (prenotazione.getStatoPrenotazione()
				== StatoPrenotazione.ANNULLATA) {
			throw new BusinessException(
					"La prenotazione è già stata annullata"
			);
		}

		if (prenotazione.getStatoPrenotazione()
				== StatoPrenotazione.COMPLETATA) {
			throw new BusinessException(
					"Non puoi annullare una prenotazione completata"
			);
		}

		// Annulla la prenotazione.
		prenotazione.setStatoPrenotazione(
				StatoPrenotazione.ANNULLATA
		);

		prenotazioneRepository.save(prenotazione);

		// Recupera lo slot collegato.
		DisponibilitaCampo disponibilita =
				prenotazione.getDisponibilitaCampo();

		if (disponibilita != null && disponibilita.getId() != null) {

			// Blocca lo slot durante la transazione.
			disponibilita = disponibilitaCampoRepository
					.findByIdForUpdate(disponibilita.getId())
					.orElseThrow(
							() -> new BusinessException(
									"Disponibilità associata non trovata"
							)
					);

			// Libera lo slot solo se è prenotato e non è ancora iniziato.
			if (disponibilita.getStatoDisponibilita()
					== StatoDisponibilita.PRENOTATO
					&& disponibilita.getOraInizio() != null
					&& disponibilita.getOraInizio()
					.isAfter(LocalDateTime.now())) {

				disponibilita.setStatoDisponibilita(
						StatoDisponibilita.DISPONIBILE
				);

				disponibilitaCampoRepository.save(disponibilita);

				eventPublisher.publishEvent(
						new DisponibilitaEvent(
								disponibilita.getCampo().getId(),
								disponibilita.getData().toLocalDate()
						)
				);
			}
		}

		return prenotazioneRepository
				.findByUtenteCreatoId(idUtente)
				.stream()
				.map(prenotazioneMapper::toDTO)
				.toList();
	}

	// 3. CALCOLA LA SPESA TOTALE DELL'UTENTE

	public Double calcolaSpesaTotale(Integer idUtente) {

		utenteRepository.findById(idUtente)
				.orElseThrow(
						() -> new RuntimeException("Utente non trovato")
				);

		Double totale =
				prenotazioneRepository.getTotaleSpesoDaUtente(idUtente);

		return totale != null ? totale : 0.0;
	}

	// 4. LISTA PRENOTAZIONI CON PAGINAZIONE

	public Page<PrenotazioneDto> getListaPrenotazioniConPaginazione(
			Integer idUtente,
			int page,
			int size
	) {

		if (page < 0 || size <= 0) {
			throw new IllegalArgumentException(
					"Pagina o dimensione non validi"
			);
		}

		utenteRepository.findById(idUtente)
				.orElseThrow(
						() -> new RuntimeException("Utente non trovato")
				);

		Pageable pageable = PageRequest.of(page, size);

		return prenotazioneMapper.toDTOPage(
				prenotazioneRepository.findPrenotazioniByUtente(
						idUtente,
						pageable
				)
		);
	}

	// 5. TROVA PRENOTAZIONI PER DATA

	public List<PrenotazioneDto> trovaPrenotazioniPerData(
			LocalDate data
	) {

		LocalDateTime inizio = data.atStartOfDay();
		LocalDateTime fine = data.atTime(LocalTime.MAX);

		return prenotazioneRepository
				.findByDataPrenotazioneBetween(inizio, fine)
				.stream()
				.map(prenotazioneMapper::toDTO)
				.toList();
	}

	// 6. COSTRUISCE L'EMAIL DI CONFERMA

	private String costruisciEmailConferma(
			Utente utente,
			String nomeCampo,
			Prenotazione prenotazione
	) {

		DateTimeFormatter formatter =
				DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");

		DisponibilitaCampo disponibilita =
				prenotazione.getDisponibilitaCampo();

		String dataFormattata =
				disponibilita != null
						&& disponibilita.getOraInizio() != null
						? disponibilita.getOraInizio().format(formatter)
						: "Data e orario non disponibili";

		return """
                <html>
                  <body style="font-family: Arial, sans-serif; color: #222;">
                    <h2>Prenotazione confermata!</h2>
                    <p>Ciao %s,</p>
                    <p>La tua prenotazione è stata registrata con successo.
                    Ecco il riepilogo:</p>
                    <table style="border-collapse: collapse;">
                      <tr>
                        <td style="padding:4px 8px;"><b>Campo</b></td>
                        <td style="padding:4px 8px;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:4px 8px;"><b>Data</b></td>
                        <td style="padding:4px 8px;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:4px 8px;"><b>Numero giocatori</b></td>
                        <td style="padding:4px 8px;">%s</td>
                      </tr>
                      <tr>
                        <td style="padding:4px 8px;"><b>Costo totale</b></td>
                        <td style="padding:4px 8px;">%s €</td>
                      </tr>
                    </table>
                    <p>Grazie per aver scelto Centro Sportivo!</p>
                  </body>
                </html>
                """.formatted(
				utente.getNome() != null ? utente.getNome() : "",
				nomeCampo,
				dataFormattata,
				prenotazione.getNumeroGiocatori(),
				prenotazione.getCostoTotale()
		);
	}
}
