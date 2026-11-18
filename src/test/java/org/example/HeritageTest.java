package org.example;

import org.example.entities.Client;
import org.example.entities.Fournisseur;
import org.example.entities.User;
import org.example.service.CrudService;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.*;

public class HeritageTest {

    @Test
    public void testRequetePolymorphiqueJoined() {
        CrudService<User, Long> users = new CrudService<>(User.class);
        Client client = new Client("Alice", LocalDate.of(2026, 1, 15));
        Fournisseur fournisseur = new Fournisseur("ACME", "12 rue des Lilas");
        users.create(client);
        users.create(fournisseur);

        // Une requête sur User renvoie les bons sous-types (jointures automatiques)
        List<User> tous = users.getAll();
        assertTrue(tous.stream().anyMatch(u -> u instanceof Client && u.getId().equals(client.getId())));
        assertTrue(tous.stream().anyMatch(u -> u instanceof Fournisseur && u.getId().equals(fournisseur.getId())));

        Client relu = new CrudService<Client, Long>(Client.class).getById(client.getId());
        assertEquals("Alice", relu.getNom());
        assertEquals(LocalDate.of(2026, 1, 15), relu.getDateInscription());
    }
}
