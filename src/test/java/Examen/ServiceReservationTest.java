package Examen;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ServiceReservationTest {

    @Mock
    private GestionDonnees gestionDonneesMock;

    @InjectMocks
    private ServiceReservation serviceReservation;

    private Etudiant etudiantTest;
    private Produit produitTest;

    @BeforeEach
    void setUp() {
        etudiantTest = new Etudiant("E1", "Dupont", "Jean");
        produitTest = new Produit("P1", "PC Portable", new BigDecimal("10.00"), new BigDecimal("100.00"));
    }

    // ==========================================
    // A. Vérification de la disponibilité et des limites
    // ==========================================

    @Test
    void test1_etudiantIndisponibleDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(2);
        when(gestionDonneesMock.trouverEtudiantParId("E_INCONNU")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E_INCONNU", "P1", debut, fin);
        });
    }

    @Test
    void test2_produitIndisponibleDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(2);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P_INCONNU")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P_INCONNU", debut, fin);
        });
    }

    @Test
    void test3_dureeSuperieureA7JoursDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(8); // 9 jours inclus
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut, fin);
        });
    }

    @Test
    void test4_dureeEgaleA7JoursSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(6); // 7 jours inclus
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
        assertEquals(7, cmd.getDuree());
    }

    @Test
    void test5_dureeInferieureA7JoursSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(3); // 4 jours inclus
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
        assertEquals(4, cmd.getDuree());
    }

    @Test
    void test6_depassementQuota2CommandesDoitEchouer() {
        // Arrange
        LocalDate d1 = LocalDate.now().plusDays(1);
        LocalDate f1 = d1.plusDays(1);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        
        List<Commande> commandesExistantes = List.of(
            new Commande("C1", "E1", "P1", d1, f1, 2, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(20)),
            new Commande("C2", "E1", "P1", d1.plusDays(2), f1.plusDays(2), 2, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(20))
        );
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(commandesExistantes);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", d1.plusDays(5), f1.plusDays(5));
        });
    }

    // ==========================================
    // B. Vérification des dates
    // ==========================================

    @Test
    void test7_dateFinInferieureActuelleDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().minusDays(5);
        LocalDate fin = LocalDate.now().minusDays(2);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut, fin);
        });
    }

    @Test
    void test8_dateDebutSuperieureActuelleSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(2);
        LocalDate fin = debut.plusDays(2);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
    }

    @Test
    void test9_dateDebutInferieureActuelleDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().minusDays(1);
        LocalDate fin = LocalDate.now().plusDays(2);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut, fin);
        });
    }

    @Test
    void test10_dateDebutEgaleActuelleSucces() {
        // Arrange
        LocalDate debut = LocalDate.now();
        LocalDate fin = debut.plusDays(1);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
    }

    @Test
    void test11_dateFinSuperieureADebutSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(3);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
    }

    @Test
    void test12_dateFinInferieureADebutDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(3);
        LocalDate fin = LocalDate.now().plusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut, fin);
        });
    }

    @Test
    void test13_dateFinEgaleADebutSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut;
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
        assertEquals(1, cmd.getDuree());
    }

    // ==========================================
    // C. Vérification des champs obligatoires
    // ==========================================

    @Test
    void test14_idEtudiantNullDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver(null, "P1", debut, fin);
        });
    }

    @Test
    void test15_idProduitNullDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(1);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", null, debut, fin);
        });
    }

    @Test
    void test16_dateDebutNullDoitEchouer() {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", null, LocalDate.now().plusDays(1));
        });
    }

    @Test
    void test17_dateFinNullDoitEchouer() {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", LocalDate.now().plusDays(1), null);
        });
    }

    @Test
    void test18_tousChampsObligatoiresRenseignesSucces() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(2);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertNotNull(cmd);
    }

    // ==========================================
    // D. Vérification disponibilité / chevauchements
    // ==========================================

    @Test
    void test19_produitDejaReserveMemePeriodeDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(3);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        
        List<Commande> commandesProduit = List.of(
            new Commande("C1", "E2", "P1", debut, fin, 3, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(30))
        );
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(commandesProduit);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut, fin);
        });
    }

    @Test
    void test20_periodesQuiSeChevauchentDoitEchouer() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(2);
        LocalDate fin = debut.plusDays(4);
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        
        List<Commande> commandesProduit = List.of(
            new Commande("C1", "E2", "P1", debut, fin, 3, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(30))
        );
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(commandesProduit);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            serviceReservation.reserver("E1", "P1", debut.minusDays(1), debut.plusDays(1));
        });
    }

    @Test
    void test21_nouvelleReservationApresPrecedenteSucces() {
        // Arrange
        LocalDate debut1 = LocalDate.now().plusDays(1);
        LocalDate fin1 = debut1.plusDays(2);
        LocalDate debut2 = fin1.plusDays(1);
        LocalDate fin2 = debut2.plusDays(2);

        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        
        List<Commande> commandesProduit = List.of(
            new Commande("C1", "E2", "P1", debut1, fin1, 3, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(30))
        );
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(commandesProduit);

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut2, fin2);

        // Assert
        assertNotNull(cmd);
    }

    @Test
    void test22_aucuneDateCommuneSucces() {
        // Arrange
        LocalDate debut1 = LocalDate.now().plusDays(1);
        LocalDate fin1 = debut1.plusDays(2);
        LocalDate debut2 = LocalDate.now().plusDays(20);
        LocalDate fin2 = debut2.plusDays(2);

        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        
        List<Commande> commandesProduit = List.of(
            new Commande("C1", "E2", "P1", debut1, fin1, 3, BigDecimal.TEN, BigDecimal.TEN, BigDecimal.valueOf(30))
        );
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(commandesProduit);

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut2, fin2);

        // Assert
        assertNotNull(cmd);
    }

    // ==========================================
    // E. Vérification des tarifs et des montants
    // ==========================================

    @Test
    void test23_tarifJournalierPositifSucces() {
        // Arrange & Act
        Produit p = new Produit("P1", "Test", new BigDecimal("10.00"), BigDecimal.ZERO);

        // Assert
        assertEquals(0, new BigDecimal("10.00").compareTo(p.getTarifJournalier()));
    }

    @Test
    void test24_cautionPositiveSucces() {
        // Arrange & Act
        Produit p = new Produit("P1", "Test", BigDecimal.TEN, new BigDecimal("50.00"));

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(p.getCaution()));
    }

    @Test
    void test25_cautionNulleSucces() {
        // Arrange & Act
        Produit p = new Produit("P1", "Test", BigDecimal.TEN, BigDecimal.ZERO);

        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(p.getCaution()));
    }

    @Test
    void test26_tarifJournalierNegatifDoitEchouer() {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Produit("P1", "Test", new BigDecimal("-5.00"), BigDecimal.TEN);
        });
    }

    @Test
    void test27_tarifJournalierEgalAZeroDoitEchouer() {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Produit("P1", "Test", BigDecimal.ZERO, BigDecimal.TEN);
        });
    }

    @Test
    void test28_cautionNegativeDoitEchouer() {
        // Arrange, Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new Produit("P1", "Test", BigDecimal.TEN, new BigDecimal("-10.00"));
        });
    }

    @Test
    void test29_verifierCalculMontantLocation() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(4); // 5 jours * 10.00 = 50.00
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertEquals(0, new BigDecimal("50.00").compareTo(cmd.getMontantLocation()));
    }

    @Test
    void test30_verifierCalculMontantTotal() {
        // Arrange
        LocalDate debut = LocalDate.now().plusDays(1);
        LocalDate fin = debut.plusDays(4); // 5 jours (50.00) + caution (100.00) = 150.00
        when(gestionDonneesMock.trouverEtudiantParId("E1")).thenReturn(etudiantTest);
        when(gestionDonneesMock.trouverProduitParId("P1")).thenReturn(produitTest);
        when(gestionDonneesMock.trouverCommandesParEtudiant("E1")).thenReturn(new ArrayList<>());
        when(gestionDonneesMock.trouverCommandesParProduit("P1")).thenReturn(new ArrayList<>());

        // Act
        Commande cmd = serviceReservation.reserver("E1", "P1", debut, fin);

        // Assert
        assertEquals(0, new BigDecimal("150.00").compareTo(cmd.getMontantTotal()));
    }
}