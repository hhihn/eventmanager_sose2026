package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.Optional;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class UserRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<User> findByUsername(String username){
        EntityManager em = getEntityManager();
        try {
            User user = em.createQuery("SELECT u FROM User u WHERE lower(u.username) = lower(:username)",
                    User.class
                            ).setParameter("username", username)
                    .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ignored) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            // wenn noch keine ID vergeben wurde, dann ist das Objekt neu
            if (user.getId() == null){
                em.persist(user);
            } else {
                em.merge(user);
            }
            tx.commit();
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
