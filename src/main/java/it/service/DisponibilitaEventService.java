package it.service;

import it.event.DisponibilitaEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

//È il “centralino” che tiene aperte le connessioni con i browser e li avvisa quando cambia la disponibilità di un campo.
//In modo asincrono, per avvisare tutti gli altri utenti con la pagina aperta che qualcosa è cambiato
@Service
public class DisponibilitaEventService {

	//Se hai 10 utenti con la pagina aperta, la lista ne contiene 10. Uso CopyOnWriteArrayList perché
	// più thread la modificano contemporaneamente (utenti che si collegano, scollegano)
	private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

	public SseEmitter subscribe() {
		//crea la connessione e rimane aperte con 0L
		SseEmitter emitter = new SseEmitter(0L);
		emitters.add(emitter);
		emitter.onCompletion(() -> emitters.remove(emitter));
		emitter.onTimeout(() -> emitters.remove(emitter));
		emitter.onError(e -> emitters.remove(emitter));
		//le 3 callback , che rimuovono la connessione della modale di registrazione di un utente, dalla lista quando la chiude
		return emitter;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void onCambioDisponibilita(DisponibilitaEvent event) {
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().name("disponibilita").data(event));
			} catch (Exception e) {
				emitters.remove(emitter);
			}
		}
	}

	//Ogni 25 secondi manda un commento ping a tutti. Il browser lo ignora, ma proxy, firewall e load balancer
	// vedono traffico e non chiudono la connessione per inattività. Inoltre, se un client è sparito senza avvisare, l’invio fallisce e lo togliamo dalla lista

	@Scheduled(fixedRate = 25000)
	public void heartbeat() {
		for (SseEmitter emitter : emitters) {
			try {
				emitter.send(SseEmitter.event().comment("ping"));
			} catch (Exception e) {
				emitters.remove(emitter);
			}
		}
	}
}
