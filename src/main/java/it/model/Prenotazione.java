package it.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "sportivo",name = "prenotazione")
public class Prenotazione {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_prenotazione")
    private Integer id;

    @Column(name="data_prenotazione")
    private LocalDateTime dataPrenotazione;

    @Column(name="num_giocatori")
    private Integer numeroGiocatori;

    @Column(name="costo_totale")
    private BigDecimal costoTotale;

    @Column(name="stato_prenotazione")
    private String statoPrenotazione;

    @JoinColumn(name="disponibilita_id")
    @ManyToOne
    private DisponibilitaCampo disponibilitaCampo;

    @JoinColumn(name="utente_id_creato")
    @ManyToOne
    private Utente utenteCreato;

    @OneToOne(mappedBy = "prenotazione",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Pagamento pagamento;
}
