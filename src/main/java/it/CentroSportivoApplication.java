package it;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
// senza @EnableScheduling heartbeat() in DisponibilitaEventService non parte mai e la connessione SSE verrà chiusa da eventuali proxy/firewall per inattività

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CentroSportivoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CentroSportivoApplication.class, args);
	}

}
