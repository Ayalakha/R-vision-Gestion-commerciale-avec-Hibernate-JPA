package org.example;

import org.example.entities.*;
import org.example.service.CommandeService;
import org.example.service.CrudService;
import org.example.service.ProduitService;
import org.example.service.RapportVentesService;
import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Démonstration en console de toutes les étapes du lab, sur la base MySQL configurée
 * dans META-INF/persistence.xml (à lancer depuis l'IDE).
 */
public class App {
    public static void main(String[] args) {
        try {
            CrudService<Categorie, Long> catService = new CrudService<>(Categorie.class);
            ProduitService produitService = new ProduitService();
            CommandeService commandeService = new CommandeService();
            CrudService<User, Long> userService = new CrudService<>(User.class);

            // 1. Catégorie + produits (suffixe pour respecter la contrainte unique sur nom)
            String suffixe = " #" + System.currentTimeMillis() % 100000;
            Categorie info = new Categorie("Informatique" + suffixe);
            catService.create(info);
            System.out.println("Catégorie créée : " + info);

            Produit clavier = new Produit("Clavier", new BigDecimal("49.99"), info);
            Produit souris = new Produit("Souris", new BigDecimal("19.90"), info);
            produitService.create(clavier);
            produitService.create(souris);

            // 2. Named query, Criteria, pagination
            System.out.println("Produits de la catégorie : " + produitService.findByCategorie(info.getId()));
            System.out.println("Recherche 'sou' : " + produitService.searchByLibelle("sou"));
            System.out.println("Criteria 10€–60€ : " + produitService.search(null, new BigDecimal("10"), new BigDecimal("60")));
            System.out.println("Page 1 (taille 5) : " + produitService.getPage(1, 5));

            // 3. Héritage JOINED : une requête sur User renvoie les sous-types
            userService.create(new Client("Alice", LocalDate.now()));
            userService.create(new Fournisseur("ACME", "12 rue des Lilas"));
            System.out.println("Utilisateurs : " + userService.getAll());

            // 4. Commande N:N avec clé composée, datée du mois précédent pour le rapport
            Commande cmd = new Commande(LocalDate.now().minusMonths(1));
            cmd.addProduit(clavier, 3);
            cmd.addProduit(souris, 5);
            commandeService.create(cmd);
            System.out.println("Commande créée avec ID = " + cmd.getId());

            // 5. Transaction explicite annulée
            EntityManager em = JpaUtil.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                Produit p = new Produit("Écran", new BigDecimal("-1"), em.getReference(Categorie.class, info.getId()));
                em.persist(p);
                if (p.getPrix().signum() < 0) throw new IllegalArgumentException("Prix négatif");
                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                System.err.println("Transaction annulée : " + e.getMessage());
            } finally {
                em.close();
            }

            // 6. Rapport de ventes du mois précédent
            System.out.println("Rapport du mois précédent : " + new RapportVentesService().rapportMoisPrecedent());
        } finally {
            JpaUtil.close();
        }
    }
}
