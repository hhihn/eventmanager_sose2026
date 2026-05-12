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
public class EventSessionsController implements Serializable {

    @Inject
    private AuthController authController;

    @Inject
    private EventService eventService;

    @Inject
    private EventSessionService eventSessionService;

    private Long eventId;

    private Event event;

    private List<EventSession> sessions = List.of();

    private EventSessionDTO newSession = new EventSessionDTO();

    public String load() {
        if (!authController.isOrganizer() && !authController.isAdmin()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt", "Nur Organisator:innen koennen Sessions erfassen.");
            return "/events.xhtml?faces-redirect=true";
        }

        if (eventId == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung fehlt", "Es wurde keine Veranstaltung ausgewaehlt.");
            return "/events.xhtml?faces-redirect=true";
        }

        event = eventService.getEventById(eventId);
        if (event == null) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Veranstaltung nicht gefunden", "Die Veranstaltung wurde nicht gefunden.");
            return "/events.xhtml?faces-redirect=true";
        }

        if (!canManageSessions()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt", "Du kannst nur Sessions fuer eigene Veranstaltungen erfassen.");
            return "/events.xhtml?faces-redirect=true";
        }

        sessions = eventSessionService.getSessionsForEvent(eventId);
        return null;
    }

    public void saveSession() {
        if (event == null || !canManageSessions()) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Nicht erlaubt", "Du kannst fuer diese Veranstaltung keine Sessions erfassen.");
            return;
        }

        eventSessionService.addSession(eventId, newSession);
        sessions = eventSessionService.getSessionsForEvent(eventId);
        newSession = new EventSessionDTO();
        addMessage(FacesMessage.SEVERITY_INFO, "Session gespeichert", "Die Session wurde zur Veranstaltung hinzugefuegt.");
    }

    public boolean hasSessions() {
        return !sessions.isEmpty();
    }

    private boolean canManageSessions() {
        if (authController.isAdmin()) {
            return true;
        }

        return event.getOrganizer() != null
                && event.getOrganizer().getId().equals(authController.getCurrentUser().getId());
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Event getEvent() {
        return event;
    }

    public List<EventSession> getSessions() {
        return sessions;
    }

    public EventSessionDTO getNewSession() {
        return newSession;
    }

    public List<String> getAvailableSessionTypes() {
        return List.of("Vortrag", "Workshop");
    }
}
