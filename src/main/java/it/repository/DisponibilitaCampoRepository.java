package it.repository;

import it.model.DisponibilitaCampo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DisponibilitaCampoRepository extends JpaRepository<DisponibilitaCampo,Integer> {

    List<DisponibilitaCampo> findByCampoId(Integer idCampo);

	List<DisponibilitaCampo> findByDataBetween(
			LocalDateTime inizio,
			LocalDateTime fine
	);

	// Slot di un determinato campo in una determinata giornata
	List<DisponibilitaCampo> findByCampoIdAndDataBetween(
			Integer idCampo,
			LocalDateTime inizio,
			LocalDateTime fine
	);
}
