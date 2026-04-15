package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class EventRepository {

    private final EntityManagerFactory emf =
            jakarta.persistence.Persistence.createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
    public List<Event> findAll() {
        EntityManager em = getEntityManager();
        return em.createQuery("SELECT e FROM Event e", Event.class)
                .getResultList();
    }

    public Event findById(Long id) {
        EntityManager em = getEntityManager();
        return em.find(Event.class, id);
    }

    @Transactional
    public Event save(Event event) {
        EntityManager em = getEntityManager();
        em.persist(event);
        return event;
    }
}
