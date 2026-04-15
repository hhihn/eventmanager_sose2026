package iu.piisj.eventmanager_sose2026.repository;

import iu.piisj.eventmanager_sose2026.event.Event;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class EventRepository {

    @PersistenceContext(unitName = "eventmanagerPU")
    private EntityManager em;

    public List<Event> findAll() {
        return em.createQuery("SELECT e FROM Event e", Event.class)
                .getResultList();
    }

    public Event findById(Long id) {
        return em.find(Event.class, id);
    }

    @Transactional
    public Event save(Event event) {
        if (event.getId() == null) {
            em.persist(event);
            return event;
        } else {
            return em.merge(event);
        }
    }
}