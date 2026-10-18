package it.repository;

import it.model.DisponibilitaCampo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import it.enumerated.StatoDisponibilita;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

	//L'intervallo inizio incluso e fine escluso evita di includere la mezzanotte del giorno successivo.
	@Query("""
    select d
    from DisponibilitaCampo d
    where d.campo.id = :idCampo
      and d.statoDisponibilita= :stato
      and d.data >= :inizio
      and d.data < :fine
      and d.oraInizio > :adesso
    order by d.oraInizio
    """)
	List<DisponibilitaCampo> findLibereByCampoAndData(
			@Param("idCampo") Integer idCampo,
			@Param("inizio") LocalDateTime inizio,
			@Param("fine") LocalDateTime fine,
			@Param("adesso") LocalDateTime adesso,
			@Param("stato") StatoDisponibilita stato
	);
	// Serve a PrenotazioneService per bloccare la riga ed evitare doppie prenotazioni
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select d from DisponibilitaCampo d where d.id = :id")
	Optional<DisponibilitaCampo> findByIdForUpdate(@Param("id") Integer id);
}
