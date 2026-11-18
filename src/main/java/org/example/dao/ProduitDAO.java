package org.example.dao;

import org.example.entities.Produit;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Requêtes spécifiques au Produit : Named Queries, JOIN FETCH, Criteria API dynamique. */
public class ProduitDAO extends GenericDAO<Produit, Long> {

    public ProduitDAO() {
        super(Produit.class);
    }

    public List<Produit> findByCategorie(Long catId) {
        return read(em -> em.createNamedQuery("Produit.findByCategorie", Produit.class)
                .setParameter("catId", catId)
                .getResultList());
    }

    public List<Produit> searchByLibelle(String term) {
        return read(em -> em.createNamedQuery("Produit.searchByLibelle", Produit.class)
                .setParameter("term", term)
                .getResultList());
    }

    /**
     * Charge le produit ET sa catégorie en une requête : la catégorie reste accessible
     * après fermeture de l'EntityManager (évite la LazyInitializationException).
     */
    public Produit findWithCategorie(Long id) {
        return read(em -> {
            TypedQuery<Produit> q = em.createQuery(
                    "SELECT p FROM Produit p JOIN FETCH p.categorie WHERE p.id = :id", Produit.class);
            List<Produit> res = q.setParameter("id", id).getResultList();
            return res.isEmpty() ? null : res.get(0);
        });
    }

    /** Recherche multicritère : chaque filtre null/vide est ignoré. */
    public List<Produit> search(String term, BigDecimal minPrix, BigDecimal maxPrix) {
        return read(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Produit> cq = cb.createQuery(Produit.class);
            Root<Produit> root = cq.from(Produit.class);

            List<Predicate> predicates = new ArrayList<>();
            if (term != null && !term.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("libelle")), "%" + term.toLowerCase() + "%"));
            }
            if (minPrix != null) {
                predicates.add(cb.ge(root.get("prix"), minPrix));
            }
            if (maxPrix != null) {
                predicates.add(cb.le(root.get("prix"), maxPrix));
            }
            cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(root.get("id")));
            return em.createQuery(cq).getResultList();
        });
    }
}
