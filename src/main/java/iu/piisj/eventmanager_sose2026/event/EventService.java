package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.repository.EventRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
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
        return eventRepository.findAll();
    }

    public List<String> getAvailableStatuses(){
        return List.of("Geplant", "Abgeschlossen", "Offen");
    }

    public void saveEvent(Event newEvent) {
        eventRepository.save(newEvent);
        FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "Erfolg: Die Veranstaltung wurde angelegt.",
                newEvent.toString()
        );

        FacesContext.getCurrentInstance().addMessage(null, message);

    }
}
