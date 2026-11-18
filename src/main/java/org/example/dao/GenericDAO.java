package org.example.dao;

import org.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.io.Serializable;
import java.util.List;
import java.util.function.Function;

/**
 * DAO générique basé uniquement sur JPA.
 * Chaque méthode = une unité de travail : un EntityManager ouvert, une transaction pour les écritures,
 * rollback en cas d'erreur, et fermeture systématique dans finally.
 */
public class GenericDAO<T, ID extends Serializable> {
    private final Class<T> entityClass;

    public GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public T find(ID id) {
        return read(em -> em.find(entityClass, id));
    }

    public List<T> findAll() {
        // Nom d'entité issu de la classe (pas d'une saisie utilisateur) : pas de risque d'injection
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
        return read(em -> em.createQuery(jpql, entityClass).getResultList());
    }

    public List<T> findPage(int pageNumber, int pageSize) {
        if (pageNumber < 1 || pageSize < 1) {
            throw new IllegalArgumentException("pageNumber et pageSize doivent être >= 1");
        }
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e ORDER BY e.id";
        return read(em -> em.createQuery(jpql, entityClass)
                .setFirstResult((pageNumber - 1) * pageSize)
                .setMaxResults(pageSize)
                .getResultList());
    }

    public long count() {
        String jpql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
        return read(em -> em.createQuery(jpql, Long.class).getSingleResult());
    }

    public void create(T entity) {
        write(em -> { em.persist(entity); return null; });
    }

    public T update(T entity) {
        return write(em -> em.merge(entity));
    }

    public void delete(T entity) {
        write(em -> {
            T attached = em.contains(entity) ? entity : em.merge(entity);
            em.remove(attached);
            return null;
        });
    }

    public void deleteById(ID id) {
        write(em -> {
            T attached = em.find(entityClass, id);
            if (attached != null) em.remove(attached);
            return null;
        });
    }

    /** Exécute une lecture dans un EntityManager dédié (sans transaction). */
    protected <R> R read(Function<EntityManager, R> work) {
        EntityManager em = JpaUtil.createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }

    /** Exécute une écriture dans une transaction ; rollback si une exception survient. */
    protected <R> R write(Function<EntityManager, R> work) {
        EntityManager em = JpaUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = work.apply(em);
            tx.commit();
            return result;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Class<T> getEntityClass() { return entityClass; }
}
