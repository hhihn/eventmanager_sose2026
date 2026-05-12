# Übungsaufgabe: Eigene Veranstaltungen und Teilnehmer:innen anzeigen

In dieser Übung erweitert ihr den Stand aus `5_event_registration` um eine Sicht für Organisator:innen.
Teilnehmer:innen können sich bereits für Veranstaltungen anmelden. Jetzt sollen Organisator:innen sehen können,
welche Teilnehmer:innen für ihre eigenen Veranstaltungen angemeldet sind. Benutzer:innen mit der Rolle `ADMIN`
dürfen die Anmeldungen aller Veranstaltungen einsehen.

Der Branch `6_my_orga_events` dient als Referenz für den Zielzustand. Nutzt ihn nicht als Kopiervorlage am Anfang,
sondern erst zum Abgleich, wenn ihr eure eigene Lösung umgesetzt habt. Schaut gerne in den Branch rein, wenn ihr nicht
weiterkommt und Hilfe braucht.

Aufgaben, die mit **GEMEINSAM PROGRAMMIEREN** markiert sind, enthalten neue oder schwierigere Konzepte. Diese Teile
sollten gemeinsam im Kurs programmiert und besprochen werden, bevor ihr allein weiterarbeitet.

## Lernziele

Nach der Übung könnt ihr:

- eine One-to-Many-/Many-to-One-Beziehung zwischen `User` und `Event` modellieren,
- beim Speichern einer Entity eine Beziehung zum aktuell eingeloggten Benutzer setzen,
- Rollenlogik im `AuthController` zentralisieren,
- JPA-Abfragen mit `JOIN FETCH` nutzen, wenn eine Beziehung in der View gebraucht wird,
- eine DTO-Projektion mit JPQL erstellen,
- eine neue JSF-Detailseite über `f:viewParam` und `f:viewAction` laden,
- UI-Sichtbarkeit und serverseitige Berechtigungsprüfung kombinieren,
- eine bestehende Tabelle um einen Link zu einer Detailansicht erweitern.

## Erwarteter Zielzustand

Nach der Umsetzung soll Folgendes funktionieren:

- Ein `User` kann viele Veranstaltungen organisieren.
- Eine Veranstaltung gehört zu genau einem Organisator.
- Beim Anlegen einer Veranstaltung wird der aktuell eingeloggte Organisator gespeichert.
- In `events.xhtml` erscheint ein Link zu den Anmeldungen nur, wenn:
  - der eingeloggte Benutzer `ADMIN` ist, oder
  - der eingeloggte Benutzer Organisator genau dieser Veranstaltung ist.
- Die Teilnehmerliste ist zusätzlich serverseitig geschützt.
- Die Teilnehmerliste zeigt Name, Benutzername, E-Mail und Anmeldezeitpunkt.
- Die Anwendung kompiliert und kann als WAR gebaut werden.

Diese Übung deckt damit dieses Requirement ab:

> Als Organisator:in möchte ich sehen können, welche Teilnehmer:innen für eine Veranstaltung angemeldet sind.

## Gesamtzeit

Wir planen für die gesamte Übung etwa **2,5 bis 3 Stunden** ein.

Die Zeiten pro Teil sind Richtwerte. Nach jedem gemeinsamen Programmierteil besprechen wir einen möglichen Lösungsweg.
Wenn ihr deutlich länger braucht, macht zuerst einen kleinen Zwischentest, bevor ihr weiterarbeitet.

## Rahmenbedingungen

- Arbeitet ausgehend vom Branch `5_event_registration`.
- Erstellt einen eigenen Branch für diese Übung.
- Verändert nur Dateien, die für die Organizer-Sicht notwendig sind.
- Gebt keine vollständigen `User`-Entities an die Teilnehmerlisten-View weiter, wenn nur einzelne Felder gebraucht werden.
- Speichert keine kompletten JPA-Entities dauerhaft in der Session.
- Nutzt den vorhandenen `AuthController`, `SessionUser`, `Event`, `User`, `UserRole` und `EventRegistration`.
- Lasst Login, Registrierung, Event-Anlage und Event-Anmeldung unverändert funktionieren.
- Achtet darauf, EntityManager nach Datenbankoperationen zu schliessen.

### Aufgabe

Wechselt auf `5_event_registration` und holt den aktuellen Stand. Der Branch `6_my_orga_events` enthält das Ziel dieser
Aufgaben: Organisator:innen und Admins können Anmeldungen zu Veranstaltungen einsehen.

## Teil 1: Ausgangszustand analysieren

**Zeit:** 15 Minuten

Verschafft euch einen Überblick über den vorhandenen Stand nach der Event-Anmeldung.

Untersucht besonders diese Dateien:

- `main/java/iu/piisj/eventmanager_sose2026/event/Event.java`
- `main/java/iu/piisj/eventmanager_sose2026/user/User.java`
- `main/java/iu/piisj/eventmanager_sose2026/event/EventController.java`
- `main/java/iu/piisj/eventmanager_sose2026/event/EventService.java`
- `main/java/iu/piisj/eventmanager_sose2026/repository/EventRepository.java`
- `main/java/iu/piisj/eventmanager_sose2026/registration/EventRegistration.java`
- `main/java/iu/piisj/eventmanager_sose2026/registration/EventRegistrationService.java`
- `main/java/iu/piisj/eventmanager_sose2026/repository/EventRegistrationRepository.java`
- `main/java/iu/piisj/eventmanager_sose2026/auth/AuthController.java`
- `main/java/iu/piisj/eventmanager_sose2026/auth/AuthFilter.java`
- `main/webapp/events.xhtml`

Beantwortet für euch:

- Wo werden Veranstaltungen geladen?
- Wo wird eine Veranstaltung gespeichert?
- Wo liegen die Anmeldungen zu Veranstaltungen?
- Welche Daten enthält `SessionUser`?
- Wo wird aktuell geprüft, ob jemand Teilnehmer:in oder Organisator:in ist?
- Warum reicht die bestehende `EventRegistration`-Tabelle aus, um Teilnehmer:innen pro Event zu finden?
- Welche Information fehlt noch am `Event`, damit wir "meine organisierten Veranstaltungen" erkennen können?

## Teil 2: Datenmodell für organisierte Veranstaltungen entwerfen

**Zeit:** 15 Minuten

Bevor ihr programmiert, entwerft kurz die Beziehung zwischen Benutzer und Veranstaltung.

Die Beziehung sieht so aus:

```text
User 1 --- n Event
```

Das bedeutet:

- Ein Benutzer kann viele Veranstaltungen organisieren.
- Eine Veranstaltung wird von genau einem Benutzer organisiert.
- In der Tabelle `events` brauchen wir eine Spalte `organizer_id`.

### Aufgabe

Skizziert für euch die spätere Erweiterung:

```sql
events
------
id
name
location
date
state
organizer_id
```

**Tipp:** Die fachliche Beziehung heisst "organisiert". Deshalb nennen wir das Feld im `Event` nicht einfach `user`,
sondern `organizer`.

**Tipp:** Für bestehende lokale Testdaten kann `organizer_id` zunächst leer sein. Neu angelegte Events sollen aber immer
einen Organisator bekommen.

## Teil 3: User-Event-Beziehung modellieren

**Zeit:** 35 Minuten

**GEMEINSAM PROGRAMMIEREN**

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/user/User.java`

und:

`main/java/iu/piisj/eventmanager_sose2026/event/Event.java`

### Aufgabe

In `User` soll eine Liste organisierter Veranstaltungen ergänzt werden:

```java
@OneToMany(mappedBy = "organizer")
private List<Event> organizedEvents = new ArrayList<>();
```

Ergänzt ausserdem einen Getter:

```java
public List<Event> getOrganizedEvents() {
    return organizedEvents;
}
```

In `Event` soll der Organisator ergänzt werden:

```java
@ManyToOne
@JoinColumn(name = "organizer_id")
private User organizer;
```

Ergänzt Getter und Setter:

```java
public User getOrganizer() {
    return organizer;
}

public void setOrganizer(User organizer) {
    this.organizer = organizer;
}
```

### Benötigte Imports

In `User.java` braucht ihr unter anderem:

```java
import iu.piisj.eventmanager_sose2026.event.Event;
import java.util.ArrayList;
import java.util.List;
```

In `Event.java` braucht ihr:

```java
import iu.piisj.eventmanager_sose2026.user.User;
```

**Tipp:** Wir setzen hier kein `cascade = ...`. Das Speichern oder Löschen eines Benutzers soll nicht automatisch Events
speichern oder löschen.

## Teil 4: Rollenlogik im AuthController zentralisieren

**Zeit:** 15 Minuten

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/auth/AuthController.java`

### Aufgabe

Der Controller soll zentrale Hilfsmethoden für Rollen anbieten.

Fügt den Import hinzu:

```java
import iu.piisj.eventmanager_sose2026.user.UserRole;
```

Implementiert:

```java
public boolean hasRole(UserRole role) {
    return currentUser != null && currentUser.getRole() == role;
}

public boolean isOrganizer() {
    return hasRole(UserRole.ORGANISATOR);
}

public boolean isAdmin() {
    return hasRole(UserRole.ADMIN);
}

public boolean isParticipant() {
    return hasRole(UserRole.TEILNEHMER);
}
```

**Tipp:** Dadurch muss `EventController` nicht überall selbst `currentUser.getRole()` auswerten.

## Teil 5: Events mit Organisator speichern und laden

**Zeit:** 35 Minuten

**GEMEINSAM PROGRAMMIEREN**

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/repository/EventRepository.java`

und:

`main/java/iu/piisj/eventmanager_sose2026/event/EventService.java`

### Aufgabe 1: Events mit Organisator laden

Wenn die View später `event.organizer.id` prüft, muss der Organisator geladen sein. Passt deshalb die Abfragen an.

In `findAll()`:

```java
return em.createQuery(
        "SELECT e FROM Event e LEFT JOIN FETCH e.organizer",
        Event.class
)
.getResultList();
```

In `findById(Long id)`:

```java
return em.createQuery(
        "SELECT e FROM Event e LEFT JOIN FETCH e.organizer WHERE e.id = :id",
        Event.class
)
.setParameter("id", id)
.getResultStream()
.findFirst()
.orElse(null);
```

### Aufgabe 2: Events mit Organisator speichern

Ergänzt in `EventRepository` eine Methode:

```java
public void save(Event event, Long organizerId)
```

Die Methode soll:

- einen `EntityManager` öffnen,
- eine Transaktion starten,
- den Organisator mit `em.find(User.class, organizerId)` laden,
- bei fehlendem Organisator eine `IllegalArgumentException` werfen,
- `event.setOrganizer(organizer)` setzen,
- neue Events per `em.persist(event)` speichern,
- bestehende Events per `em.merge(event)` speichern,
- committen,
- bei Fehlern rollbacken,
- den EntityManager im `finally`-Block schliessen.

### Aufgabe 3: EventService anpassen

Ändert in `EventService` die Methode zum Speichern:

```java
public void saveEvent(Event newEvent, SessionUser organizer)
```

Die Methode soll:

- prüfen, ob `organizer` vorhanden ist,
- `eventRepository.save(newEvent, organizer.getId())` aufrufen,
- die bestehende Erfolgsmeldung weiter anzeigen.

**Tipp:** Das Repository braucht das echte `User`-Entity, weil die Beziehung in JPA auf eine Entity zeigt. Der Service
bekommt trotzdem nur `SessionUser`, weil die Session kein vollständiges Entity speichern soll.

## Teil 6: EventController für Organisator- und Admin-Sicht erweitern

**Zeit:** 25 Minuten

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/event/EventController.java`

### Aufgabe

Passt das Speichern neuer Events an:

```java
eventService.saveEvent(eventEntity, authController.getCurrentUser());
```

Vereinfacht die Rollenmethoden:

```java
public boolean isParticipant() {
    return authController.isParticipant();
}

public boolean isOrganizer() {
    return authController.isOrganizer();
}

public boolean isAdmin() {
    return authController.isAdmin();
}
```

Ergänzt ausserdem:

```java
public boolean canViewParticipants(Event event) {
    if (authController.isAdmin()) {
        return true;
    }

    return authController.isOrganizer()
            && event != null
            && event.getOrganizer() != null
            && event.getOrganizer().getId().equals(authController.getCurrentUser().getId());
}
```

**Tipp:** Diese Methode ist für die Anzeige in `events.xhtml`. Sie ersetzt aber keine serverseitige Prüfung auf der
Detailseite.

## Teil 7: DTO für Teilnehmerliste erstellen

**Zeit:** 20 Minuten

Legt eine neue Klasse an:

`main/java/iu/piisj/eventmanager_sose2026/dto/EventParticipantDTO.java`

### Aufgabe

Die Klasse soll nur die Daten enthalten, die in der Teilnehmerliste angezeigt werden:

- Benutzername,
- E-Mail,
- Vorname,
- Nachname,
- Anmeldezeitpunkt.

### Grundstruktur

```java
public class EventParticipantDTO {

    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final LocalDateTime registeredAt;

    public EventParticipantDTO(String username, String email, String firstName, String lastName, LocalDateTime registeredAt) {
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registeredAt = registeredAt;
    }

    // Getter für alle Felder
}
```

**Tipp:** Wir geben bewusst kein vollständiges `User`-Entity an die View. Dadurch landen zum Beispiel keine
Passwort-Hashes versehentlich in der Oberfläche.

## Teil 8: Teilnehmer:innen pro Event laden

**Zeit:** 35 Minuten

**GEMEINSAM PROGRAMMIEREN**

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/repository/EventRegistrationRepository.java`

und:

`main/java/iu/piisj/eventmanager_sose2026/registration/EventRegistrationService.java`

### Aufgabe 1: Repository-Methode ergänzen

Implementiert:

```java
List<EventParticipantDTO> findParticipantsByEventId(Long eventId)
```

Die Methode soll:

- einen `EntityManager` öffnen,
- per JPQL alle Anmeldungen zu einem Event laden,
- über `JOIN r.user u` die Benutzerdaten lesen,
- direkt ein `EventParticipantDTO` erzeugen,
- nach `registeredAt` aufsteigend sortieren,
- den EntityManager im `finally`-Block schliessen.

### JPQL

```java
return em.createQuery(
        """
        SELECT new iu.piisj.eventmanager_sose2026.dto.EventParticipantDTO(
            u.username,
            u.email,
            u.firstName,
            u.lastName,
            r.registeredAt
        )
        FROM EventRegistration r
        JOIN r.user u
        WHERE r.event.id = :eventId
        ORDER BY r.registeredAt ASC
        """,
        EventParticipantDTO.class
)
.setParameter("eventId", eventId)
.getResultList();
```

### Aufgabe 2: Service-Methode ergänzen

Implementiert in `EventRegistrationService`:

```java
public List<EventParticipantDTO> getParticipantsForEvent(Long eventId) {
    if (eventId == null) {
        return List.of();
    }

    return eventRegistrationRepository.findParticipantsByEventId(eventId);
}
```

**Tipp:** Die Rollen- und Besitzerprüfung machen wir später im Controller der Detailseite. Der Service bleibt hier
einfach und delegiert die fachliche Datenabfrage.

## Teil 9: Controller für Teilnehmerliste erstellen

**Zeit:** 35 Minuten

**GEMEINSAM PROGRAMMIEREN**

Legt eine neue Klasse an:

`main/java/iu/piisj/eventmanager_sose2026/event/EventParticipantsController.java`

### Aufgabe

Der Controller soll:

- `@Named` und `@ViewScoped` sein,
- `Serializable` implementieren,
- `AuthController`, `EventService` und `EventRegistrationService` injizieren,
- den Request-Parameter `eventId` aufnehmen,
- das Event laden,
- prüfen, ob der aktuelle Benutzer `ADMIN` ist oder Organisator dieses Events ist,
- bei fehlender Berechtigung zurück zu `events.xhtml` umleiten,
- die Teilnehmerliste laden,
- Getter für Event und Teilnehmerliste bereitstellen.

### Wichtige Felder

```java
private Long eventId;
private Event event;
private List<EventParticipantDTO> participants = List.of();
```

### Methode: `load`

Implementiert:

```java
public String load()
```

Die Methode soll:

- prüfen, ob der aktuelle Benutzer Organisator oder Admin ist,
- prüfen, ob `eventId` vorhanden ist,
- `eventService.getEventById(eventId)` aufrufen,
- prüfen, ob das Event existiert,
- prüfen, ob der Benutzer die Teilnehmerliste sehen darf,
- `eventRegistrationService.getParticipantsForEvent(eventId)` aufrufen,
- bei Fehlern eine `FacesMessage` anzeigen und `"/events.xhtml?faces-redirect=true"` zurückgeben,
- bei Erfolg `null` zurückgeben.

### Methode: `canViewParticipants`

Implementiert intern:

```java
private boolean canViewParticipants() {
    if (authController.isAdmin()) {
        return true;
    }

    return event.getOrganizer() != null
            && event.getOrganizer().getId().equals(authController.getCurrentUser().getId());
}
```

### Methode: `hasParticipants`

```java
public boolean hasParticipants() {
    return !participants.isEmpty();
}
```

**Tipp:** Diese serverseitige Prüfung ist wichtig. Ein versteckter Link in der Tabelle reicht nicht aus, weil Benutzer
die URL auch direkt aufrufen könnten.

## Teil 10: Teilnehmerlisten-View erstellen

**Zeit:** 30 Minuten

**GEMEINSAM PROGRAMMIEREN**

Legt eine neue View an:

`main/webapp/event-participants.xhtml`

### Aufgabe

Die View soll:

- den Parameter `eventId` per `f:viewParam` an den Controller binden,
- beim Laden `eventParticipantsController.load` per `f:viewAction` ausführen,
- den Namen, Ort und Termin der Veranstaltung anzeigen,
- eine Tabelle mit Teilnehmer:innen anzeigen,
- bei leerer Liste einen Hinweis anzeigen,
- einen Button zurück zur Event-Übersicht anbieten.

### Grundstruktur

```xml
<f:metadata>
  <f:viewParam name="eventId"
               value="#{eventParticipantsController.eventId}"/>
  <f:viewAction action="#{eventParticipantsController.load}"/>
</f:metadata>
```

### Tabelle

Die Tabelle soll diese Spalten enthalten:

- Name,
- Benutzername,
- E-Mail,
- Angemeldet am.

Für das Datum könnt ihr verwenden:

```xml
<h:outputText value="#{participant.registeredAt}">
  <f:convertDateTime type="localDateTime"
                     pattern="dd.MM.yyyy HH:mm"/>
</h:outputText>
```

**Tipp:** `f:viewAction` ist für Initialisierungslogik bei GET-Seiten geeignet. Dadurch kann die Seite direkt per URL
mit `eventId` geöffnet werden.

## Teil 11: Link in events.xhtml ergänzen

**Zeit:** 20 Minuten

Erweitert:

`main/webapp/events.xhtml`

### Aufgabe

Fügt innerhalb von `h:dataTable` eine neue Spalte ein.

Die Spalte soll nur für Organisator:innen oder Admins grundsätzlich sichtbar sein:

```xml
<h:column rendered="#{eventController.organizer or eventController.admin}">
```

Der eigentliche Button soll nur angezeigt werden, wenn der Benutzer die konkrete Veranstaltung sehen darf:

```xml
<h:button id="showParticipants"
          value="Anzeigen"
          outcome="event-participants"
          rendered="#{eventController.canViewParticipants(event)}">
  <f:param name="eventId" value="#{event.id}"/>
</h:button>
```

### Komplette Spalte

```xml
<h:column rendered="#{eventController.organizer or eventController.admin}">
  <f:facet name="header">Teilnehmer:innen</f:facet>
  <h:button id="showParticipants"
            value="Anzeigen"
            outcome="event-participants"
            rendered="#{eventController.canViewParticipants(event)}">
    <f:param name="eventId" value="#{event.id}"/>
  </h:button>
</h:column>
```

**Tipp:** `h:button` ist hier passend, weil nur zu einer Detailseite navigiert wird. Es wird keine fachliche Aktion per
POST ausgeführt.

## Teil 12: AuthFilter erweitern

**Zeit:** 15 Minuten

Erweitert:

`main/java/iu/piisj/eventmanager_sose2026/auth/AuthFilter.java`

### Aufgabe

Die neue Seite `event-participants.xhtml` soll nicht öffentlich sein. Sie darf grundsätzlich nur von Organisator:innen
oder Admins aufgerufen werden.

Ergänzt ein neues Set:

```java
private static final Set<String> ORGANIZER_OR_ADMIN_PAGES = Set.of(
        "/event-participants.xhtml"
);
```

Ergänzt in `doFilter(...)` nach der Organizer-Prüfung:

```java
if (isOrganizerOrAdminOnly(path) && !isOrganizerOrAdmin(authUser)) {
    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

Ergänzt Hilfsmethoden:

```java
private boolean isOrganizerOrAdminOnly(String path) {
    return ORGANIZER_OR_ADMIN_PAGES.contains(path);
}

private boolean isOrganizerOrAdmin(Object authUser) {
    return authUser instanceof SessionUser sessionUser
            && (sessionUser.getRole() == UserRole.ORGANISATOR || sessionUser.getRole() == UserRole.ADMIN);
}
```

**Tipp:** Der Filter prüft nur die grobe Rolle. Ob ein Organisator wirklich dieses Event organisiert, prüft
`EventParticipantsController`.

## Teil 13: Testen

**Zeit:** 20 Minuten

### Build-Test

Führt aus:

```bash
./mvnw test
./mvnw package
```

Beide Befehle sollen erfolgreich durchlaufen.

### Funktionaler Test

Testet mindestens diese Fälle:

1. Als Organisator ein neues Event anlegen.
2. Prüfen, dass das neue Event einen Organisator bekommt.
3. Als Teilnehmer:in für dieses Event anmelden.
4. Als Organisator die Eventliste öffnen.
5. Prüfen, dass der Button "Anzeigen" nur bei eigenen Events erscheint.
6. Die Teilnehmerliste öffnen.
7. Prüfen, dass der angemeldete Teilnehmer angezeigt wird.
8. Als anderer Organisator prüfen, dass der Button für fremde Events nicht erscheint.
9. Als Admin prüfen, dass die Teilnehmerlisten aller Events erreichbar sind.
10. Eine fremde Teilnehmerlisten-URL direkt aufrufen und prüfen, dass die serverseitige Berechtigung greift.

### SQL-Kontrolle

Optional könnt ihr in der Datenbank prüfen:

```sql
select id, name, organizer_id from events;
```

und:

```sql
select r.event_id, u.username, r.registered_at
from event_registrations r
join users u on u.id = r.user_id
order by r.event_id, r.registered_at;
```

## Kontrollfragen

1. Warum ist die Beziehung zwischen `User` und `Event` eine One-to-Many-/Many-to-One-Beziehung?
2. Warum speichern wir im `Event` ein Feld `organizer` und nicht nur `organizerId`?
3. Warum wird beim Speichern eines Events der Organisator im Repository nochmal als Entity geladen?
4. Warum verwenden wir `LEFT JOIN FETCH e.organizer` beim Laden der Events?
5. Warum soll die Teilnehmerliste ein DTO verwenden und nicht direkt `User`-Entities anzeigen?
6. Was macht `SELECT new ...` in JPQL?
7. Warum reicht es nicht, den Button in `events.xhtml` nur per `rendered` zu verstecken?
8. Welche Aufgabe hat `f:viewParam`?
9. Welche Aufgabe hat `f:viewAction`?
10. Warum darf der Filter nur grob nach Rolle prüfen, während der Controller das konkrete Event prüfen muss?
11. Was passiert mit bestehenden Events, die noch keinen `organizer_id` haben?
12. Welche Erweiterung wäre nötig, um zusätzlich die Anzahl der Anmeldungen direkt in `events.xhtml` anzuzeigen?
