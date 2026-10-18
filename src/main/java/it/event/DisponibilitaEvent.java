package it.event;


import java.time.LocalDate;
//è un semplice record (DTO immutabile) che rappresenta “il campo X ha avuto un cambio di disponibilità nella data Y” — nessuna logica, solo un contenitore di dati.

public record DisponibilitaEvent(Integer idCampo, LocalDate data) {}