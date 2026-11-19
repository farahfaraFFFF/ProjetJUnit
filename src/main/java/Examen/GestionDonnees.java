
package Examen;

import java.util.List;

public interface GestionDonnees {
    Etudiant trouverEtudiantParId(String idEtudiant);
    Produit trouverProduitParId(String idProduit);
    List<Commande> trouverCommandesParEtudiant(String idEtudiant);
    List<Commande> trouverCommandesParProduit(String idProduit);
    void sauvegarderCommande(Commande commande);
}