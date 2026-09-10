package it.controller;

import it.enumerated.RuoloEnum;
import it.model.Campo;
import it.model.Utente;
import it.model.DisponibilitaCampo;
import it.repository.CampoRepository;
import it.repository.DisponibilitaCampoRepository;
import it.repository.UtenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

	private final UtenteRepository utenteRepository;
	private final CampoRepository campoRepository;
	private final DisponibilitaCampoRepository disponibilitaRepository;

	private final PasswordEncoder passwordEncoder;


	@Override
	public void run(String... args) {

		// ==========================================
		// UTENTI
		// ==========================================

		creaAdmin();
		creaUtenti();


		// ==========================================
		// CAMPI
		// ==========================================

		Campo padel = creaCampo(
				"Campo 1",
				"Padel",
				BigDecimal.valueOf(25.00)

		);

		Campo tennis = creaCampo(
				"Campo 2",
				"Tennis",
				BigDecimal.valueOf(20.00)

		);

		Campo calcio5 = creaCampo(
				"Campo 3",
				"Calcio a 5",
				BigDecimal.valueOf(50.00)

		);

		Campo basket = creaCampo(
				"Campo 4",
				"Basket",
				BigDecimal.valueOf(35.00)

		);

		Campo calcio11 = creaCampo(
				"Campo 5",
				"Calcio a 11",
				BigDecimal.valueOf(100.00)

		);


		// ==========================================
		// DISPONIBILITÀ
		// ==========================================

		creaDisponibilita(padel);
		creaDisponibilita(tennis);
		creaDisponibilita(calcio5);
		creaDisponibilita(basket);
		creaDisponibilita(calcio11);


		System.out.println(
				"=============================================="
		);

		System.out.println(
				"DATABASE INIZIALIZZATO CORRETTAMENTE"
		);

		System.out.println(
				"Admin, utenti, campi e disponibilità creati."
		);

		System.out.println(
				"=============================================="
		);
	}


	// =========================================================
	// ADMIN
	// =========================================================

	private void creaAdmin() {

		String email = "pignatiello.nicol@gmail.it";

		if (utenteRepository.findByEmail(email).isPresent()) {
			return;
		}

		Utente admin = new Utente();

		admin.setNome("Nicola");
		admin.setCognome("Pignatone");
		admin.setEmail(email);

		admin.setPassword(
				passwordEncoder.encode("Admin123!")
		);

		admin.setTelefono("3331234567");

		admin.setDataRegistrazione(
				LocalDateTime.now()
		);

		admin.setRuolo(
				RuoloEnum.ADMIN
		);

		utenteRepository.save(admin);
	}


	// =========================================================
	// UTENTI
	// =========================================================

	private void creaUtenti() {

		creaUtente(
				"Mario",
				"Rossi",
				"mario.rossi@gmail.com",
				"3332345111"
		);

		creaUtente(
				"Luca",
				"Bianchi",
				"luca.bianchi@gmail.com",
				"3332231722"
		);

		creaUtente(
				"Francesco",
				"Verdi",
				"francesco.verdi@gmail.com",
				"32789098533"
		);

		creaUtente(
				"Giuseppe",
				"Esposito",
				"giuseppe.esposito@gmail.com",
				"3334562144"
		);

		creaUtente(
				"Andrea",
				"Romano",
				"andrea.romano@gmail.com",
				"3335589155"
		);
	}


	private Utente creaUtente(
			String nome,
			String cognome,
			String email,
			String telefono
	) {

		if (utenteRepository.findByEmail(email).isPresent()) {
			return utenteRepository
					.findByEmail(email)
					.get();
		}

		Utente utente = new Utente();

		utente.setNome(nome);
		utente.setCognome(cognome);
		utente.setEmail(email);

		utente.setPassword(
				passwordEncoder.encode("Pass7777!")
		);

		utente.setTelefono(telefono);

		utente.setRuolo(
				RuoloEnum.USER
		);

		utente.setDataRegistrazione(
				LocalDateTime.now()
		);

		return utenteRepository.save(utente);
	}


	// =========================================================
	// CAMPI
	// =========================================================

	private Campo creaCampo(
			String nome,
			String tipologia,
			BigDecimal prezzo
	) {

		Campo campo = new Campo();

		campo.setNome(nome);
		campo.setTipologia(tipologia);
		campo.setPrezzo(prezzo);
		campo.setCoperto(true);

		return campoRepository.save(campo);
	}


	// =========================================================
	// DISPONIBILITÀ
	// =========================================================

	private void creaDisponibilita(Campo campo) {

		/*
		 * Creiamo le disponibilità per i prossimi 14 giorni.
		 *
		 * Ogni giorno:
		 *
		 * 09:00 - 10:00
		 * 10:00 - 11:00
		 * 11:00 - 12:00
		 * ...
		 * 20:00 - 21:00
		 *
		 * Ogni slot viene salvato come un record
		 * DisponibilitaCampo.
		 */

		LocalDate oggi = LocalDate.now();

		for (int giorno = 1; giorno <= 14; giorno++) {

			LocalDate data = oggi.plusDays(giorno);


			// Dalle 09:00 alle 21:00
			for (int ora = 9; ora < 21; ora++) {

				LocalDateTime inizio =
						data.atTime(ora, 0);

				LocalDateTime fine =
						data.atTime(ora + 1, 0);


				DisponibilitaCampo disponibilita =
						new DisponibilitaCampo();


				// ==========================================
				// CAMPO
				// ==========================================

				disponibilita.setCampo(campo);


				// ==========================================
				// DATA
				// ==========================================

				/*
				 * La data identifica il giorno.
				 *
				 * Esempio:
				 *
				 * 2026-09-15 00:00
				 */

				disponibilita.setData(
						data.atStartOfDay()
				);


				// ==========================================
				// ORARIO SLOT
				// ==========================================

				disponibilita.setOraInizio(
						inizio
				);

				disponibilita.setOraFine(
						fine
				);


				// ==========================================
				// DISPONIBILITÀ
				// ==========================================

				disponibilita.setDisponibilita(
						true
				);

				disponibilita.setStatoDisponibilita(
						"DISPONIBILE"
				);


				// ==========================================
				// SALVATAGGIO
				// ==========================================

				disponibilitaRepository.save(
						disponibilita
				);
			}
		}
	}
}