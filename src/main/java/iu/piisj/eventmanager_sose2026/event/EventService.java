package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventService {

    @Inject
    private EventRepository eventRepository;

    public List<Event> getEvents() {
        return new ArrayList<>(List.of(
                new Event("Java User Group Meetup", "Berlin", "2026-05-12", "Geplant"),
                new Event("Cloud Native Summit", "Hamburg", "2026-06-03", "Offen"),
                new Event("Jakarta EE Workshop", "Muenchen", "2026-06-25", "Abgeschlossen")
        ));
    }

    public List<String> getAvailableStatuses() {
        return List.of("Geplant", "Offen", "Abgeschlossen");
    }

    public void addEvent() {
        Event newEvent = new Event("", "", "", "Geplant");
        eventRepository.save(newEvent);
    }

}
