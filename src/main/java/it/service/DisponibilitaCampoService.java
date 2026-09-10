package it.service;

import it.dto.DisponibilitaCampoDto;
import it.mapper.Converter;
import it.mapper.DisponibilitaCampoMapper;
import it.model.Campo;
import it.model.DisponibilitaCampo;
import it.repository.CampoRepository;
import it.repository.DisponibilitaCampoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class DisponibilitaCampoService
		extends AbstractService<DisponibilitaCampo, DisponibilitaCampoDto> {

	private final DisponibilitaCampoMapper disponibilitaCampoMapper;
	private final DisponibilitaCampoRepository disponibilitaCampoRepository;
	private final CampoRepository campoRepository;

	protected DisponibilitaCampoService(
			JpaRepository<DisponibilitaCampo, Integer> repository,
			Converter<DisponibilitaCampo, DisponibilitaCampoDto> converter,
			DisponibilitaCampoMapper disponibilitaCampoMapper,
			DisponibilitaCampoRepository disponibilitaCampoRepository,
			CampoRepository campoRepository) {

		super(repository, converter);

		this.disponibilitaCampoMapper = disponibilitaCampoMapper;
		this.disponibilitaCampoRepository = disponibilitaCampoRepository;
		this.campoRepository = campoRepository;
	}

	// 1. TUTTE LE DISPONIBILITÀ
	public List<DisponibilitaCampoDto> getAllDisponibilita() {

		List<DisponibilitaCampo> lista =
				disponibilitaCampoRepository.findAll();

		return lista.stream()
				.map(disponibilitaCampoMapper::toDTO)
				.toList();
	}

	// 2. DISPONIBILITÀ DI UNA DATA
	public List<DisponibilitaCampoDto> getDisponibilitaByData(
			LocalDate data) throws Exception {

		LocalDateTime inizio = data.atStartOfDay();

		LocalDateTime fine = data.atTime(LocalTime.MAX);

		List<DisponibilitaCampo> lista =
				disponibilitaCampoRepository.findByDataBetween(
						inizio,
						fine
				);

		if (lista.isEmpty()) {

			throw new Exception(
					"Nessuna disponibilità trovata per la data "
							+ data
			);
		}

		return lista.stream()
				.map(disponibilitaCampoMapper::toDTO)
				.toList();
	}

	// 3. DISPONIBILITÀ DI UN CAMPO
	public List<DisponibilitaCampoDto> getDisponibilitaByCampo(
			Integer idCampo) throws Exception {

		// Controlliamo che il campo esista
		campoRepository.findById(idCampo)
				.orElseThrow(() ->
						new Exception("Campo non trovato")
				);

		List<DisponibilitaCampo> lista =
				disponibilitaCampoRepository.findByCampoId(idCampo);

		return lista.stream()
				.map(disponibilitaCampoMapper::toDTO)
				.toList();
	}


	// 4. DISPONIBILITÀ DI UN CAMPO IN UNA DATA
	public List<DisponibilitaCampoDto> getDisponibilitaByCampoEData(
			Integer idCampo,
			LocalDate data) throws Exception {
		campoRepository.findById(idCampo)
				.orElseThrow(() ->
						new Exception("Campo non trovato")
				);

		LocalDateTime inizio = data.atStartOfDay();

		LocalDateTime fine = data.atTime(LocalTime.MAX);

		List<DisponibilitaCampo> lista =
				disponibilitaCampoRepository
						.findByCampoIdAndDataBetween(
								idCampo,
								inizio,
								fine
						);

		if (lista.isEmpty()) {

			throw new Exception(
					"Nessuna disponibilità trovata per il campo "
							+ idCampo
							+ " nella data "
							+ data
			);
		}

		return lista.stream()
				.map(disponibilitaCampoMapper::toDTO)
				.toList();
	}
}