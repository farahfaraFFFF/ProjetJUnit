package Examen;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class ServiceReservation {

    private GestionDonnees gestionDonnees;

    
    public ServiceReservation(GestionDonnees gestionDonnees) {
        this.gestionDonnees = gestionDonnees;
    }

    public Commande reserver(String idEtudiant, String idProduit, LocalDate dateDebut, LocalDate dateFin) {
        
        // RG01 & RG02 : Champs obligatoires
        if (idEtudiant == null || idEtudiant.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de l'étudiant est obligatoire.");
        }
        if (idProduit == null || idProduit.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant du produit est obligatoire.");
        }
        if (dateDebut == null || dateFin == null) {
            throw new IllegalArgumentException("Les dates sont obligatoires.");
        }

        LocalDate aujourdHui = LocalDate.now();
        // RG03 : Date de début >= date courante
        if (dateDebut.isBefore(aujourdHui)) {
            throw new IllegalArgumentException("La date de début doit être égale ou postérieure à la date courante.");
        }
        // RG03 : Date de fin >= date de début
        if (dateFin.isBefore(dateDebut)) {
            throw new IllegalArgumentException("La date de fin doit être égale ou postérieure à la date de début.");
        }

        // RG04 : L'étudiant doit exister via le mock
        Etudiant etudiant = gestionDonnees.trouverEtudiantParId(idEtudiant);
        if (etudiant == null) {
            throw new IllegalArgumentException("L'étudiant n'existe pas.");
        }

        // RG04 : Le produit doit exister via le mock
        Produit produit = gestionDonnees.trouverProduitParId(idProduit);
        if (produit == null) {
            throw new IllegalArgumentException("Le produit n'existe pas.");
        }

        // RG05 : Calcul durée (incluses)
        long duree = ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        if (duree > 7) {
            throw new IllegalArgumentException("La durée maximale est de 7 jours.");
        }

        // RG06 : Max 2 commandes en cours ou à venir pour l'étudiant
        List<Commande> commandesEtudiant = gestionDonnees.trouverCommandesParEtudiant(idEtudiant);
        long nbCommandesEnCours = commandesEtudiant.stream()
                .filter(c -> !c.getDateFin().isBefore(aujourdHui))
                .count();
        if (nbCommandesEnCours >= 2) {
            throw new IllegalArgumentException("L'étudiant a atteint son quota maximum de 2 commandes.");
        }

        // RG08 : Disponibilité du produit (vérification des chevauchements de dates)
        List<Commande> commandesProduit = gestionDonnees.trouverCommandesParProduit(idProduit);
        boolean produitIndisponible = commandesProduit.stream()
                .anyMatch(c -> !(dateFin.isBefore(c.getDateDebut()) || dateDebut.isAfter(c.getDateFin())));
        if (produitIndisponible) {
            throw new IllegalArgumentException("Le produit est déjà réservé sur cette période.");
        }

        // RG10, RG11, RG12 : Calculs monétaires avec BigDecimal et HALF_UP
        BigDecimal montantLocation = produit.getTarifJournalier()
                .multiply(BigDecimal.valueOf(duree))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal caution = produit.getCaution();
        BigDecimal montantTotal = montantLocation.add(caution);

        // RG13 : Référence unique
        String reference = "CMD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        Commande nouvelleCommande = new Commande(reference, idEtudiant, idProduit, dateDebut, dateFin, duree, montantLocation, caution, montantTotal);
        gestionDonnees.sauvegarderCommande(nouvelleCommande);

        return nouvelleCommande;
    }
}