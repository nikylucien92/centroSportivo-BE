package it.service;

import it.dto.PrenotazioneDto;
import it.mapper.PrenotazioneMapper;
import it.mapper.Converter;
import it.model.Campo;
import it.model.DisponibilitaCampo;
import it.model.Prenotazione;
import it.model.Utente;
import it.repository.DisponibilitaCampoRepository;
import it.repository.PrenotazioneRepository;
import it.repository.UtenteRepository;
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

@Service
public class PrenotazioneService  extends AbstractService<Prenotazione,PrenotazioneDto> {

	private final PrenotazioneRepository prenotazioneRepository;
	private final DisponibilitaCampoRepository disponibilitaCampoRepository;
	private final UtenteRepository utenteRepository;
	private final PrenotazioneMapper prenotazioneMapper;
	private final EmailService emailService;

	protected PrenotazioneService(
			JpaRepository<Prenotazione, Integer> repository,
			Converter<Prenotazione, PrenotazioneDto> converter,
			PrenotazioneMapper prenotazioneMapper,
			PrenotazioneRepository prenotazioneRepository,
			UtenteRepository utenteRepository,
			DisponibilitaCampoRepository disponibilitaCampoRepository,
			EmailService emailService) {

		super(repository, converter);
		this.prenotazioneMapper = prenotazioneMapper;
		this.prenotazioneRepository = prenotazioneRepository;
		this.utenteRepository = utenteRepository;
		this.disponibilitaCampoRepository = disponibilitaCampoRepository;
		this.emailService = emailService;
	}

	@Transactional
	public PrenotazioneDto effetuaPrenotazione(PrenotazioneDto prenotazioneDto, Integer utenteId) throws Exception {

		// 1. Verifica utente ,restituisce un Optional<Utente>. Se è vuoto (utente non esiste)
		Utente utente = utenteRepository.findById(utenteId)
				.orElseThrow(() -> new Exception("Utente non trovato"));

		// 2. Mappatura e associazione
		Prenotazione prenotazione = prenotazioneMapper.toEntity(prenotazioneDto);
		prenotazione.setUtenteCreato(utente);

		// 3. Gestione data prenotazione
		//Se il frontend non manda una data di prenotazione nel body, la impostiamo automaticamente al momento attuale.
		if (prenotazione.getDataPrenotazione() == null) {
			prenotazione.setDataPrenotazione(LocalDateTime.now());
		}

		// 4. Validazione disponibilità campo (se indicata nel DTO)
		DisponibilitaCampo disponibilita = null;

		if (prenotazioneDto.getDisponibilitaCampo() != null
				&& prenotazioneDto.getDisponibilitaCampo().getId() != null) {

			disponibilita = disponibilitaCampoRepository
					.findById(prenotazioneDto.getDisponibilitaCampo().getId())
					.orElseThrow(() -> new Exception("Disponibilità non trovata"));

			prenotazione.setDisponibilitaCampo(disponibilita);

			disponibilita.setDisponibilita(false);
			disponibilita.setStatoDisponibilita("PRENOTATO");
			disponibilitaCampoRepository.save(disponibilita);
		}

		if (prenotazione.getStatoPrenotazione() == null) {
			prenotazione.setStatoPrenotazione("CONFERMATA");
		}

		// 5. Salvataggio su DB
		Prenotazione saved = prenotazioneRepository.save(prenotazione);

		// 6. Invio email di conferma (asincrona)
		String nomeCampo = "il campo prenotato";
		if (disponibilita != null && disponibilita.getCampo() != null) {
			Campo campo = disponibilita.getCampo();
			nomeCampo = campo.getNome() != null ? campo.getNome() : nomeCampo;
		}

		String corpoHtml = costruisciEmailConferma(utente, nomeCampo, saved);
		emailService.sendEmail(
				utente.getEmail(),
				"Conferma prenotazione - Centro Sportivo",
				corpoHtml
		);

		// 7. Restituzione DTO
		return prenotazioneMapper.toDTO(saved);
	}

	// 2. CANCELLA PRENOTAZIONE
	@Transactional
	public List<PrenotazioneDto> cancellaPrenotazione(Integer idUtente, Integer idPrenotazione) throws Exception {

		Prenotazione prenotazione = prenotazioneRepository.findById(idPrenotazione)
				.orElseThrow(() -> new Exception("Prenotazione non trovata"));

		if (prenotazione.getUtenteCreato() == null
				|| !prenotazione.getUtenteCreato().getId().equals(idUtente)) {
			throw new Exception("La prenotazione indicata non appartiene all'utente selezionato");
		}

		// Libera lo slot di disponibilità associato, se presente
		DisponibilitaCampo disponibilita = prenotazione.getDisponibilitaCampo();
		if (disponibilita != null) {
			disponibilita.setDisponibilita(true);
			disponibilita.setStatoDisponibilita("DISPONIBILE");
			disponibilitaCampoRepository.save(disponibilita);
		}

		prenotazioneRepository.delete(prenotazione);

		return prenotazioneRepository.findByUtenteCreatoId(idUtente).stream()
				.map(prenotazioneMapper::toDTO)
				.toList();
	}

	// 3. CALCOLA SPESA TOTALE
	public Double calcolaSpesaTotale(Integer idUtente) {

		utenteRepository.findById(idUtente)
				.orElseThrow(() -> new RuntimeException("Utente non trovato"));

		Double totale = prenotazioneRepository.getTotaleSpesoDaUtente(idUtente);
		return totale != null ? totale : 0.0;
	}

	// 4. LISTA PRENOTAZIONI PAGINATA
	public Page<PrenotazioneDto> getListaPrenotazioniConPaginazione(Integer idUtente, int page, int size) {

		utenteRepository.findById(idUtente)
				.orElseThrow(() -> new RuntimeException("Utente non trovato"));

		Pageable pageable = PageRequest.of(page, size);

		return prenotazioneMapper.toDTOPage(
				prenotazioneRepository.findPrenotazioniByUtente(idUtente, pageable)
		);
	}


	// 5. PRENOTAZIONI PER DATA
	public List<PrenotazioneDto> trovaPrenotazioniPerData(LocalDate data) {

		LocalDateTime inizio = data.atStartOfDay();
		LocalDateTime fine = data.atTime(LocalTime.MAX);

		return prenotazioneRepository.findByDataPrenotazioneBetween(inizio, fine).stream()
				.map(prenotazioneMapper::toDTO)
				.toList();
	}

	// TEMPLATE EMAIL DI CONFERMA
	private String costruisciEmailConferma(Utente utente, String nomeCampo, Prenotazione prenotazione) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
		String dataFormattata = prenotazione.getDataPrenotazione() != null
				? prenotazione.getDataPrenotazione().format(formatter)
				: "";

		return """
				<html>
				  <body style="font-family: Arial, sans-serif; color: #222;">
				    <h2>Prenotazione confermata!</h2>
				    <p>Ciao %s,</p>
				    <p>la tua prenotazione è stata registrata con successo. Ecco il riepilogo:</p>
				    <table style="border-collapse: collapse;">
				      <tr><td style="padding:4px 8px;"><b>Campo</b></td><td style="padding:4px 8px;">%s</td></tr>
				      <tr><td style="padding:4px 8px;"><b>Data</b></td><td style="padding:4px 8px;">%s</td></tr>
				      <tr><td style="padding:4px 8px;"><b>Numero giocatori</b></td><td style="padding:4px 8px;">%s</td></tr>
				      <tr><td style="padding:4px 8px;"><b>Costo totale</b></td><td style="padding:4px 8px;">%s €</td></tr>
				    </table>
				    <p>Grazie per aver scelto Centro Sportivo!</p>
				  </body>
				</html>
				"""
				.formatted(
						utente.getNome() != null ? utente.getNome() : "",
						nomeCampo,
						dataFormattata,
						prenotazione.getNumeroGiocatori(),
						prenotazione.getCostoTotale()
				);
	}


}