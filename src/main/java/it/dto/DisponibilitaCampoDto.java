package it.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import it.enumerated.StatoDisponibilita;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
public class DisponibilitaCampoDto {
    private Integer id;
    private StatoDisponibilita statoDisponibilita;
    private LocalDateTime data;
    private LocalDateTime oraInizio;
    private LocalDateTime oraFine;

	@JsonIgnore
    private PrenotazioneDto prenotazioneCampo;
    @JsonIgnore
    private CampoDto campo;
}
