package com.spring.teststock.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

//c'est-à-dire qu'elle n'est pas une entité indépendante, mais qu'elle peut être incluse dans une autre entité. Cela permet de
//réutiliser des structures de données complexes dans plusieurs entités sans créer de nouvelles tables pour elles.
//
//Dans votre code, la classe Adresse est marquée avec
//@Embeddable, ce qui signifie que ses champs (adresse1, adresse2, ville, codePostale, pays) seront inclus
//directement dans la table de l'entité qui l'utilise.
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Embeddable
public class Adresse implements Serializable {

    @Column(name = "adresse1")
    private String adresse1;

    @Column(name = "adresse2")
    private String adresse2;

    @Column(name = "ville")
    private String ville;

    @Column(name = "codepostale")
    private String codePostale;

    @Column(name = "pays")
    private String pays;
}
