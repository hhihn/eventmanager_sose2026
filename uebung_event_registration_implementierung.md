# Übungsaufgabe: Event-Anmeldung implementieren

In dieser Übung erweitert ihr den aktuellen Stand aus `main` um die Anmeldung von Teilnehmer:innen zu Veranstaltungen.
Der Login-Mechanismus ist bereits vorhanden: Benutzer:innen können sich registrieren, anmelden und haben eine Rolle.
Am Ende sollen eingeloggte Teilnehmer:innen sich für Veranstaltungen anmelden können. Bereits angemeldete
Veranstaltungen sollen in der Übersicht als "Angemeldet" markiert werden.

Der Branch `5_event_registration` dient als Referenz für den Zielzustand. Nutzt ihn nicht als Kopiervorlage am Anfang,
sondern erst zum Abgleich, wenn ihr eure eigene Lösung umgesetzt habt. Schaut gerne in den Branch rein, wenn ihr nicht
weiterkommt und Hilfe braucht.

Aufgaben, die mit **LIVE-CODING EMPFOHLEN** markiert sind, enthalten viel neues Material. Diese Teile sollten gemeinsam
im Kurs programmiert und besprochen werden, bevor ihr allein weiterarbeitet.

## Lernziele

Nach der Übung könnt ihr:

- eine Many-to-One-Beziehung zwischen JPA-Entities modellieren,
- eine Zwischentabelle als eigene Entity abbilden,
- doppelte Anmeldungen über eine Datenbank-Constraint und Repository-Logik verhindern,
- fachliche Ergebniszustände mit einem Enum ausdrücken,
- die aktuelle Login-Session für fachliche Aktionen verwenden,
- JSF-Views abhängig von Rolle und Anmeldestatus darstellen,
- eine bestehende View um eine zeilenbezogene Aktion erweitern.

## Erwarteter Zielzustand

Nach der Umsetzung soll Folgendes funktionieren:

- Es gibt eine neue Tabelle `event_registrations`.
- Jede Event-Anmeldung verbindet genau einen Benutzer mit genau einer Veranstaltung.
- Eine Teilnehmer:in kann sich für eine Veranstaltung anmelden.
- Eine Teilnehmer:in kann sich nicht doppelt für dieselbe Veranstaltung anmelden.
- Bereits angemeldete Veranstaltungen werden in `events.xhtml` als "Angemeldet" angezeigt.
- Nicht angemeldete Veranstaltungen zeigen für Teilnehmer:innen einen Button "Anmelden".
- Benutzer:innen mit anderen Rollen als `TEILNEHMER` können sich nicht für Veranstaltungen anmelden.
- Die Anwendung kompiliert und kann als WAR gebaut werden.

Diese Übung deckt damit dieses MUST-Requirement ab:

> Als Teilnehmer:in möchte ich mich für eine Veranstaltung registrieren können.

Sie schafft ausserdem die Grundlage für spätere Requirements:

- Organisator:in sieht angemeldete Teilnehmer:innen pro Veranstaltung.
- Organisator:in sieht Anzahl der Anmeldungen pro Veranstaltung.

## Gesamtzeit

Wir planen für die gesamte Übung etwa **2,5 bis 3 Stunden** ein.

Die Zeiten pro Teil sind Richtwerte. Nach jedem Live-Coding-Teil besprechen wir gemeinsam einen möglichen Lösungsweg.
Wenn ihr deutlich länger braucht, macht zuerst einen kleinen Zwischentest, bevor ihr weiterarbeitet.

## Rahmenbedingungen

- Arbeitet ausgehend vom Branch `main`.
- Erstellt einen eigenen Branch für diese Übung.
- Verändert nur Dateien, die für die Event-Anmeldung notwendig sind.
- Speichert keine kompletten JPA-Entities dauerhaft in der Session.
- Nutzt den vorhandenen `AuthController`, `SessionUser`, `Event`, `User` und `UserRole`.
- Lasst bestehende Login-, Registrierungs- und Event-Anlegefunktionen unverändert funktionieren.
- Achtet darauf, EntityManager nach Datenbankoperationen zu schliessen.

### Aufgabe

Wechselt auf `main` und holt den aktuellen Stand. Der Branch `5_event_registration` enthält das Ziel dieser Aufgaben:
Teilnehmer:innen können sich für Events registrieren.

## Teil 1: Ausgangszustand analysieren

**Zeit:** 15 Minuten

Verschafft euch einen Überblick über den vorhandenen Event- und Login-Stand.

Untersucht besonders diese Dateien:

- `event/Event.java`
- `event/EventController.java`
- `event/EventService.java`
- `repository/EventRepository.java`
- `auth/AuthController.java`
- `auth/SessionUser.java`
- `user/User.java`
- `user/UserRole.java`
- `webapp/events.xhtml`

Beantwortet für euch:

- Wo werden Veranstaltungen aktuell geladen?
- Wo wird eine neue Veranstaltung gespeichert?
- Wie kommt `events.xhtml` an die Liste der Veranstaltungen?
- Wo liegt der aktuell eingeloggte Benutzer?
- Welche Rolle bekommt ein neu registrierter Benutzer?
- Warum reicht die bestehende Benutzer-Registrierung nicht für eine Event-Anmeldung?

**Tipp:** Benutzer-Registrierung und Event-Anmeldung sind fachlich zwei verschiedene Dinge. Die Registrierung legt ein
Benutzerkonto an. Die Event-Anmeldung verbindet ein bestehendes Benutzerkonto mit einer Veranstaltung.

## Teil 2: Modell für Event-Anmeldungen entwerfen

**Zeit:** 15 Minuten

Bevor ihr programmiert, entwerft kurz das Datenmodell.

Eine Event-Anmeldung braucht mindestens:

- eine eindeutige ID,
- eine Veranstaltung,
- einen Benutzer,
- einen Zeitpunkt der Anmeldung.

Die Beziehung sieht so aus:

```text
User 1 --- n EventRegistration n --- 1 Event
```

Das bedeutet:

- Ein Benutzer kann viele Event-Anmeldungen haben.
- Ein Event kann viele Event-Anmeldungen haben.
- Eine einzelne Event-Anmeldung gehört genau zu einem Benutzer und genau zu einem Event.

### Aufgabe

Skizziert für euch die spätere Tabelle:

```sql
event_registrations
-------------------
id
event_id
user_id
registered_at
```

Zusätzlich soll es eine Eindeutigkeitsregel geben:

```text
Ein Benutzer darf pro Event nur eine Anmeldung haben.
```

Technisch bedeutet das:

```sql
unique(event_id, user_id)
```

**Tipp:** Wir modellieren die Anmeldung als eigene Entity und nicht als einfache Liste in `Event`, weil wir später
weitere Informationen an der Anmeldung speichern können, zum Beispiel Zeitpunkt, Status oder Stornierung.

## Teil 3: EventRegistration-Entity erstellen

**Zeit:** 35 Minuten

Diesen Teil programmieren wir gemeinsam.

Legt dazu ein neues Package und eine neue Klasse an:

`/registration/EventRegistration.java`

### Aufgabe

Die Klasse soll:

- eine JPA-Entity sein,
- auf die Tabelle `event_registrations` gemappt werden,
- eine automatisch erzeugte ID besitzen,
- eine Pflicht-Beziehung zu `Event` besitzen,
- eine Pflicht-Beziehung zu `User` besitzen,
- den Zeitpunkt der Anmeldung speichern,
- doppelte Kombinationen aus `event_id` und `user_id` per Unique Constraint verhindern,
- einen leeren `protected` Konstruktor für JPA besitzen,
- einen öffentlichen Konstruktor für `Event` und `User` besitzen,
- Getter für alle Felder anbieten.

### Vorgegebene Annotationen

Ihr braucht unter anderem:

```java
@Entity
@Table(
        name = "event_registrations",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_registration_event_user",
                columnNames = {"event_id", "user_id"}
        )
)
```

### Vorgegebene Felder

```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@ManyToOne(optional = false)
@JoinColumn(name = "event_id", nullable = false)
private Event event;

@ManyToOne(optional = false)
@JoinColumn(name = "user_id", nullable = false)
private User user;

@Column(name = "registered_at", nullable = false)
private LocalDateTime registeredAt;
```

### Konstruktoren

JPA braucht einen leeren Konstruktor:

```java
protected EventRegistration() {
}
```

Für euren Anwendungscode braucht ihr einen Konstruktor:

```java
public EventRegistration(Event event, User user) {
    this.event = event;
    this.user = user;
    this.registeredAt = LocalDateTime.now();
}
```

**Tipp:** `@ManyToOne` ist hier passend, weil viele Anmeldungen auf dasselbe Event zeigen können und viele Anmeldungen
auf denselben Benutzer zeigen können.

**Tipp:** `registeredAt` wird im Konstruktor gesetzt. Dadurch muss der Controller keinen Zeitpunkt kennen.

## Teil 4: Ergebnis-Enum erstellen

**Zeit:** 10 Minuten

Legt eine neue Enum-Klasse an:

`/registration/EventRegistrationResult.java`

### Aufgabe

Das Enum soll alle fachlichen Ergebnisse einer Event-Anmeldung ausdrücken.

Verwendet diese Werte:

```java
public enum EventRegistrationResult {
    SUCCESS,
    ALREADY_REGISTERED,
    EVENT_NOT_FOUND,
    USER_NOT_FOUND,
    NOT_LOGGED_IN,
    NOT_PARTICIPANT
}
```

### Warum ein Enum?

Das Repository oder der Service soll nicht direkt JSF-Meldungen erzeugen. Stattdessen gibt die fachliche Logik ein
Ergebnis zurück. Der Controller entscheidet später, welche Meldung in der Oberfläche angezeigt wird.

**Tipp:** Dieses Muster kennt ihr bereits aus `RegistrationResult`.

## Teil 5: EventRegistrationRepository implementieren

**Zeit:** 45 Minuten

Legt ein neues Repository an:

`/repository/EventRegistrationRepository.java`

### Aufgabe

Das Repository soll:

- `@ApplicationScoped` sein,
- die Persistence Unit `eventmanagerPU` verwenden,
- pro Operation einen `EntityManager` erzeugen,
- Event-Anmeldungen in einer Transaktion speichern,
- prüfen, ob das Event existiert,
- prüfen, ob der User existiert,
- prüfen, ob bereits eine Anmeldung für dieselbe Kombination aus Event und User existiert,
- bei Erfolg eine neue `EventRegistration` speichern,
- bei Fehlern Transaktionen zurückrollen,
- Event-IDs laden können, für die ein Benutzer bereits angemeldet ist.

### Grundstruktur

```java
@ApplicationScoped
public class EventRegistrationRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
```

### Methode 1: `register`

Implementiert:

```java
EventRegistrationResult register(Long eventId, Long userId)
```

Die Methode soll:

- einen `EntityManager` öffnen,
- eine `EntityTransaction` holen,
- die Transaktion starten,
- das Event mit `em.find(Event.class, eventId)` laden,
- bei fehlendem Event `EVENT_NOT_FOUND` zurückgeben,
- den User mit `em.find(User.class, userId)` laden,
- bei fehlendem User `USER_NOT_FOUND` zurückgeben,
- per JPQL zählen, ob es schon eine Anmeldung gibt,
- bei vorhandener Anmeldung `ALREADY_REGISTERED` zurückgeben,
- bei neuer Anmeldung `em.persist(new EventRegistration(event, user))` ausführen,
- die Transaktion committen,
- `SUCCESS` zurückgeben,
- bei `RuntimeException` rollbacken und die Exception weiterwerfen,
- den EntityManager im `finally`-Block schliessen.

### JPQL für die Duplikatprüfung

```java
Long existingRegistrations = em.createQuery(
        "SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId AND r.user.id = :userId",
        Long.class
)
.setParameter("eventId", eventId)
.setParameter("userId", userId)
.getSingleResult();
```

Danach:

```java
if (existingRegistrations > 0) {
    tx.rollback();
    return EventRegistrationResult.ALREADY_REGISTERED;
}
```

### Methode 2: `findEventIdsByUserId`

Implementiert:

```java
Set<Long> findEventIdsByUserId(Long userId)
```

Die Methode soll:

- alle Event-IDs laden, für die der Benutzer angemeldet ist,
- die Liste in ein `Set<Long>` umwandeln,
- den EntityManager schliessen.

JPQL:

```java
List<Long> eventIds = em.createQuery(
        "SELECT r.event.id FROM EventRegistration r WHERE r.user.id = :userId",
        Long.class
)
.setParameter("userId", userId)
.getResultList();
```

Rückgabe:

```java
return new HashSet<>(eventIds);
```

**Tipp:** Wir prüfen doppelte Anmeldungen sowohl fachlich im Repository als auch technisch über den Unique Constraint
in der Entity. Das ist absichtlich doppelt: Die Anwendung kann eine verständliche Meldung anzeigen, und die Datenbank
bleibt zusätzlich geschützt.

**Tipp:** Nutzt `setParameter(...)` und baut Werte nicht per String-Konkatenation in JPQL ein.

## Teil 6: EventRegistrationService implementieren

**Zeit:** 25 Minuten

Legt einen neuen Service an:

`/registration/EventRegistrationService.java`

### Aufgabe

Der Service soll:

- `@RequestScoped` sein,
- das `EventRegistrationRepository` injizieren,
- prüfen, ob ein Benutzer eingeloggt ist,
- prüfen, ob die Rolle `TEILNEHMER` ist,
- die Anmeldung an das Repository delegieren,
- angemeldete Event-IDs für den aktuellen Benutzer laden.

### Grundstruktur

```java
@RequestScoped
public class EventRegistrationService {

    @Inject
    private EventRegistrationRepository eventRegistrationRepository;
}
```

### Methode 1: `registerForEvent`

Implementiert:

```java
EventRegistrationResult registerForEvent(Long eventId, SessionUser currentUser)
```

Die Methode soll:

- bei `currentUser == null` `NOT_LOGGED_IN` zurückgeben,
- bei anderer Rolle als `UserRole.TEILNEHMER` `NOT_PARTICIPANT` zurückgeben,
- sonst `eventRegistrationRepository.register(eventId, currentUser.getId())` aufrufen.

### Methode 2: `getRegisteredEventIds`

Implementiert:

```java
Set<Long> getRegisteredEventIds(SessionUser currentUser)
```

Die Methode soll:

- bei `currentUser == null` ein leeres Set zurückgeben,
- sonst `eventRegistrationRepository.findEventIdsByUserId(currentUser.getId())` aufrufen.

**Tipp:** Der Service bekommt bewusst nur ein `SessionUser`-Objekt. Dieses enthält die nötigen Informationen aus der
Session, aber nicht das komplette `User`-Entity.

## Teil 7: EventController erweitern

**Zeit:** 35 Minuten

Diese Aufgabe programmieren wir gemeinsam.

Erweitert:

`/event/EventController.java`

Der Controller soll die Event-Anmeldung aus der View entgegennehmen und den Anmeldestatus für die Tabelle bereitstellen.

### Aufgabe

Der `EventController` soll zusätzlich:

- den `EventRegistrationService` injizieren,
- den `AuthController` injizieren,
- eine Menge von bereits angemeldeten Event-IDs speichern,
- diese Menge in `@PostConstruct` laden,
- eine Methode für den Button "Anmelden" bereitstellen,
- eine Methode für die Prüfung "kann sich anmelden" bereitstellen,
- eine Methode für die Prüfung "ist bereits angemeldet" bereitstellen,
- eine Methode für die Prüfung "ist Teilnehmer:in" bereitstellen,
- passende `FacesMessage`-Meldungen anzeigen.

### Neue Imports

Ihr braucht unter anderem:

```java
import iu.piisj.eventmanager_sose2026.auth.AuthController;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationResult;
import iu.piisj.eventmanager_sose2026.registration.EventRegistrationService;
import iu.piisj.eventmanager_sose2026.user.UserRole;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.util.HashSet;
import java.util.Set;
```

### Neue Felder

```java
@Inject
private EventRegistrationService eventRegistrationService;

@Inject
private AuthController authController;

private Set<Long> registeredEventIds = new HashSet<>();
```

### `init()` erweitern

Die bestehende Methode lädt bereits die Events:

```java
events = eventService.getEvents();
```

Ergänzt danach:

```java
registeredEventIds = eventRegistrationService.getRegisteredEventIds(authController.getCurrentUser());
```

### Methode: `registerForEvent`

Implementiert:

```java
void registerForEvent(Event event)
```

Die Methode soll:

- prüfen, ob `event` und `event.getId()` vorhanden sind,
- `eventRegistrationService.registerForEvent(event.getId(), authController.getCurrentUser())` aufrufen,
- bei `SUCCESS` die Event-ID zu `registeredEventIds` hinzufügen,
- bei `ALREADY_REGISTERED` die Event-ID ebenfalls zu `registeredEventIds` hinzufügen,
- für jeden Ergebnisfall eine passende `FacesMessage` anzeigen.

Erwartete Fälle:

```java
SUCCESS
ALREADY_REGISTERED
NOT_PARTICIPANT
NOT_LOGGED_IN
EVENT_NOT_FOUND
USER_NOT_FOUND
```

### Methode: `canRegister`

Implementiert:

```java
boolean canRegister(Event event)
```

Die Methode soll `true` liefern, wenn:

- der aktuelle Benutzer Teilnehmer:in ist,
- und noch nicht für das Event angemeldet ist.

### Methode: `isRegistered`

Implementiert:

```java
boolean isRegistered(Event event)
```

Die Methode soll prüfen, ob die ID des Events in `registeredEventIds` enthalten ist.

### Methode: `isParticipant`

Implementiert:

```java
boolean isParticipant()
```

Die Methode soll prüfen:

```java
authController.isLoggedIn()
authController.getCurrentUser() != null
authController.getCurrentUser().getRole() == UserRole.TEILNEHMER
```

### Hilfsmethode für Meldungen

Optional, aber empfohlen:

```java
private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
}
```

**Tipp:** `EventController` ist bereits `@ViewScoped`. Dadurch bleibt `registeredEventIds` während der aktuellen
Events-Seite erhalten und kann nach erfolgreicher Anmeldung sofort aktualisiert werden.

**Tipp:** Nutzt keine Datenbankabfrage in jeder Tabellenzeile. Ladet die angemeldeten Event-IDs einmal und prüft dann
im Set. Das ist einfacher und vermeidet unnötige Datenbankzugriffe.

## Teil 8: events.xhtml um Anmeldespalte erweitern

**Zeit:** 25 Minuten

Erweitert:

`src/main/webapp/events.xhtml`

### Aufgabe

Die Tabelle soll eine neue Spalte "Anmeldung" bekommen.

Für Teilnehmer:innen:

- Wenn noch keine Anmeldung existiert, wird ein Button "Anmelden" angezeigt.
- Wenn schon eine Anmeldung existiert, wird "Angemeldet" angezeigt.

Für andere Rollen:

- In der Spalte wird `-` angezeigt.

### Vorgegebene Spalte

Fügt innerhalb von `h:dataTable` eine neue `h:column` ein:

```xml
<h:column>
  <f:facet name="header">Anmeldung</f:facet>
  <h:panelGroup rendered="#{eventController.participant}">
    <h:form id="registrationForm" rendered="#{eventController.canRegister(event)}">
      <h:commandButton id="registerEvent"
                       value="Anmelden"
                       action="#{eventController.registerForEvent(event)}"/>
    </h:form>
    <h:outputText value="Angemeldet"
                  rendered="#{eventController.isRegistered(event)}"/>
  </h:panelGroup>
  <h:outputText value="-"
                rendered="#{not eventController.participant}"/>
</h:column>
```

### Wichtige JSF-Ausdrücke

```text
#{eventController.participant}
#{eventController.canRegister(event)}
#{eventController.registerForEvent(event)}
#{eventController.isRegistered(event)}
```

**Tipp:** In JSF kann `#{eventController.participant}` auf die Java-Methode `isParticipant()` zugreifen.

**Tipp:** Der Button steht in einem eigenen `h:form`, weil `h:commandButton` ein Formular braucht.

## Kontrollfragen

1. Warum ist `EventRegistration` eine eigene Entity und keine Liste von Benutzern in `Event`?
2. Warum braucht `EventRegistration` zwei `@ManyToOne`-Beziehungen?
3. Was verhindert der Unique Constraint auf `event_id` und `user_id`?
4. Warum prüft das Repository doppelte Anmeldungen zusätzlich per JPQL?
5. Warum bekommt der Service ein `SessionUser` und kein vollständiges `User`-Entity?
6. Warum soll nur die Rolle `TEILNEHMER` eine Event-Anmeldung durchführen können?
7. Warum lädt der Controller die angemeldeten Event-IDs in ein `Set<Long>`?
8. Warum gehört die JSF-Meldung in den Controller und nicht in das Repository?
9. Welche SQL-Abfrage zeigt euch die Anzahl der Anmeldungen pro Event?
10. Welche Erweiterung wäre nötig, damit Organisator:innen alle angemeldeten Teilnehmer:innen eines Events sehen können?
