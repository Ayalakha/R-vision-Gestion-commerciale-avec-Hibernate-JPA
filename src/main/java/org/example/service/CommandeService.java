package org.example.service;

import org.example.dao.CommandeDAO;
import org.example.entities.Commande;
import org.example.entities.CommandeProduit;

public class CommandeService extends CrudService<Commande, Long> {

    public CommandeService() {
        super(new CommandeDAO());
    }

    @Override
    public void create(Commande commande) {
        if (commande == null) throw new IllegalArgumentException("L'entité ne doit pas être nulle");
        if (commande.getLignes().isEmpty())
            throw new IllegalArgumentException("Une commande doit contenir au moins un produit");
        for (CommandeProduit l : commande.getLignes()) {
            if (l.getQuantite() <= 0) throw new IllegalArgumentException("La quantité doit être positive");
        }
        super.create(commande);
    }
}
