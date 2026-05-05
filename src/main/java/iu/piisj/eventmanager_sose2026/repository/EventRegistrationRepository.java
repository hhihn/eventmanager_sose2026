package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import iu.piisj.eventmanager_sose2026.registration.EventRegistration;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationResult;
import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class EventRegistrationRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public EventRegistrationResult register(Long eventId, Long userId) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Event event = em.find(Event.class, eventId);
            if (event == null) {
                tx.rollback();
                return EventRegistrationResult.EVENT_NOT_FOUND;
            }

            User user = em.find(User.class, userId);
            if (user == null) {
                tx.rollback();
                return EventRegistrationResult.USER_NOT_FOUND;
            }

            Long existingRegistrations = em.createQuery(
                            "SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.user.id = :userId",
                            Long.class
                    )
                    .setParameter("eventId", eventId)
                    .setParameter("userId", userId)
                    .getSingleResult();

            if (existingRegistrations > 0) {
                tx.rollback();
                return EventRegistrationResult.ALREADY_REGISTERED;
            }

            em.persist(new EventRegistration(event, user));
            tx.commit();
            return EventRegistrationResult.SUCCESS;
        } catch (RuntimeException ex) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Set<Long> findEventIdsByUserId(Long userId) {
        EntityManager em = getEntityManager();
        try {
            List<Long> eventIds = em.createQuery(
                            "SELECT r.event.id FROM EventRegistration r WHERE r.user.id = :userId",
                            Long.class
                    )
                    .setParameter("userId", userId)
                    .getResultList();
            return new HashSet<>(eventIds);
        } finally {
            em.close();
        }
    }
}
