package iu.piisj.eventmanager_sose2026.event;

import iu.piisj.eventmanager_sose2026.dto.EventSessionDTO;
import iu.piisj.eventmanager_sose2026.repository.EventSessionRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;

import java.util.List;

@RequestScoped
public class EventSessionService {

    @Inject
    private EventSessionRepository eventSessionRepository;

    public List<EventSession> getSessionsForEvent(Long eventId) {
        if (eventId == null) {
            return List.of();
        }

        return eventSessionRepository.findByEventId(eventId);
    }

    public EventSession addSession(Long eventId, EventSessionDTO dto) {
        if (eventId == null) {
            throw new IllegalArgumentException("Veranstaltung fehlt.");
        }

        EventSession session = new EventSession(
                dto.getTitle(),
                dto.getSpeaker(),
                dto.getSessionType(),
                dto.getRoom(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getDescription()
        );

        return eventSessionRepository.save(eventId, session);
    }
}
