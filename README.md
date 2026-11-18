# Gestion commerciale — Hibernate 5.6 / JPA 2.2

## Lancer
- **Tests** (H2 en mémoire, aucune base requise) : `mvn test`
- **WAR** : `mvn package` → `target/gestion-commerciale.war`, à déployer sur WildFly / Payara / Tomcat 9
- **Démo console sur MySQL** : adapter user/mot de passe dans `src/main/resources/META-INF/persistence.xml`
  (et `hibernate.cfg.xml`), puis lancer `org.example.App` depuis l'IDE. La base `gestion_commerciale` est créée si absente.
- **Rapport des ventes** : `GET /gestion-commerciale/rapport-ventes` (mois précédent) ou `?mois=2026-10`

## Organisation
| Couche | Classes |
|---|---|
| Config | `persistence.xml` (JPA, `defaultPU`), `hibernate.cfg.xml` + `HibernateUtil` (API native), `JpaUtil` (EMF unique) |
| Entités | `Categorie` ⇄ `Produit` (1:N bidirectionnel), `User` → `Client`/`Fournisseur` (JOINED), `Commande` ⇄ `CommandeProduit` ⇄ `Produit` (N:N, clé composée `@EmbeddedId` + `@MapsId`) |
| DAO | `GenericDAO<T, ID>`, `ProduitDAO` (Named Queries, JOIN FETCH, Criteria), `CommandeDAO` |
| Services | `CrudService<T, ID>`, `ProduitService`, `CommandeService`, `RapportVentesService` (exercice de synthèse) |
| Web | `RapportVentesServlet`, `PersistenceLifecycleListener` (fermeture des fabriques) |

## Corrections par rapport à l'énoncé
1. `&` non échappé dans l'URL JDBC de `hibernate.cfg.xml` → XML invalide (`&amp;`).
2. `GenericDAO` : import `EntityTransaction` manquant ; `Persistence.createEntityManagerFactory("defaultPU")` exige un `persistence.xml` (ajouté).
3. `new GenericDAO<>(Categorie.class)` affecté à `GenericDAO<Categorie>` : il faut deux paramètres de type (`<Categorie, Long>`).
4. Méthode Criteria `search` : l'`EntityManager` n'était jamais fermé.
5. Test `fetched.getCategorie().getNom()` : `LazyInitializationException` (catégorie LAZY, EM fermé) → `ProduitService.getWithCategorie` avec `JOIN FETCH`.
6. Persistance d'une commande avec des produits chargés auparavant : `detached entity passed to persist` à cause de `@MapsId` → `CommandeDAO` réattache les produits via `em.getReference`.
7. `Client` et `Fournisseur` ajoutés aux mappings (seul `User` était déclaré).
8. Hibernate 5.6 sur JDK 21+ : Byte Buddy plus récent forcé dans le `pom.xml` pour les proxys LAZY.
