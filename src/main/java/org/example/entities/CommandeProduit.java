package org.example.entities;

import javax.persistence.*;

/**
 * Ligne de commande : association N:N Commande <-> Produit portant un attribut (quantité).
 * @MapsId remplit automatiquement la clé composée à partir des associations au moment du flush,
 * ce qui permet de créer la ligne avant que la commande n'ait reçu son id.
 */
@Entity
@Table(name = "commande_produit")
public class CommandeProduit {
    @EmbeddedId
    private CommandeProduitPK id = new CommandeProduitPK();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("commandeId")
    @JoinColumn(name = "commande_id")
    private Commande commande;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("produitId")
    @JoinColumn(name = "produit_id")
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    public CommandeProduit() {}
    public CommandeProduit(Commande commande, Produit produit, int quantite) {
        this.commande = commande;
        this.produit = produit;
        this.quantite = quantite;
        this.id = new CommandeProduitPK(commande.getId(), produit.getId());
    }

    public CommandeProduitPK getId() { return id; }
    public Commande getCommande() { return commande; }
    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }
    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }
}
