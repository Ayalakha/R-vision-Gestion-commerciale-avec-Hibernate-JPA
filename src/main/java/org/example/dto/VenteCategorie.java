package org.example.dto;

import java.math.BigDecimal;

/** Une ligne du rapport de ventes : chiffre d'affaires d'une catégorie sur la période. */
public class VenteCategorie {
    private final String categorie;
    private final BigDecimal chiffreAffaires;

    public VenteCategorie(String categorie, BigDecimal chiffreAffaires) {
        this.categorie = categorie;
        this.chiffreAffaires = chiffreAffaires;
    }

    public String getCategorie() { return categorie; }
    public BigDecimal getChiffreAffaires() { return chiffreAffaires; }

    @Override
    public String toString() { return categorie + " : " + chiffreAffaires; }
}
