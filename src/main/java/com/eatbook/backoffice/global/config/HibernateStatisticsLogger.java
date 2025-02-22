package com.eatbook.backoffice.global.config;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class HibernateStatisticsLogger {

    private final SessionFactory sessionFactory;

    @Autowired
    public HibernateStatisticsLogger(EntityManagerFactory entityManagerFactory) {
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
    }

    public void logStatistics() {
        Statistics stats = sessionFactory.getStatistics();
        System.out.println("Hibernate Statistics:");
        System.out.println("Query count: " + stats.getQueryExecutionCount());
        System.out.println("Query execution time (ms): " + stats.getQueryExecutionMaxTime());
        System.out.println("Entity load count: " + stats.getEntityLoadCount());
        System.out.println("Entity fetch count: " + stats.getEntityFetchCount());
        System.out.println("Collection fetch count: " + stats.getCollectionFetchCount());
    }
}
