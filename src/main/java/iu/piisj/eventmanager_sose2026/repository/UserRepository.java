package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

@ApplicationScoped
public class UserRepository {

    private volatile EntityManagerFactory entityManagerFactory;

    private EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory != null) {
            return entityManagerFactory;
        }

        synchronized (this) {
            if (entityManagerFactory != null) {
                return entityManagerFactory;
            }
            entityManagerFactory = Persistence.createEntityManagerFactory("eventmanagerPU");
            return entityManagerFactory;
        }
    }

    public void save(User user) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            em.close();
        }
    }

    public boolean emailExists(String email) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(u) FROM User u WHERE u.email = :email",
                            Long.class
                    ).setParameter("email", email)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }

    public User findByUsername(String username) {
        EntityManager em = getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username",
                            User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}
