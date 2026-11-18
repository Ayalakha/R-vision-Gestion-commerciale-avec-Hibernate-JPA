package org.example.util;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * EntityManagerFactory unique (coûteuse à créer) pour toute l'application.
 * Les propriétés système "javax.persistence.*" et "hibernate.*" surchargent persistence.xml :
 * c'est ce qui permet aux tests de tourner sur H2 en mémoire (voir le pom.xml, plugin surefire).
 */
public final class JpaUtil {
    public static final String PERSISTENCE_UNIT = "defaultPU";
    private static volatile EntityManagerFactory emf;

    private JpaUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JpaUtil.class) {
                if (emf == null) {
                    emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, overrides());
                }
            }
        }
        return emf;
    }

    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    private static Map<String, String> overrides() {
        Map<String, String> props = new HashMap<>();
        System.getProperties().stringPropertyNames().stream()
                .filter(k -> k.startsWith("javax.persistence.") || k.startsWith("hibernate."))
                .forEach(k -> props.put(k, System.getProperty(k)));
        return props;
    }
}
