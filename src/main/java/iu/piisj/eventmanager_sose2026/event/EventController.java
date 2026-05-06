package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.auth.AuthController;
import iu.piisj.eventmanager_sose2026.dto.EventDTO;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

// diese Annotation macht den Bean (Controller) in der View sichtbar. Dies passiert über die beans.xml dessen
// Einstellungen
@Named
@ViewScoped
public class EventController implements Serializable {

    @Inject
    private EventService eventService;

    @Inject
    private AuthController authController;

    private List<Event> events;

    private EventDTO newEvent = new EventDTO();

    @PostConstruct
    public void init() {
        events = eventService.getEvents();
    }

    private Event mapDTOToEvent(EventDTO dto) {
        return new Event(
                dto.getName(),
                dto.getLocation(),
                dto.getDate(),
                dto.getState()
        );
    }

    public void saveEvent() {

        if (authController.isOrganizerOrAdmin()) {
            Event eventEntity = mapDTOToEvent(newEvent);
            eventService.saveEvent(eventEntity);
            // Formular zurücksetzen, bzw. die EventDTO zurücksetzen
            newEvent = new EventDTO();
        } else {
            addMessage(FacesMessage.SEVERITY_ERROR,
                    "Nicht erlaubt",
                    "Nur Organistor:innen oder Admins dürfen Events anlegen");
        }
    }

    public void addMessage(FacesMessage.Severity severity, String summary, String detail){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public EventDTO getNewEvent() {return newEvent;}

    public List<Event> getEvents() {
        return events;
    }

    public List<String> getAvailableStatuses() {
        return eventService.getAvailableStatuses();
    }
}
