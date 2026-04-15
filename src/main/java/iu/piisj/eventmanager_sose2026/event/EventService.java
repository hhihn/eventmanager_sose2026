package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class EventService {

    @Inject
    private EventRepository eventRepository;

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }

    public List<String> getAvailableStatuses() {
        return List.of("Geplant", "Offen", "Abgeschlossen");
    }

    public void addEvent() {
        Event newEvent = new Event("", "", "", "Geplant");
        eventRepository.save(newEvent);
    }

    public void saveEvents(List<Event> events) {
        eventRepository.saveAll(events);
    }
}
