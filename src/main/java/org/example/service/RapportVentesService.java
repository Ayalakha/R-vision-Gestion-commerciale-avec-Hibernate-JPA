package org.example.service;

import org.example.dto.VenteCategorie;
import org.example.entities.Categorie;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;
import org.example.entities.Produit;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exercice de synthèse : chiffre d'affaires (SUM(prix × quantité)) par catégorie sur un mois,
 * calculé entièrement en base avec la Criteria API.
 *
 * SQL équivalent :
 *   SELECT c.nom, SUM(p.prix * l.quantite)
 *   FROM commande_produit l JOIN produits p ... JOIN categories c ... JOIN commandes cmd ...
 *   WHERE cmd.date_commande BETWEEN :debut AND :fin
 *   GROUP BY c.id, c.nom ORDER BY 2 DESC
 */
public class RapportVentesService {

    public List<VenteCategorie> rapportMoisPrecedent() {
        return rapportMensuel(YearMonth.now().minusMonths(1));
    }

    public List<VenteCategorie> rapportMensuel(YearMonth mois) {
        LocalDate debut = mois.atDay(1);
        LocalDate fin = mois.atEndOfMonth();

        EntityManager em = JpaUtil.createEntityManager();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tuple> cq = cb.createTupleQuery();

            Root<CommandeProduit> ligne = cq.from(CommandeProduit.class);
            Join<CommandeProduit, Produit> produit = ligne.join("produit");
            Join<Produit, Categorie> categorie = produit.join("categorie");
            Join<CommandeProduit, Commande> commande = ligne.join("commande");

            // prix × quantité, puis somme par catégorie
            Expression<Number> montant = cb.prod(produit.<BigDecimal>get("prix"), ligne.<Integer>get("quantite"));
            Expression<Number> total = cb.sum(montant);

            cq.multiselect(categorie.get("nom"), total)
              .where(cb.between(commande.<LocalDate>get("dateCommande"), debut, fin))
              .groupBy(categorie.get("id"), categorie.get("nom"))
              .orderBy(cb.desc(total));

            return em.createQuery(cq).getResultList().stream()
                    .map(t -> new VenteCategorie(t.get(0, String.class), toBigDecimal(t.get(1))))
                    .collect(Collectors.toList());
        } finally {
            em.close();
        }
    }

    private static BigDecimal toBigDecimal(Object value) {
        if (value == null) return BigDecimal.ZERO.setScale(2);
        BigDecimal bd = value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
        return bd.setScale(2, RoundingMode.HALF_UP);
    }
}
