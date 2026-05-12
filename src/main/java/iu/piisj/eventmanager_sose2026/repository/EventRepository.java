package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import iu.piisj.eventmanager_sose2026.user.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

import java.util.List;

import static jakarta.persistence.Persistence.createEntityManagerFactory;

@ApplicationScoped
public class EventRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public List<Event> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Event e LEFT JOIN FETCH e.organizer",
                            Event.class
                    )
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public Event findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery(
                            "SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.id = :id",
                            Event.class
                    )
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
        }
        finally {
            em.close();
        }
    }


    public void save(Event event) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            // wenn noch keine ID vergeben wurde, dann ist das Objekt neu
            if (event.getId() == null){
                em.persist(event);
            } else {
                em.merge(event);
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

    public void save(Event event, Long organizerId) {
        EntityManager em = getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User organizer = em.find(User.class, organizerId);
            if (organizer == null) {
                throw new IllegalArgumentException("Organizer wurde nicht gefunden.");
            }

            event.setOrganizer(organizer);

            if (event.getId() == null){
                em.persist(event);
            } else {
                em.merge(event);
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
