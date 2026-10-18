
package it.mapper;

import it.dto.PrenotazioneDto;
import it.model.Prenotazione;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class PrenotazioneMapper
		extends AbstractConverter<Prenotazione, PrenotazioneDto> {

	@Override
	public Prenotazione toEntity(PrenotazioneDto dto) {
		if (dto == null) {
			return null;
		}

		Prenotazione entity = new Prenotazione();

		entity.setId(dto.getId());
		entity.setDataPrenotazione(dto.getDataPrenotazione());
		entity.setNumeroGiocatori(dto.getNumeroGiocatori());
		entity.setCostoTotale(dto.getCostoTotale());

		if (dto.getStatoPrenotazione() != null) {
			entity.setStatoPrenotazione(
					it.enumerated.StatoPrenotazione.valueOf(
							dto.getStatoPrenotazione()
					)
			);
		}

		// Le relazioni vengono assegnate nel service,
		// recuperando le entità dal database.
		// Non convertire automaticamente i DTO annidati.

		return entity;
	}

	@Override
	public PrenotazioneDto toDTO(Prenotazione entity) {
		if (entity == null) {
			return null;
		}

		PrenotazioneDto dto = new PrenotazioneDto();

		dto.setId(entity.getId());
		dto.setDataPrenotazione(entity.getDataPrenotazione());
		dto.setNumeroGiocatori(entity.getNumeroGiocatori());
		dto.setCostoTotale(entity.getCostoTotale());

		if (entity.getStatoPrenotazione() != null) {
			dto.setStatoPrenotazione(
					entity.getStatoPrenotazione().name()
			);
		}

		// Per ora non convertiamo automaticamente:
		// disponibilitaCampo, utenteCreato e pagamento.

		return dto;
	}

	public Page<PrenotazioneDto> toDTOPage(
			Page<Prenotazione> prenotazioni) {
		return prenotazioni.map(this::toDTO);
	}
}
