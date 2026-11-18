package org.example.web;

import org.example.util.HibernateUtil;
import org.example.util.JpaUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/** Libère les fabriques JPA/Hibernate (et leurs connexions) à l'arrêt de l'application. */
@WebListener
public class PersistenceLifecycleListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JpaUtil.close();
        HibernateUtil.shutdown();
    }
}
