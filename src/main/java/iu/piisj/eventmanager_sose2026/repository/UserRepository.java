package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;

import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<User> findByUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE lower(u.username) = lower(:username)",
                            User.class
                    )
                    .setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ignored) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public Optional<User> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            User user = em.createQuery(
                            "SELECT u FROM User u WHERE lower(u.email) = lower(:email)",
                            User.class
                    )
                    .setParameter("email", email)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ignored) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public User save(User user) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User managedUser;
            if (user.getId() == null) {
                em.persist(user);
                managedUser = user;
            } else {
                managedUser = em.merge(user);
            }
            tx.commit();
            return managedUser;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}
