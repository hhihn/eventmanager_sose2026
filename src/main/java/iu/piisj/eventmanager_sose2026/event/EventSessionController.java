package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.auth.AuthController;
import iu.piisj.eventmanager_sose2026.dto.EventSessionDTO;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class EventSessionController implements Serializable {

    @Inject
    private EventController eventController;

    @Inject
    private EventService eventService;

    @Inject
    private EventSessionService eventSessionService;

    private Long eventId;

    private Event event;

    private List<EventSession> sessions = List.of();

    private EventSessionDTO newSession = new EventSessionDTO();

    public String load(){
        if (eventId == null){
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung fehlt.",
                    "Es wurde keine Veranstaltung angegeben.");
            return "/events.xhtml/?faces-redirect=true";
        }

        if (!canManageSessions()){
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt.",
                    "Nur Organisator or Admin darf Session zu einem Event hinzufügen.");
            return "/events.xhtml/?faces-redirect=true";
        }

        event = eventService.getEventById(eventId);
        if (event == null){
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung nicht gefunden.",
                    "Die Veranstaltung konnte nicht gefunden werden.");
        }

        sessions = eventSessionService.getSessionsForEvent(eventId);
        return null;
    }

    public void saveSession(){
        if (event == null || !canManageSessions()){
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt.",
                    "Nur Organisator or Admin darf Session zu einem Event hinzufügen.");
            return;
        }

        eventSessionService.addSession(eventId, newSession);
        sessions = eventSessionService.getSessionsForEvent(eventId);

        newSession = new EventSessionDTO();
        addMessage(FacesMessage.SEVERITY_INFO, "Session gespeichert.",
                "Die Session wurde erfolgreich zur Veranstaltung hinzugefügt.");
    }

    public boolean canManageSessions(){
        return eventController.canEditEvent(eventId);
    }

    public boolean hasSession() {return !sessions.isEmpty();}

    public void addMessage(FacesMessage.Severity severity, String summary, String detail){
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public List<String> getAvailableSessionTypes(){return List.of("Vortrag", "Workshop");}

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public List<EventSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<EventSession> sessions) {
        this.sessions = sessions;
    }

    public EventSessionDTO getNewSession() {
        return newSession;
    }

    public void setNewSession(EventSessionDTO newSession) {
        this.newSession = newSession;
    }
}
