package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class EventService {

    // Dependency Inject: wenn ein Objekt der Klasse EventService instanziert wird, wird ein neues Object der Klasse
    // EventRepository erstellt und hier eingesetzt ("injection")
    // Die injizierte Klasse braucht einen leeren Standardkonstruktor, muss also stateless sein.
    @Inject
    private EventRepository eventRepository;

    public List<Event> getEvents(){
        return new ArrayList<>(List.of(
                new Event("Java User Group Meetup", "Berlin", "09.09.2026", "Geplant"),
                new Event("Java User Group Meetup", "Hamburg", "09.09.2026", "Geplant"),
                new Event("Java User Group Meetup", "München", "09.09.2026", "Geplant")
        ));
    }

    public List<String> getAvailableStatuses(){
        return List.of("Geplant", "Abgeschlossen", "Offen");
    }

}
