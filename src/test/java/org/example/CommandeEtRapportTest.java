package org.example;

import org.example.dto.VenteCategorie;
import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;
import org.example.util.JpaUtil;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class CommandeEtRapportTest {
    private final CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
    private final ProduitService produitService = new ProduitService();
    private final CommandeService commandeService = new CommandeService();

    @Test
    public void testCommandeAvecDeuxProduits() {
        Categorie cat = new Categorie("Bureautique");
        catService.create(cat);
        Produit p1 = new Produit("Agrafeuse", new BigDecimal("8.00"), cat);
        Produit p2 = new Produit("Classeur", new BigDecimal("3.50"), cat);
        produitService.create(p1);
        produitService.create(p2);

        Commande cmd = new Commande(LocalDate.now());
        cmd.addProduit(produitService.getById(p1.getId()), 3);
        cmd.addProduit(produitService.getById(p2.getId()), 5);
        commandeService.create(cmd);
        assertNotNull(cmd.getId());

        EntityManager em = JpaUtil.createEntityManager();
        try {
            List<CommandeProduit> lignes = em.createQuery(
                    "SELECT l FROM CommandeProduit l WHERE l.commande.id = :id ORDER BY l.quantite", CommandeProduit.class)
                    .setParameter("id", cmd.getId()).getResultList();
            assertEquals(2, lignes.size());
            // La clé composée a été remplie par @MapsId
            assertEquals(cmd.getId(), lignes.get(0).getId().getCommandeId());
            assertEquals(p1.getId(), lignes.get(0).getId().getProduitId());
            assertEquals(5, lignes.get(1).getQuantite());
        } finally {
            em.close();
        }
    }

    @Test
    public void testRapportVentesMoisPrecedent() {
        Categorie livres = new Categorie("Rapport-Livres");
        Categorie jeux = new Categorie("Rapport-Jeux");
        catService.create(livres);
        catService.create(jeux);
        Produit roman = new Produit("Roman", new BigDecimal("12.50"), livres);
        Produit bd = new Produit("BD", new BigDecimal("10.00"), livres);
        Produit puzzle = new Produit("Puzzle", new BigDecimal("20.00"), jeux);
        produitService.create(roman);
        produitService.create(bd);
        produitService.create(puzzle);

        YearMonth moisPrecedent = YearMonth.now().minusMonths(1);

        Commande c1 = new Commande(moisPrecedent.atDay(3));
        c1.addProduit(roman, 2);   // 25.00
        c1.addProduit(puzzle, 1);  // 20.00
        Commande c2 = new Commande(moisPrecedent.atEndOfMonth());
        c2.addProduit(bd, 4);      // 40.00
        c2.addProduit(puzzle, 3);  // 60.00
        Commande horsPeriode = new Commande(moisPrecedent.plusMonths(1).atDay(1));
        horsPeriode.addProduit(roman, 100); // ne doit pas être comptée
        commandeService.create(c1);
        commandeService.create(c2);
        commandeService.create(horsPeriode);

        List<VenteCategorie> rapport = new RapportVentesService().rapportMoisPrecedent();
        Map<String, BigDecimal> ca = rapport.stream()
                .collect(Collectors.toMap(VenteCategorie::getCategorie, VenteCategorie::getChiffreAffaires));

        assertEquals(new BigDecimal("65.00"), ca.get("Rapport-Livres"));
        assertEquals(new BigDecimal("80.00"), ca.get("Rapport-Jeux"));
        // Trié par chiffre d'affaires décroissant
        List<String> ordre = rapport.stream().map(VenteCategorie::getCategorie).collect(Collectors.toList());
        assertTrue(ordre.indexOf("Rapport-Jeux") < ordre.indexOf("Rapport-Livres"));
    }
}
