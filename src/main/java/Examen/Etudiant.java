package Examen;

public class Etudiant {
    private String idEtudiant;
    private String nom;
    private String prenom;

    public Etudiant(String idEtudiant, String nom, String prenom) {
        this.idEtudiant = idEtudiant;
        this.nom = nom;
        this.prenom = prenom;
    }

    public String getIdEtudiant() { return idEtudiant; }
    public String getNom() { return nom; }
    public String getPrenom() { return prenom; }
}