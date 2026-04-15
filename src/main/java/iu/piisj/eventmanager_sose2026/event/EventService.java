package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import static java.util.Collections.emptyList;

@RequestScoped
public class EventService {

    public EventService() {}

    @Inject
    private EventRepository eventRepository;

    public List<Event> getEvents() {
        return emptyList();// return eventRepository.findAll();
    }

    public List<String> getAvailableStatuses() {
        return List.of("Geplant", "Offen", "Abgeschlossen");
    }

    public void addEvent() {
        Event newEvent = new Event("", "", "", "Geplant");
        // eventRepository.save(newEvent);
    }


}
