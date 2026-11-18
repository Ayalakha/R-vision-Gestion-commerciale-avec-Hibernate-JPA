package org.example;

import org.example.entities.Categorie;
import org.example.entities.Produit;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.*;

public class ProduitDaoTest {
    private static CrudService<Categorie, Long> catService;
    private static ProduitService prodService;

    @BeforeClass
    public static void init() {
        catService = new CrudService<>(Categorie.class);
        prodService = new ProduitService();
    }

    @Test
    public void testCreateProduitAvecCategorie() {
        Categorie cat = new Categorie("Papeterie");
        catService.create(cat);
        Produit p = new Produit("Stylo", new BigDecimal("1.20"), cat);
        prodService.create(p);
        assertNotNull(p.getId());

        // JOIN FETCH : la catégorie est chargée avant la fermeture de l'EntityManager
        Produit fetched = prodService.getWithCategorie(p.getId());
        assertEquals("Papeterie", fetched.getCategorie().getNom());
    }

    @Test
    public void testNamedQueriesEtCriteria() {
        Categorie cat = new Categorie("Audio");
        catService.create(cat);
        prodService.create(new Produit("Casque Bluetooth", new BigDecimal("89.00"), cat));
        prodService.create(new Produit("Enceinte", new BigDecimal("45.50"), cat));
        prodService.create(new Produit("Câble jack", new BigDecimal("5.00"), cat));

        assertEquals(3, prodService.findByCategorie(cat.getId()).size());
        assertEquals(1, prodService.searchByLibelle("BLUETOOTH").size());

        List<Produit> entre10et50 = prodService.search(null, new BigDecimal("10"), new BigDecimal("50"));
        assertTrue(entre10et50.stream().anyMatch(p -> p.getLibelle().equals("Enceinte")));
        assertTrue(entre10et50.stream().allMatch(p -> p.getPrix().compareTo(new BigDecimal("10")) >= 0
                && p.getPrix().compareTo(new BigDecimal("50")) <= 0));
        assertEquals(1, prodService.search("cas", new BigDecimal("50"), null).size());
    }

    @Test
    public void testPagination() {
        Categorie cat = new Categorie("Pagination");
        catService.create(cat);
        for (int i = 1; i <= 7; i++) {
            prodService.create(new Produit("Article " + i, BigDecimal.ONE, cat));
        }
        long total = prodService.count();
        List<Produit> page1 = prodService.getPage(1, 3);
        List<Produit> page2 = prodService.getPage(2, 3);
        assertEquals(3, page1.size());
        assertEquals(Math.min(3, total - 3), page2.size());
        assertTrue(page1.get(2).getId() < page2.get(0).getId());
    }

    @Test
    public void testUpdateEtDelete() {
        Categorie cat = new Categorie("Jardin");
        catService.create(cat);
        Produit p = new Produit("Pelle", new BigDecimal("15.00"), cat);
        prodService.create(p);

        p.setPrix(new BigDecimal("12.50"));
        prodService.update(p);
        assertEquals(0, new BigDecimal("12.50").compareTo(prodService.getById(p.getId()).getPrix()));

        prodService.delete(p);
        assertNull(prodService.getById(p.getId()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrixNegatifRefuse() {
        Categorie cat = new Categorie("Validation");
        catService.create(cat);
        prodService.create(new Produit("Invalide", new BigDecimal("-3"), cat));
    }

    @Test
    public void testOrphanRemoval() {
        Categorie cat = new Categorie("Orphelins");
        Produit p = new Produit("Orphelin", BigDecimal.TEN, null);
        cat.addProduit(p);
        catService.create(cat); // cascade ALL : le produit est persisté avec la catégorie
        assertNotNull(p.getId());

        catService.delete(cat); // cascade REMOVE : le produit disparaît aussi
        assertNull(prodService.getById(p.getId()));
    }
}
