package Examen;

import java.math.BigDecimal;

public class Produit {
    private String idProduit;
    private String libelle;
    private BigDecimal tarifJournalier;
    private BigDecimal caution;

    public Produit(String idProduit, String libelle, BigDecimal tarifJournalier, BigDecimal caution) {
        this.idProduit = idProduit;
        this.libelle = libelle;
        this.tarifJournalier = tarifJournalier;
        this.caution = caution;
    }

    public String getIdProduit() { return idProduit; }
    public String getLibelle() { return libelle; }
    public BigDecimal getTarifJournalier() { return tarifJournalier; }
    public BigDecimal getCaution() { return caution; }
}