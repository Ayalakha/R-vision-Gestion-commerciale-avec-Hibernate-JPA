package org.example.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Accès à la SessionFactory Hibernate native (lit hibernate.cfg.xml).
 * Initialisation paresseuse (idiome "holder") : la connexion n'est ouverte qu'au premier usage,
 * et shutdown() ne la crée pas inutilement si elle n'a jamais servi.
 */
public final class HibernateUtil {
    private static volatile SessionFactory sessionFactory;

    private HibernateUtil() {}

    private static SessionFactory buildSessionFactory() {
        try {
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateUtil.class) {
                if (sessionFactory == null) {
                    sessionFactory = buildSessionFactory();
                }
            }
        }
        return sessionFactory;
    }

    public static synchronized void shutdown() {
        if (sessionFactory != null && sessionFactory.isOpen()) {
            sessionFactory.close();
        }
    }
}
