package org.example.service;

import org.example.dao.ProduitDAO;
import org.example.entities.Produit;

import java.math.BigDecimal;
import java.util.List;

public class ProduitService extends CrudService<Produit, Long> {
    private final ProduitDAO produitDAO;

    public ProduitService() {
        this(new ProduitDAO());
    }

    private ProduitService(ProduitDAO produitDAO) {
        super(produitDAO);
        this.produitDAO = produitDAO;
    }

    @Override
    public void create(Produit p) {
        valider(p);
        super.create(p);
    }

    @Override
    public Produit update(Produit p) {
        valider(p);
        return super.update(p);
    }

    public List<Produit> findByCategorie(Long catId) { return produitDAO.findByCategorie(catId); }
    public List<Produit> searchByLibelle(String term) { return produitDAO.searchByLibelle(term); }
    public Produit getWithCategorie(Long id) { return produitDAO.findWithCategorie(id); }

    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        return produitDAO.search(term, minPrix, maxPrix);
    }

    private static void valider(Produit p) {
        if (p == null) throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        if (p.getLibelle() == null || p.getLibelle().isBlank())
            throw new IllegalArgumentException("Le libellé est obligatoire");
        if (p.getPrix() != null && p.getPrix().signum() < 0)
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        if (p.getCategorie() == null)
            throw new IllegalArgumentException("La catégorie est obligatoire");
    }
}
