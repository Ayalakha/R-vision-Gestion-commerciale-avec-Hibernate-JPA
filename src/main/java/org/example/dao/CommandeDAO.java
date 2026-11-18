package org.example.dao;

import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;

public class CommandeDAO extends GenericDAO<Commande, Long> {

    public CommandeDAO() {
        super(Commande.class);
    }

    /**
     * Les produits passés à Commande.addProduit() viennent généralement d'un autre EntityManager
     * (déjà fermé) : ils sont "detached". Avec @MapsId, Hibernate refuserait de les persister
     * ("detached entity passed to persist"). On les remplace donc par des références gérées
     * par l'EntityManager courant avant de persister la commande et ses lignes.
     */
    @Override
    public void create(Commande commande) {
        write(em -> {
            for (CommandeProduit ligne : commande.getLignes()) {
                Long produitId = ligne.getProduit().getId();
                if (produitId == null) {
                    throw new IllegalArgumentException("Le produit doit être enregistré avant d'être commandé");
                }
                ligne.setProduit(em.getReference(Produit.class, produitId));
            }
            em.persist(commande);
            return null;
        });
    }
}
