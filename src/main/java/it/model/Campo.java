package it.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "sportivo" ,name = "campo")
public class Campo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_campo")
    private Integer id;

    @Column(name="nome")
    private String nome;

    @Column(name="tipologia")
    private String tipologia;

    @Column(name="prezzo")
    private BigDecimal prezzo;

    @Column(name="coperto")
    private Boolean coperto;



    @OneToMany(mappedBy = "campo")
    private List<DisponibilitaCampo> listaDisponibilita=new ArrayList<>();


    @OneToMany(mappedBy = "campo")
    private List<Corso> listaCorsi=new ArrayList<>();


}
