package it.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrenotazioneRequest {

	private Integer disponibilitaCampoId;
	private Integer numeroGiocatori;
}
