# Übungsaufgabe: Sessions zu Veranstaltungen erfassen

In dieser Übung erweitern wir den Stand aus `6_my_orga_events` um Sessions zu Veranstaltungen.
Organisator:innen können bereits eigene Veranstaltungen sehen und die angemeldeten Teilnehmer:innen prüfen.
Jetzt sollen sie zu einer Veranstaltung mehrere Sessions erfassen können, zum Beispiel Vorträge oder Workshops.

Der Branch `7_sessions` dient als Referenz für den Zielzustand. Falls in Aufgaben oder Gesprächen von `7_session`
die Rede ist, ist damit in diesem Repository der Branch `7_sessions` gemeint. Nutzt den Zielbranch nicht als
Kopiervorlage am Anfang, sondern erst zum Abgleich, wenn ihr eure eigene Lösung umgesetzt habt.

Aufgaben, die mit **GEMEINSAM PROGRAMMIEREN** markiert sind, enthalten neue oder schwierigere Konzepte. Diese Teile
sollten gemeinsam im Kurs programmiert und besprochen werden, bevor ihr allein weiterarbeitet.

## Lernziele

Nach der Übung könnt ihr:

- eine neue fachliche Entity für abhängige Daten modellieren,
- eine One-to-Many-/Many-to-One-Beziehung zwischen `Event` und `EventSession` abbilden,
- Repository- und Service-Schicht für eine neue Fachfunktion ergänzen,
- DTOs als Formularmodell für JSF-Views nutzen,
- eine neue GET-basierte Detailseite mit `f:viewParam` und `f:viewAction` bauen,
- HTML5-DateTime-Picker über Jakarta-Faces-Passthrough-Attribute verwenden,
- Berechtigungslogik für Organisator:innen und Admins wiederverwenden,
- eine bestehende Tabellenübersicht um eine Detailaktion erweitern.

## Erwarteter Zielzustand

Nach der Umsetzung soll Folgendes funktionieren:

- Es gibt eine neue Tabelle `event_sessions`.
- Eine Veranstaltung kann mehrere Sessions haben.
- Eine einzelne Session gehört genau zu einer Veranstaltung.
- Sessions enthalten Titel, Referent:in, Typ, Raum, Start, Ende und Beschreibung.
- Organisator:innen können Sessions nur für eigene Veranstaltungen erfassen.
- Admins können Sessions für alle Veranstaltungen erfassen.
- Die Sessions einer Veranstaltung werden auf einer eigenen Seite angezeigt.
- Start und Ende werden über Date-/Time-Picker erfasst.
- Die Anwendung kompiliert und kann als WAR gebaut werden.

Diese Übung deckt damit dieses Requirement ab:

> Als Organisator:in möchte ich zu einer Veranstaltung mehrere Sessions (Vorträge/Workshops) erfassen können.

## Gesamtzeit

Wir planen für die gesamte Übung etwa **2,5 bis 3 Stunden** ein.

Die Zeiten pro Teil sind Richtwerte. Nach jedem gemeinsamen Programmierteil besprechen wir einen möglichen Lösungsweg.
Wenn ihr deutlich länger braucht, macht zuerst einen kleinen Zwischentest, bevor ihr weiterarbeitet.

## Rahmenbedingungen

- Arbeitet ausgehend vom Branch `main`.
- Erstellt einen eigenen Branch für diese Übung.
- Verändert nur Dateien, die für die Session-Erfassung notwendig sind.
- Nutzt die vorhandene Organizer-/Admin-Prüfung aus dem Stand `6_my_orga_events`.
- Speichert keine kompletten JPA-Entities dauerhaft in der Session.
- Achtet darauf, EntityManager nach Datenbankoperationen zu schliessen.
- Achtet bei JSF darauf, dass `UIInput` und `UICommand` in einem `h:form` liegen.
- Verwendet keine Variable `session` als `var` in JSF-Tabellen, weil `session` ein implizites EL-Objekt ist.

### Aufgabe

Wechselt auf `6_my_orga_events` und holt den aktuellen Stand, dann zurück zu `main`. Der Branch `7_sessions` enthält das Ziel dieser Aufgaben:
Organisator:innen und Admins können Sessions zu Veranstaltungen erfassen.

## Teil 1: Ausgangszustand analysieren

**Zeit:** 15 Minuten

Verschafft euch einen Überblick über den vorhandenen Stand nach der Organizer-Sicht.

Untersucht besonders diese Dateien:

- `event/Event.java`
- `event/EventController.java`
- `event/EventService.java`
- `event/EventParticipantsController.java`
- `repository/EventRepository.java`
- `auth/AuthController.java`
- `auth/AuthFilter.java`
- `main/webapp/events.xhtml`
- `main/webapp/event-participants.xhtml`

Beantwortet für euch:

- Wo wird geprüft, ob ein Benutzer Organisator oder Admin ist?
- Wie wird erkannt, ob ein Organisator ein bestimmtes Event verwalten darf?
- Wie wird `eventId` in `event-participants.xhtml` an den Controller übergeben?
- Warum reicht es nicht, nur einen Button in der View zu verstecken?
- Wo wäre der passende Ort, um Sessions dauerhaft zu speichern?
- Warum sollte eine Session eine eigene Entity sein und nicht nur ein String-Feld in `Event`?

## Teil 2: Datenmodell für Sessions entwerfen

**Zeit:** 15 Minuten

Bevor ihr programmiert, entwerft kurz das Datenmodell.

Die Beziehung sieht so aus:

```text
Event 1 --- n EventSession
```

Das bedeutet:

- Eine Veranstaltung kann viele Sessions haben.
- Eine Session gehört genau zu einer Veranstaltung.
- In der Tabelle `event_sessions` brauchen wir eine Spalte `event_id`.

### Aufgabe

Skizziert für euch die spätere Tabelle:

```sql
event_sessions
--------------
id
event_id
title
speaker
session_type
room
start_time
end_time
description
```

**Tipp:** Wir modellieren Vorträge und Workshops gemeinsam als `EventSession`. Der Typ unterscheidet später, ob es ein
Vortrag oder Workshop ist.

**Tipp:** Start und Ende werden in dieser Übung als String gespeichert. Das hält die Übung einfach. In einer echten
Produktiv-Anwendung wäre `LocalDateTime` mit Converter/Formatter die robustere Lösung.

## Teil 3: EventSession-Entity erstellen

**Zeit:** 35 Minuten

Legt eine neue Klasse an:

`event/EventSession.java`

### Aufgabe

Die Klasse soll:

- eine JPA-Entity sein,
- auf die Tabelle `event_sessions` gemappt werden,
- `Serializable` implementieren,
- eine automatisch erzeugte ID besitzen,
- eine Pflicht-Beziehung zu `Event` besitzen,
- Titel, Referent:in, Typ, Raum, Startzeit, Endzeit und Beschreibung speichern,
- einen leeren `protected` Konstruktor für JPA besitzen,
- einen öffentlichen Konstruktor für die Formularwerte besitzen,
- Getter für alle Felder anbieten,
- einen Setter für `event` anbieten.

Denkt an die passenden Annotationen wie `@Entity` für die Klasse und die Annotationen für die Felder
und Attribute der Klasse, wie bpsw. `@Id`.

**Tipp:** Die Beziehung zu `Event` wird nicht im Konstruktor gesetzt, sondern später im Repository. Dort wird das Event
als verwaltete JPA-Entity geladen.

## Teil 4: Event um Sessions erweitern

**Zeit:** 15 Minuten

Erweitert:

`event/Event.java`

### Aufgabe

Fügt eine One-to-Many-Beziehung zwischen `Event` und `EventSession` sowie einen passenden Getter hinzu.


**Tipp:** Wir setzen hier kein `cascade = ...`. Das Speichern neuer Sessions passiert bewusst über ein eigenes
Repository.

## Teil 5: Formular-DTO für Sessions erstellen

**Zeit:** 20 Minuten

Legt eine neue Klasse an:

`dto/EventSessionDTO.java`

### Aufgabe

Das DTO soll:

- `Serializable` implementieren,
- die Formularfelder für eine neue Session enthalten,
- Getter und Setter für alle Felder anbieten.

### Felder

```java
private String title;
private String speaker;
private String sessionType;
private String room;
private String startTime;
private String endTime;
private String description;
```

**Tipp:** Das DTO ist das Formularmodell. Die JPA-Entity `EventSession` wird erst im Service daraus erzeugt.

## Teil 6: EventSessionRepository implementieren

**Zeit:** 40 Minuten

Legt ein neues Repository an:

`repository/EventSessionRepository.java`

### Aufgabe

Das Repository soll:

- `@ApplicationScoped` sein,
- die Persistence Unit `eventmanagerPU` verwenden,
- pro Operation einen `EntityManager` erzeugen,
- Sessions zu einem Event laden können,
- eine neue Session in einer Transaktion speichern können,
- prüfen, ob das zugehörige Event existiert,
- bei Fehlern Transaktionen zurückrollen,
- den EntityManager im `finally`-Block schliessen.

### Grundstruktur

```java
@ApplicationScoped
public class EventSessionRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
```

### Methode 1: `findByEventId`

Implementiert:

```java
List<EventSession> findByEventId(Long eventId)
```

JPQL Vorlage:

```java
return em.createQuery(
        """
        SELECT s
        FROM EventSession s
        {FÜGE HIER DEINEN CODE EIN}
        ORDER BY s.startTime ASC, s.id ASC
        """,
        EventSession.class
)
.setParameter("eventId", eventId)
.getResultList();
```

Wie bekommst du es hin, dass die JPQL die gesuchte `EventSession` zurückgibt?

### Methode 2: `save`

Implementiert:

```java
EventSession save(Long eventId, EventSession session)
```

Die Methode soll:

- eine Transaktion starten,
- das Event mit `em.find(Event.class, eventId)` laden,
- bei fehlendem Event eine `IllegalArgumentException` werfen,
- `session.setEvent(event)` setzen,
- die Session per `em.persist(session)` speichern,
- committen,
- bei `RuntimeException` rollbacken und weiterwerfen,
- den EntityManager schliessen.

**Tipp:** Nutzt `setParameter(...)` und baut Werte nicht per String-Konkatenation in JPQL ein.

## Teil 7: EventSessionService implementieren

**Zeit:** 25 Minuten

Legt einen neuen Service an:

`event/EventSessionService.java`

### Aufgabe

Der Service soll:

- `@RequestScoped` sein,
- das `EventSessionRepository` injizieren,
- Sessions für ein Event laden,
- aus einem `EventSessionDTO` eine `EventSession` erzeugen,
- das Speichern an das Repository delegieren.

### Grundstruktur

```java
@RequestScoped
public class EventSessionService {

    @Inject
    private EventSessionRepository eventSessionRepository;
}
```

### Methode 1: `getSessionsForEvent`

```java
public List<EventSession> getSessionsForEvent(Long eventId) {

}
```

### Methode 2: `addSession`

```java
public EventSession addSession(Long eventId, EventSessionDTO dto) {
    
}
```

Die Methode soll:

- prüfen, ob `eventId` vorhanden ist,
- eine neue `EventSession` aus dem DTO erzeugen,
- `eventSessionRepository.save(eventId, session)` aufrufen.

**Tipp:** Der Service enthält die Übersetzung vom Formularmodell zur Entity. Der Controller muss dadurch keine
JPA-Entity selbst zusammenbauen.

## Teil 8: EventSessionsController erstellen

**Zeit:** 40 Minuten

**GEMEINSAM PROGRAMMIEREN**

Legt eine neue Klasse an:

`event/EventSessionsController.java`

### Aufgabe

Der Controller soll:

- `@Named` und `@ViewScoped` sein,
- `Serializable` implementieren,
- `AuthController`, `EventService` und `EventSessionService` injizieren,
- den Request-Parameter `eventId` aufnehmen,
- das Event laden,
- prüfen, ob der aktuelle Benutzer `ADMIN` ist oder Organisator dieses Events ist,
- bei fehlender Berechtigung zurück zu `events.xhtml` umleiten,
- die vorhandenen Sessions laden,
- eine neue Session speichern können,
- nach dem Speichern das Formular zurücksetzen,
- Getter für Event, Sessions, neues Session-DTO und verfügbare Session-Typen bereitstellen.

### Wichtige Felder

```java
private Long eventId;
private Event event;
private List<EventSession> sessions = List.of();
private EventSessionDTO newSession = new EventSessionDTO();
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
- prüfen, ob der Benutzer Sessions für dieses Event verwalten darf,
- `eventSessionService.getSessionsForEvent(eventId)` aufrufen,
- bei Fehlern eine `FacesMessage` anzeigen und `"/events.xhtml?faces-redirect=true"` zurückgeben,
- bei Erfolg `null` zurückgeben.

### Methode: `saveSession`

Implementiert:

```java
public void saveSession()
```

Die Methode soll:

- erneut prüfen, ob das Event vorhanden und der Benutzer berechtigt ist,
- `eventSessionService.addSession(eventId, newSession)` aufrufen,
- danach die Session-Liste neu laden,
- `newSession = new EventSessionDTO()` setzen,
- eine Erfolgsmeldung anzeigen.

### Methode: `canManageSessions`

Implementiert intern:

```java
private boolean canManageSessions() {

}
```

### Methode: `getAvailableSessionTypes`

```java
public List<String> getAvailableSessionTypes() {

}
```

**Tipp:** Diese serverseitige Prüfung ist wichtig. Ein versteckter Button in `events.xhtml` reicht nicht aus, weil die
URL direkt aufgerufen werden kann.

## Teil 9: Sessions-View erstellen

**Zeit:** 45 Minuten

**GEMEINSAM PROGRAMMIEREN**

Legt eine neue View an:

`main/webapp/event-sessions.xhtml`

### Aufgabe

Die View soll:

- den Parameter `eventId` per `f:viewParam` an den Controller binden,
- beim Laden `eventSessionsController.load` per `f:viewAction` ausführen,
- den Namen, Ort und Termin der Veranstaltung anzeigen,
- ein Formular zum Anlegen einer Session enthalten,
- eine Tabelle mit bestehenden Sessions anzeigen,
- bei leerer Liste einen Hinweis anzeigen,
- einen Button zurück zur Event-Übersicht anbieten.

### Metadaten

```xml
<f:metadata>
  <f:viewParam name="eventId"
               value="#{eventSessionsController.eventId}"/>
  <f:viewAction action="#{eventSessionsController.load}"/>
</f:metadata>
```

### Formular

Das Formular braucht:

- `h:form` mit ID `eventSessionsForm`,
- Inputs für `title`, `speaker`, `sessionType`, `room`, `startTime`, `endTime`, `description`,
- für jedes Input ein `h:message`,
- einen `h:commandButton` mit ID `saveSession`.

### Session-Typ

Verwendet ein Auswahlfeld:

```xml
<h:selectOneMenu id="sessionType"
                 value="#{eventSessionsController.newSession.sessionType}"
                 required="true"
                 requiredMessage="Bitte einen Session-Typ auswaehlen.">
  <f:selectItem itemLabel="Bitte auswaehlen" itemValue="#{null}" noSelectionOption="true"/>
  <f:selectItems value="#{eventSessionsController.availableSessionTypes}"/>
</h:selectOneMenu>
<h:message for="sessionType" styleClass="field-error"/>
```

### Start und Ende mit Date-/Time-Picker

Verwendet Jakarta-Faces-Passthrough-Attribute:

```xml
xmlns:pt="jakarta.faces.passthrough"
```

Für Start:

```xml
<h:inputText id="startTime"
             value="#{eventSessionsController.newSession.startTime}"
             pt:type="datetime-local"
             required="true"
             requiredMessage="Bitte eine Startzeit eingeben."/>
<h:message for="startTime" styleClass="field-error"/>
```

Für Ende:

```xml
<h:inputText id="endTime"
             value="#{eventSessionsController.newSession.endTime}"
             pt:type="datetime-local"/>
<h:message for="endTime" styleClass="field-error"/>
```

### Tabelle

Die Tabelle soll diese Spalten enthalten:

- Zeit,
- Typ,
- Titel,
- Referent:in,
- Raum.

Verwendet als Tabellenvariable nicht `session`, sondern zum Beispiel:

```xml
var="eventSession"
```

**Tipp:** `session` ist in EL bereits ein implizites Objekt für die HTTP-Session. Wenn ihr `var="session"` verwendet,
kann es zu Fehlern wie `PropertyNotFoundException: endTime` auf `StandardSessionFacade` kommen.

## Teil 10: Eventliste um Sessions-Link erweitern

**Zeit:** 20 Minuten

Erweitert:

`main/webapp/events.xhtml`

### Aufgabe

Fügt innerhalb von `h:dataTable` eine neue Spalte ein.

Die Spalte soll nur für Organisator:innen oder Admins grundsätzlich sichtbar sein:

```xml
<h:column rendered="{FÜGE HIER DEINEN CODE EIN}">
```

Der Button soll nur angezeigt werden, wenn der Benutzer die konkrete Veranstaltung verwalten darf:

```xml
<h:button {FÜGE HIER DEINEN CODE EIN}>
  <f:param name="eventId" value="#{event.id}"/>
</h:button>
```

### EventController erweitern

Ergänzt in:

`event/EventController.java`

```java
public boolean canManageSessions(Event event) {

}
```

**Tipp:** Für dieses Feature gilt dieselbe Berechtigung wie für die Teilnehmerliste: Admins dürfen alle Events
verwalten, Organisator:innen nur eigene Events.

## Teil 11: AuthFilter erweitern

**Zeit:** 10 Minuten

Erweitert:

`auth/AuthFilter.java`

### Aufgabe

Die neue Seite `event-sessions.xhtml` soll nicht öffentlich sein. Sie darf grundsätzlich nur von Organisator:innen oder
Admins aufgerufen werden.

Ergänzt die Seite im vorhandenen Set `ORGANIZER_OR_ADMIN_PAGES`.

**Tipp:** Der Filter prüft nur die grobe Rolle. Ob ein Organisator wirklich dieses Event organisiert, prüft
`EventSessionsController`.

## Teil 12: Styling für DateTime-Felder ergänzen

**Zeit:** 10 Minuten

Erweitert:

`main/webapp/resources/css/style.css`

### Aufgabe

Damit die neuen Date-/Time-Picker wie die anderen Felder aussehen, ergänzt `input[type="datetime-local"]` bei den
bestehenden Input-Regeln.

Beispiel:

```css
input[type="text"],
input[type="email"],
input[type="date"],
input[type="datetime-local"],
textarea,
select {
    width: 100%;
}
```

Falls `textarea` noch nicht in den Fokus-Regeln enthalten ist, ergänzt es dort ebenfalls.

## Teil 13: Kleine Serialisierungs-Korrektur

**Zeit:** 10 Minuten

Prüft diese Klassen:

- `event/Event.java`
- `event/EventSession.java`
- `dto/EventSessionDTO.java`
- `dto/EventParticipantDTO.java`
- `user/User.java`

### Aufgabe

Objekte, die in `@ViewScoped` Beans gehalten werden, sollten `Serializable` sein. Ergänzt deshalb bei den neuen
Session-Klassen `implements Serializable`. Wenn vorhandene Entities oder DTOs bereits in ViewScoped Beans liegen und
noch nicht serialisierbar sind, zieht das ebenfalls nach.

**Tipp:** `@ViewScoped` Beans werden in der HTTP-Session gehalten. Deshalb ist Serialisierbarkeit hier keine reine
Formsache.

### Funktionaler Test

Testet mindestens diese Fälle:

1. Als Organisator ein eigenes Event in der Eventliste öffnen.
2. Prüfen, dass der Button "Sessions bearbeiten" nur bei eigenen Events erscheint.
3. Die Sessions-Seite öffnen.
4. Eine Session vom Typ `Vortrag` mit Startzeit speichern.
5. Eine Session vom Typ `Workshop` mit Start- und Endzeit speichern.
6. Prüfen, dass beide Sessions in der Tabelle erscheinen.
7. Als anderer Organisator prüfen, dass fremde Events nicht bearbeitet werden können.
8. Als Admin prüfen, dass alle Events bearbeitet werden können.
9. Eine fremde `event-sessions.xhtml?eventId=...` URL direkt aufrufen und prüfen, dass die serverseitige Berechtigung greift.

### SQL-Kontrolle

Optional könnt ihr in der Datenbank prüfen:

```sql
select id, event_id, title, speaker, session_type, start_time, end_time
from event_sessions
order by event_id, start_time;
```

## Kontrollfragen

1. Warum ist `EventSession` eine eigene Entity?
2. Warum ist die Beziehung `EventSession -> Event` eine `@ManyToOne`-Beziehung?
3. Warum wird das Event im Repository mit `em.find(Event.class, eventId)` geladen?
4. Warum verwendet die View ein DTO und nicht direkt eine neue `EventSession`?
5. Warum braucht `EventSessionsController` eine serverseitige Berechtigungsprüfung?
6. Welche Aufgabe hat `f:viewParam`?
7. Welche Aufgabe hat `f:viewAction`?
8. Warum darf die Tabellenvariable nicht `session` heissen?
9. Warum liegt `h:commandButton` in einem `h:form`?
10. Warum hat jedes Eingabefeld ein eigenes `h:message`?
11. Was macht `pt:type="datetime-local"`?
12. Welche Erweiterung wäre nötig, um Sessions später bearbeiten oder löschen zu können?
