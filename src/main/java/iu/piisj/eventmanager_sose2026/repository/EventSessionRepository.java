package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import iu.piisj.eventmanager_sose2026.event.EventSession;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class EventSessionRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<EventSession> findByEventId(Long eventId) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            """
                            SELECT s
                            FROM EventSession s
                            WHERE s.event.id = :eventId
                            ORDER BY s.startTime ASC, s.id ASC
                            """,
                            EventSession.class
                    )
                    .setParameter("eventId", eventId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public EventSession save(Long eventId, EventSession session) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Event event = em.find(Event.class, eventId);
            if (event == null) {
                throw new IllegalArgumentException("Veranstaltung wurde nicht gefunden.");
            }

            session.setEvent(event);
            em.persist(session);
            tx.commit();
            return session;
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
