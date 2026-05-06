# Konzepte im Eventmanager-Repository

Diese Datei erklärt die wichtigsten Konzepte, die im Eventmanager-Projekt umgesetzt sind. Sie richtet sich an Studierende, die Jakarta Faces, CDI, JPA/Hibernate, Persistenz, Entities, SQL-nahe Abfragen und typische Java-EE-Webanwendungsarchitektur lernen möchten.

Das Repository zeigt eine klassische Jakarta-Webanwendung:

```text
XHTML View
  -> JSF Controller
  -> Service
  -> Repository
  -> JPA EntityManager
  -> H2 Datenbank
```

Konkrete Beispiele findet ihr unter anderem in:

- `src/main/webapp/*.xhtml`
- `src/main/java/iu/piisj/eventmanager_sose2026/event/*`
- `src/main/java/iu/piisj/eventmanager_sose2026/auth/*`
- `src/main/java/iu/piisj/eventmanager_sose2026/repository/*`
- `src/main/java/iu/piisj/eventmanager_sose2026/user/*`
- `src/main/resources/META-INF/persistence.xml`

## 1. Technologiestack

Das Projekt ist eine Maven-WAR-Anwendung. Das erkennt man in `pom.xml`:

```xml
<packaging>war</packaging>
```

Eine WAR-Datei wird typischerweise in einem Servlet-Container oder Application Server ausgeführt. In diesem Projekt werden unter anderem diese Technologien verwendet:

- Jakarta Faces / JSF für serverseitige Weboberflächen
- CDI für Dependency Injection und Bean-Lifecycle
- JPA mit Hibernate für Persistenz
- H2 als dateibasierte Entwicklungsdatenbank
- BCrypt für Passwort-Hashing
- Servlet Filter für Zugriffsschutz

Wichtige Dependencies aus `pom.xml`:

```xml
<dependency>
    <groupId>org.glassfish</groupId>
    <artifactId>jakarta.faces</artifactId>
    <version>4.0.5</version>
</dependency>

<dependency>
    <groupId>org.jboss.weld.servlet</groupId>
    <artifactId>weld-servlet-shaded</artifactId>
    <version>5.1.2.Final</version>
</dependency>

<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-core</artifactId>
    <version>6.4.4.Final</version>
</dependency>

<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <version>2.2.224</version>
    <scope>runtime</scope>
</dependency>
```

`jakarta.jakartaee-api` hat den Scope `provided`. Das bedeutet: Die API wird zum Kompilieren gebraucht, aber zur Laufzeit stellt normalerweise der Server die Klassen bereit. Andere Bibliotheken wie Mojarra, Weld, Hibernate und H2 werden hier explizit mitgebracht.

## 2. Schichtenarchitektur

Das Projekt trennt Verantwortlichkeiten in mehrere Schichten.

### View

Die Views liegen in `src/main/webapp`, zum Beispiel:

- `events.xhtml`
- `create-event.xhtml`
- `login.xhtml`
- `register.xhtml`

Sie beschreiben die Benutzeroberfläche mit JSF-Komponenten:

```xml
<h:inputText id="name"
             value="#{eventController.newEvent.name}"
             required="true"/>

<h:commandButton value="Speichern"
                 action="#{eventController.saveEvent}"/>
```

Die View bindet Eingabefelder direkt an Properties eines Controllers. `#{eventController.newEvent.name}` bedeutet: JSF sucht eine Bean namens `eventController`, ruft `getNewEvent()` auf und greift dann auf `getName()` oder `setName(...)` des DTOs zu.

### Controller

Controller nehmen Daten aus der View entgegen und steuern die Benutzerinteraktion.

Beispiele:

- `EventController`
- `LoginController`
- `RegistrationController`
- `AuthController`

In `EventController` sieht man das Muster:

```java
@Named
@ViewScoped
public class EventController implements Serializable {

    @Inject
    private EventService eventService;

    private EventDTO newEvent = new EventDTO();

    public void saveEvent(){
        Event eventEntity = mapDTOToEvent(newEvent);
        eventService.saveEvent(eventEntity);
        newEvent = new EventDTO();
    }
}
```

Der Controller kennt die View-Daten und ruft den Service auf. Er spricht nicht direkt mit der Datenbank.

### Service

Services enthalten fachliche Logik.

Beispiel `AuthService`:

```java
@RequestScoped
public class AuthService {

    @Inject
    private UserRepository userRepository;

    public Optional<SessionUser> authenticate(String username, String plainPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
            return Optional.empty();
        }

        return Optional.of(new SessionUser(user.getId(), user.getUsername(), user.getRole()));
    }
}
```

Der Service entscheidet fachlich, ob ein Login erfolgreich ist. Er erzeugt keine HTML-Ausgabe und kennt keine Datenbankdetails.

### Repository

Repositories kapseln den Datenbankzugriff.

Beispiel `UserRepository`:

```java
@ApplicationScoped
public class UserRepository {

    private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public Optional<User> findByUsername(String username){
        EntityManager em = getEntityManager();
        try {
            User user = em.createQuery(
                    "SELECT u FROM User u WHERE lower(u.username) = lower(:username)",
                    User.class
            ).setParameter("username", username)
             .getSingleResult();
            return Optional.of(user);
        } catch (NoResultException ignored) {
            return Optional.empty();
        } finally {
            em.close();
        }
    }
}
```

Das Repository enthält JPA-Code, Query-Logik, EntityManager-Verwendung und Transaktionen.

### Entity

Entities beschreiben Datenbanktabellen als Java-Klassen.

Beispiele:

- `Event`
- `User`

```java
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String date;
    private String state;
}
```

Die Entity ist das Objektmodell, das JPA auf Tabellen und Spalten abbildet.

## 3. Scopes

Scopes bestimmen, wie lange eine CDI-Bean lebt. Das ist wichtig, weil Webanwendungen viele Benutzer, viele Requests und viele Views gleichzeitig haben können.

### `@ViewScoped`

Verwendet in:

- `EventController`
- `LoginController`
- `RegistrationController`

Beispiel:

```java
@Named
@ViewScoped
public class EventController implements Serializable {
    ...
}
```

Ein `@ViewScoped` Controller lebt so lange, wie dieselbe JSF-View aktiv ist. Das ist passend für Formularseiten, bei denen Daten über mehrere Postbacks hinweg erhalten bleiben sollen.

Wichtig: `@ViewScoped` Beans müssen `Serializable` implementieren. Deshalb steht in den Controllern:

```java
public class EventController implements Serializable
```

Typischer Einsatz:

- Formulardaten halten
- Select-Listen bereitstellen
- Buttons/Aktionen der aktuellen Seite behandeln

### `@SessionScoped`

Verwendet in:

- `AuthController`

```java
@Named
@SessionScoped
public class AuthController implements Serializable {
    private SessionUser currentUser;
}
```

Eine `@SessionScoped` Bean lebt über mehrere Requests und Views hinweg, solange die HTTP-Session aktiv ist. Das passt für Login-Zustand.

Im Projekt speichert `AuthController` den aktuellen Benutzer:

```java
private SessionUser currentUser;
```

Beim Login:

```java
currentUser = sessionUser.get();
FacesContext.getCurrentInstance()
        .getExternalContext()
        .getSessionMap()
        .put(SESSION_USER_KEY, currentUser);
```

Beim Logout:

```java
externalContext.invalidateSession();
currentUser = null;
```

Das ist ein gutes Beispiel dafür, dass Login-Daten nicht in einem Request- oder View-Scope liegen dürfen. Sonst wären sie nach dem nächsten Seitenwechsel weg.

### `@RequestScoped`

Verwendet in:

- `AuthService`

```java
@RequestScoped
public class AuthService {
    ...
}
```

Eine `@RequestScoped` Bean lebt nur für einen einzelnen HTTP-Request. Das ist passend für zustandsarme fachliche Logik, bei der keine Daten zwischen Seitenaufrufen gespeichert werden müssen.

`AuthService` muss nicht wissen, wer in der Session liegt. Er bekommt Benutzernamen und Passwort übergeben, prüft diese Daten und liefert ein Ergebnis zurück.

### `@ApplicationScoped`

Verwendet in:

- `EventService`
- `EventRepository`
- `UserRepository`

```java
@ApplicationScoped
public class EventRepository {
    ...
}
```

Eine `@ApplicationScoped` Bean existiert einmal pro Anwendung. Das ist sinnvoll für zustandsarme Services oder Repositories, die keine benutzerspezifischen Daten speichern.

Wichtig: Ein `EntityManager` selbst ist nicht threadsicher und sollte nicht als Feld einer `@ApplicationScoped` Bean dauerhaft gehalten werden. Das Projekt macht hier das richtige Grundmuster: Es hält eine `EntityManagerFactory` und erzeugt pro Methode einen frischen `EntityManager`.

```java
private EntityManager getEntityManager() {
    return emf.createEntityManager();
}
```

## 4. Wichtige Annotationen

### `@Named`

`@Named` macht eine CDI-Bean in JSF unter einem Namen verfügbar.

```java
@Named
@ViewScoped
public class LoginController implements Serializable {
    ...
}
```

Aus `LoginController` wird in der XHTML-Seite `loginController`:

```xml
<h:inputText id="username"
             value="#{loginController.username}"
             required="true"/>
```

JSF verwendet dabei JavaBean-Konventionen:

- `#{loginController.username}` liest über `getUsername()`
- beim Absenden schreibt JSF über `setUsername(...)`
- `#{loginController.submitLogin}` ruft `submitLogin()` auf

### `@Inject`

`@Inject` ist Dependency Injection. Die Klasse erstellt ihre Abhängigkeit nicht selbst mit `new`, sondern bekommt sie vom CDI-Container.

Beispiel aus `EventController`:

```java
@Inject
private EventService eventService;
```

Beispiel aus `EventService`:

```java
@Inject
private EventRepository eventRepository;
```

Dadurch entsteht eine Kette:

```text
EventController
  -> EventService
  -> EventRepository
  -> EntityManager
```

Vorteil: Jede Klasse hat eine klarere Aufgabe. Der Controller muss nicht wissen, wie das Repository gebaut wird. Der Service muss nicht wissen, wer ihn aufruft.

### `@PostConstruct`

`@PostConstruct` markiert eine Methode, die nach dem Erzeugen der Bean und nach dem Injizieren der Abhängigkeiten ausgeführt wird.

Beispiel aus `EventController`:

```java
@PostConstruct
public void init() {
    events = eventService.getEvents();
}
```

Wichtig: Der Konstruktor ist zu früh, um auf injizierte Felder zuzugreifen. `eventService` ist im Konstruktor noch nicht zuverlässig verfügbar. In `@PostConstruct` ist die Injection abgeschlossen.

### JPA-Annotationen: `@Entity`, `@Table`, `@Id`

Beispiel aus `User`:

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Long id;
}
```

Bedeutung:

- `@Entity`: Diese Klasse wird von JPA verwaltet.
- `@Table(name = "users")`: Die Klasse wird auf die Tabelle `users` gemappt.
- `@Id`: Dieses Feld ist der Primärschlüssel.
- `@GeneratedValue`: Die Datenbank oder JPA erzeugt den Schlüsselwert automatisch.

### `@Column`

`@Column` beschreibt Spalteneigenschaften.

```java
@Column(nullable = false, unique = true, length = 50)
private String username;
```

Das bedeutet:

- `nullable = false`: Die Spalte darf nicht `NULL` sein.
- `unique = true`: Der Wert muss eindeutig sein.
- `length = 50`: Die maximale Länge wird im Schema berücksichtigt.

Diese Regeln sind nicht nur Dokumentation. Hibernate kann daraus Datenbankschema erzeugen oder aktualisieren.

### `@Enumerated(EnumType.STRING)`

In `User` wird eine Rolle gespeichert:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 30)
private UserRole role;
```

Das Enum:

```java
public enum UserRole {
    ADMIN,
    ORGANISATOR,
    TEILNEHMER,
}
```

`EnumType.STRING` ist eine gute Entscheidung. Die Datenbank speichert dann zum Beispiel `TEILNEHMER` statt einer Zahl wie `0` oder `1`.

Warum ist das besser?

- Die Werte sind in der Datenbank lesbar.
- Die Reihenfolge im Enum kann später geändert werden, ohne alte Daten falsch zu interpretieren.
- SQL-Abfragen bleiben verständlicher.

### `@WebFilter`

`AuthFilter` schützt Seiten:

```java
@WebFilter("*.xhtml")
public class AuthFilter implements Filter {
    ...
}
```

Der Filter wird für alle XHTML-Seiten ausgeführt. Er prüft, ob die Seite öffentlich ist oder ein Benutzer angemeldet ist.

Öffentliche Seiten:

```java
private static final Set<String> PUBLIC_PAGES = Set.of(
        "/index.xhtml",
        "/login.xhtml",
        "/register.xhtml"
);
```

Ist der Benutzer nicht angemeldet, wird auf die Login-Seite umgeleitet:

```java
httpResponse.sendRedirect(contextPath + "/login.xhtml?redirect=" + encodedPath);
```

Das ist ein zentraler Zugriffsschutz. Man muss nicht in jeder einzelnen View dieselbe Prüfung wiederholen.

### `@WebServlet`

`HelloServlet` zeigt ein klassisches Servlet:

```java
@WebServlet(name = "helloServlet", value = "/hello-servlet")
public class HelloServlet extends HttpServlet {
    ...
}
```

Das Servlet ist ein einfaches Beispiel für requestbasierte HTTP-Verarbeitung. Die eigentliche Anwendung nutzt aber hauptsächlich JSF.

## 5. Persistenz mit JPA und Hibernate

Die zentrale Konfiguration liegt in `src/main/resources/META-INF/persistence.xml`.

```xml
<persistence-unit name="eventmanagerPU" transaction-type="RESOURCE_LOCAL">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

    <properties>
        <property name="jakarta.persistence.jdbc.driver" value="org.h2.Driver"/>
        <property name="jakarta.persistence.jdbc.url"
                  value="jdbc:h2:file:/Users/heinke.hihn/IdeaProjects/eventmanager_sose2026/data/eventdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"/>
        <property name="jakarta.persistence.jdbc.user" value="sa"/>
        <property name="jakarta.persistence.jdbc.password" value=""/>

        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <property name="hibernate.show_sql" value="true"/>
        <property name="hibernate.format_sql" value="true"/>
    </properties>
</persistence-unit>
```

### Persistence Unit

Die Persistence Unit heisst:

```text
eventmanagerPU
```

Die Repositories verwenden genau diesen Namen:

```java
private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");
```

JPA weiss dadurch, welche Datenbankverbindung und welcher Provider verwendet werden sollen.

### `RESOURCE_LOCAL`

```xml
transaction-type="RESOURCE_LOCAL"
```

Das bedeutet: Die Anwendung verwaltet Transaktionen selbst über `EntityTransaction`.

Beispiel aus `EventRepository`:

```java
EntityTransaction tx = em.getTransaction();

try {
    tx.begin();
    if (event.getId() == null){
        em.persist(event);
    } else {
        em.merge(event);
    }
    tx.commit();
} catch (RuntimeException ex) {
    if (tx.isActive()) {
        tx.rollback();
    }
    throw ex;
} finally {
    em.close();
}
```

Dieses Muster ist wichtig:

- Transaktion starten
- Änderung ausführen
- committen
- bei Fehler rollback
- EntityManager immer schliessen

### `EntityManagerFactory` und `EntityManager`

Die `EntityManagerFactory` ist schwergewichtig und wird verwendet, um `EntityManager` zu erzeugen.

```java
private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");
```

Der `EntityManager` arbeitet mit Entities:

```java
EntityManager em = getEntityManager();
```

Er wird im Repository pro Operation geöffnet und im `finally`-Block geschlossen:

```java
finally {
    em.close();
}
```

Das ist wichtig, weil offene EntityManager Ressourcen belegen.

### `persist` vs. `merge`

In den Repositories wird anhand der ID entschieden, ob ein Objekt neu ist:

```java
if (user.getId() == null){
    em.persist(user);
} else {
    em.merge(user);
}
```

`persist(...)` bedeutet: Neues Entity speichern.

`merge(...)` bedeutet: Änderungen eines bestehenden oder detached Entity in den Persistence Context übernehmen.

Merksatz:

```text
Keine ID -> neues Objekt -> persist
Vorhandene ID -> bestehendes Objekt -> merge
```

## 6. Entities im Projekt

### `Event`

`Event` ist eine JPA-Entity für Veranstaltungen.

```java
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private String date;
    private String state;

    protected Event(){
        // benötigt für die JPA
    }
}
```

Wichtige Punkte:

- `@Entity` macht die Klasse persistent.
- `@Table(name = "events")` legt den Tabellennamen fest.
- `@Id` markiert den Primärschlüssel.
- `GenerationType.IDENTITY` nutzt eine datenbankseitige ID-Erzeugung.
- Der leere Konstruktor ist für JPA notwendig.

Der Konstruktor ist `protected`. Das ist eine gute fachliche Einschränkung: JPA kann ihn nutzen, aber Anwendungscode soll eher den Konstruktor mit Werten verwenden.

### `User`

`User` ist eine Entity für Benutzerkonten.

```java
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 128)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;
}
```

Hier sieht man mehrere wichtige Persistenzkonzepte:

- Benutzername und E-Mail sind eindeutig.
- Passwort wird nicht als Klartext gespeichert, sondern als Hash.
- Java-Feldnamen können über `@Column(name = "...")` anderen Spaltennamen zugeordnet werden.
- Rollen werden als lesbare Strings gespeichert.

### `UserRole`

```java
public enum UserRole {
    ADMIN,
    ORGANISATOR,
    TEILNEHMER,
}
```

Enums sind sinnvoll, wenn nur eine feste Menge gültiger Werte existiert. Dadurch kann nicht versehentlich irgendein beliebiger Rollenname verwendet werden.

### `EventDTO`

`EventDTO` ist keine Entity.

```java
public class EventDTO {
    private String name;
    private String location;
    private String date;
    private String state;
}
```

DTO bedeutet Data Transfer Object. Es transportiert Daten zwischen View und Controller.

Im Projekt wird ein DTO für das Event-Formular verwendet:

```java
private EventDTO newEvent = new EventDTO();
```

Danach wird es in eine Entity gemappt:

```java
private Event mapDTOToEvent(EventDTO dto) {
    return new Event(
            dto.getName(),
            dto.getLocation(),
            dto.getDate(),
            dto.getState()
    );
}
```

Das ist ein gutes Muster: Die View arbeitet nicht direkt mit der JPA-Entity. Dadurch lassen sich Formularfelder, Validierung und Datenbankmodell getrennt entwickeln.

### `SessionUser`

`SessionUser` ist ebenfalls keine Entity.

```java
public class SessionUser implements Serializable {

    private final Long id;
    private final String username;
    private final UserRole role;
}
```

Diese Klasse speichert nur die Daten, die für die Login-Session benötigt werden.

Das ist besser, als das komplette `User`-Entity in die Session zu legen:

- weniger Daten in der Session
- keine JPA-Probleme mit detached Entities
- keine unnötige Speicherung des Passwort-Hashes in der Session

## 7. JPQL und SQL

Das Projekt verwendet in den Repositories JPQL. JPQL sieht SQL ähnlich, arbeitet aber mit Entity-Klassen und ihren Feldern statt direkt mit Tabellen und Spalten.

### Alle Events laden

Aus `EventRepository`:

```java
return em.createQuery("SELECT e FROM Event e", Event.class).getResultList();
```

JPQL:

```sql
SELECT e FROM Event e
```

Sinngemäss erzeugt Hibernate daraus SQL gegen die Tabelle `events`:

```sql
select
    e.id,
    e.name,
    e.location,
    e.date,
    e.state
from events e;
```

Wichtig: In JPQL steht `Event`, weil das die Entity-Klasse ist. In SQL steht `events`, weil das die Tabelle ist.

### Event per ID laden

Aus `EventRepository`:

```java
return em.find(Event.class, id);
```

Sinngemässer SQL-Befehl:

```sql
select
    e.id,
    e.name,
    e.location,
    e.date,
    e.state
from events e
where e.id = ?;
```

Das Fragezeichen steht für einen Parameterwert, den JDBC/Hibernate setzt.

### Benutzer per Username laden

Aus `UserRepository`:

```java
User user = em.createQuery(
        "SELECT u FROM User u WHERE lower(u.username) = lower(:username)",
        User.class
).setParameter("username", username)
 .getSingleResult();
```

JPQL:

```sql
SELECT u FROM User u WHERE lower(u.username) = lower(:username)
```

Sinngemässer SQL-Befehl:

```sql
select
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.password_hash,
    u.role
from users u
where lower(u.username) = lower(?);
```

Das ist ein wichtiges Best-Practice-Beispiel:

```java
.setParameter("username", username)
```

Der Wert wird als Parameter gebunden und nicht per String-Konkatenation in die Query eingebaut. Das schützt vor SQL Injection und vermeidet Fehler bei Sonderzeichen.

### Benutzer per E-Mail laden

Aus `UserRepository`:

```java
User user = em.createQuery(
        "SELECT u FROM User u WHERE lower(u.email) = lower(:email)",
        User.class
).setParameter("email", email)
 .getSingleResult();
```

Sinngemässer SQL-Befehl:

```sql
select
    u.id,
    u.username,
    u.email,
    u.first_name,
    u.last_name,
    u.password_hash,
    u.role
from users u
where lower(u.email) = lower(?);
```

### Neues Event speichern

Aus `EventRepository`:

```java
if (event.getId() == null){
    em.persist(event);
}
```

Sinngemässer SQL-Befehl:

```sql
insert into events
    (name, location, date, state)
values
    (?, ?, ?, ?);
```

Da `id` automatisch erzeugt wird, muss der Anwendungscode keine ID setzen.

### Bestehendes Event aktualisieren

Aus `EventRepository`:

```java
em.merge(event);
```

Sinngemässer SQL-Befehl:

```sql
update events
set
    name = ?,
    location = ?,
    date = ?,
    state = ?
where id = ?;
```

### Tabellenstruktur

Durch `hibernate.hbm2ddl.auto=update` kann Hibernate Tabellen aus den Entities erzeugen oder aktualisieren.

Sinngemäss passt `Event` zu einer Tabelle wie:

```sql
create table events (
    id bigint generated by default as identity primary key,
    name varchar(255),
    location varchar(255),
    date varchar(255),
    state varchar(255)
);
```

Sinngemäss passt `User` zu einer Tabelle wie:

```sql
create table users (
    id bigint primary key,
    username varchar(50) not null unique,
    email varchar(128) not null unique,
    first_name varchar(128) not null,
    last_name varchar(128) not null,
    password_hash varchar(128) not null,
    role varchar(30) not null
);
```

Die exakt erzeugte SQL-Syntax kann je nach Hibernate-Version, H2-Version und ID-Strategie leicht anders aussehen. Fachlich wichtig ist die Zuordnung von Entity, Tabelle, Spalte, Primärschlüssel und Constraints.

### Nützliche SQL-Abfragen zum Lernen

Wenn ihr die H2-Datenbank inspiziert, sind diese Abfragen hilfreich:

```sql
select * from events;
select * from users;
select username, email, role from users;
select name, location, date, state from events order by id;
```

Passwörter sollten nie manuell als Klartext in die Datenbank geschrieben werden. Die Anwendung verwendet BCrypt und speichert nur `password_hash`.

## 8. Login, Registrierung und Zugriffsschutz

Das Projekt enthält einen einfachen Authentifizierungsfluss.

### Registrierung

Die Registrierung startet in `register.xhtml`:

```xml
<h:commandButton id="submitRegistration"
                 value="Registrieren"
                 action="#{registrationController.submitRegistration}"/>
```

Der Button ruft `RegistrationController.submitRegistration()` auf.

Dort werden zuerst die Passwörter verglichen:

```java
if (!password.equals(confirmPassword)) {
    FacesContext.getCurrentInstance().addMessage(
            null,
            new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Registrierung fehlgeschlagen",
                    "Passwort und Passwort-Bestaetigung stimmen nicht ueberein.")
    );
    return null;
}
```

Dann wird `AuthService.register(...)` aufgerufen.

Im Service werden Benutzername und E-Mail geprüft:

```java
if (userRepository.findByUsername(username).isPresent()){
    return RegistrationResult.USERNAME_EXISTS;
}

if (userRepository.findByEmail(email).isPresent()){
    return RegistrationResult.EMAIL_EXISTS;
}
```

Danach wird das Passwort gehasht:

```java
String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
```

Erst dieser Hash wird gespeichert:

```java
User user = new User(username, email, firstName, lastName, passwordHash, UserRole.TEILNEHMER);
userRepository.save(user);
```

Das ist eine wichtige Sicherheits-Best-Practice.

### Login

`login.xhtml` bindet die Eingaben an `LoginController`:

```xml
<h:inputText id="username"
             value="#{loginController.username}"
             required="true"/>

<h:inputSecret id="password"
               value="#{loginController.password}"
               required="true"/>
```

Das Passwortfeld nutzt `h:inputSecret`, damit die Eingabe im Browser verdeckt wird.

Der Login-Button ruft auf:

```xml
<h:commandButton id="submitLogin"
                 value="Anmelden"
                 action="#{loginController.submitLogin}"/>
```

`LoginController` delegiert an `AuthController`:

```java
boolean authenticated = authController.login(username, password);
```

`AuthController` delegiert an `AuthService`:

```java
Optional<SessionUser> sessionUser = authService.authenticate(userName, password);
```

`AuthService` prüft das Passwort:

```java
if (!BCrypt.checkpw(plainPassword, user.getPasswordHash())) {
    return Optional.empty();
}
```

Bei Erfolg wird ein kleines Session-Objekt erzeugt:

```java
return Optional.of(new SessionUser(user.getId(), user.getUsername(), user.getRole()));
```

### Zugriffsschutz mit Filter

`AuthFilter` schützt alle XHTML-Seiten:

```java
@WebFilter("*.xhtml")
public class AuthFilter implements Filter {
    ...
}
```

Der Filter erlaubt öffentliche Seiten:

```java
if (isPublic(path)){
    filterChain.doFilter(httpRequest, httpResponse);
    return;
}
```

Er erlaubt auch JSF-Ressourcen:

```java
return path.startsWith("/jakarta.faces.resource/");
```

Das ist wichtig, damit CSS, JS und andere Ressourcen auch auf der Login-Seite geladen werden können.

Dann prüft er die Session:

```java
HttpSession session = httpRequest.getSession(false);
Object authUser = session != null ? session.getAttribute(AuthController.SESSION_USER_KEY) : null;
```

Wenn kein Benutzer angemeldet ist:

```java
httpResponse.sendRedirect(contextPath + "/login.xhtml?redirect=" + encodedPath);
```

Damit sind `/events.xhtml` und `/create-event.xhtml` geschützt.

## 9. Jakarta Faces in den XHTML-Dateien

### Templates

Das Projekt nutzt ein gemeinsames Layout:

```xml
<ui:composition template="WEB-INF/templates/layout.xhtml">
    <ui:define name="content">
        ...
    </ui:define>
</ui:composition>
```

Das Template liegt in:

```text
src/main/webapp/WEB-INF/templates/layout.xhtml
```

Vorteil:

- Header, Navigation und Footer stehen an einer Stelle.
- Einzelne Seiten definieren nur ihren Inhalt.
- Änderungen am Layout müssen nicht in jeder View wiederholt werden.

### Navigation abhängig vom Login-Zustand

In `layout.xhtml` wird die Navigation abhängig von `authController.loggedIn` gerendert:

```xml
<h:panelGroup rendered="#{authController.loggedIn}">
    <h:link value="Veranstaltungen" outcome="events"/>
    <h:outputText value="Hallo #{authController.currentUser.username}!"/>
</h:panelGroup>

<h:panelGroup rendered="#{not authController.loggedIn}">
    <h:link value="Login" outcome="login"/>
    <h:link value="Registrieren" outcome="register"/>
</h:panelGroup>
```

JSF kann `#{authController.loggedIn}` lesen, obwohl die Methode in Java `isLoggedIn()` heisst. Das ist JavaBean-Konvention.

### Formulare und Validierung

JSF-Formular aus `create-event.xhtml`:

```xml
<h:form>
    <h:inputText id="name"
                 value="#{eventController.newEvent.name}"
                 required="true"/>

    <h:commandButton value="Speichern"
                     action="#{eventController.saveEvent}"/>
</h:form>
```

`required="true"` aktiviert einfache Pflichtfeldvalidierung. Fehlermeldungen können mit `h:message` oder `h:messages` angezeigt werden.

Beispiel aus `login.xhtml`:

```xml
<h:message for="username" styleClass="field-error"/>
```

Globale Meldungen im Layout:

```xml
<h:messages id="messages" redisplay="false" />
```

### Tabellen

`events.xhtml` nutzt `h:dataTable`:

```xml
<h:dataTable styleClass="event-table"
             id="eventsTable"
             value="#{eventController.events}"
             var="event">

  <h:column>
    <f:facet name="header">Event</f:facet>
    #{event.name}
  </h:column>
</h:dataTable>
```

`value` ist die Liste, `var` ist der Name des aktuellen Elements in der Schleife.

Das entspricht gedanklich:

```java
for (Event event : eventController.getEvents()) {
    ...
}
```

## 10. Best Practices, die im Repository sichtbar sind

### Klare Schichtung

Views, Controller, Services, Repositories und Entities sind getrennt.

Gutes Beispiel:

```text
create-event.xhtml
  -> EventController.saveEvent()
  -> EventService.saveEvent(...)
  -> EventRepository.save(...)
  -> EntityManager.persist(...)
```

Diese Trennung macht Code testbarer und leichter verständlich.

### Dependency Injection statt manuellem Verdrahten

`@Inject` reduziert Kopplung:

```java
@Inject
private AuthService authService;
```

Die Klasse muss nicht selbst wissen, wie `AuthService` erzeugt wird.

### Parameterisierte Queries

Im Repository wird nicht so gearbeitet:

```java
"SELECT u FROM User u WHERE u.username = '" + username + "'"
```

Stattdessen:

```java
"SELECT u FROM User u WHERE lower(u.username) = lower(:username)"
```

mit:

```java
.setParameter("username", username)
```

Das ist sicherer und robuster.

### Transaktionen mit Rollback

Die Repositories verwenden ein sauberes Fehlerbehandlungsmuster:

```java
try {
    tx.begin();
    ...
    tx.commit();
} catch (RuntimeException ex) {
    if (tx.isActive()) {
        tx.rollback();
    }
    throw ex;
} finally {
    em.close();
}
```

Das verhindert teilweise gespeicherte Änderungen.

### EntityManager wird geschlossen

Jeder geöffnete EntityManager wird im `finally`-Block geschlossen:

```java
finally {
    em.close();
}
```

Das ist wichtig für Ressourcenmanagement.

### Passwörter werden gehasht

Registrierung:

```java
String passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
```

Login:

```java
BCrypt.checkpw(plainPassword, user.getPasswordHash())
```

Das ist eine zentrale Sicherheitsregel: Niemals Klartextpasswörter speichern.

### Kleines Session-Objekt statt Entity in der Session

`SessionUser` enthält nur:

- ID
- Benutzername
- Rolle

Das ist besser als das komplette `User`-Entity in der Session zu halten.

### Zentrale Zugriffskontrolle

`AuthFilter` schützt Seiten an einer zentralen Stelle. Das ist übersichtlicher als viele Einzelprüfungen in Views und Controllern.

### Templates statt Duplikation

`layout.xhtml` vermeidet kopierten Header-/Footer-Code in jeder Seite.

### CSS Design Tokens

In `style.css` werden CSS-Variablen verwendet:

```css
:root {
    --color-primary: #227C9D;
    --color-secondary: #17C3B2;
    --space-md: 1rem;
}
```

Das ist wartbarer als überall feste Farb- und Abstandswerte zu wiederholen.

## 11. Lernchancen und mögliche Verbesserungen

Das Projekt ist gut geeignet, um die Grundkonzepte zu lernen. Gleichzeitig gibt es Stellen, an denen man weiterdenken kann.

### Datenbankpfad

In `persistence.xml` steht ein absoluter Pfad:

```xml
value="jdbc:h2:file:/Users/heinke.hihn/IdeaProjects/eventmanager_sose2026/data/eventdb;DB_CLOSE_DELAY=-1;AUTO_SERVER=TRUE"
```

Für lokale Übungen funktioniert das. Für ein Teamprojekt wäre ein relativer Pfad oder eine Konfiguration über Umgebungsvariablen flexibler.

### `hibernate.hbm2ddl.auto=update`

```xml
<property name="hibernate.hbm2ddl.auto" value="update"/>
```

Das ist praktisch in der Entwicklung, weil Hibernate Tabellen automatisch aktualisiert. In Produktion sollte man Schemaänderungen aber kontrolliert über Migrationstools wie Flyway oder Liquibase durchführen.

### Datum als `String`

`Event` speichert das Datum aktuell so:

```java
private String date;
```

Zum Lernen ist das einfach. Fachlich wäre oft `LocalDate` besser:

```java
private LocalDate date;
```

Dann sind Sortierung, Validierung und Datumslogik sauberer.

### Bean Validation

In den XHTML-Dateien gibt es `required="true"`. Zusätzlich könnten Entities oder DTOs mit Bean Validation annotiert werden:

```java
@NotBlank
@Size(max = 50)
private String username;
```

Dann liegen Validierungsregeln näher am Datenmodell oder DTO.

### UI-Meldungen eher im Controller

`AuthService` ist ein gutes Beispiel für fachliche Logik ohne direkte UI-Abhängigkeit: Er gibt ein Ergebnis zurück, und der Controller erzeugt daraus eine Meldung.

`EventService` erzeugt aktuell selbst eine `FacesMessage`:

```java
FacesContext.getCurrentInstance().addMessage(null, message);
```

Für ein Lernprojekt ist das nachvollziehbar. In einer strengeren Schichtung wäre die Erfolgsmeldung eher Aufgabe des Controllers, weil `FacesContext` zur JSF-Oberfläche gehört. Der Service würde dann nur speichern oder ein Ergebnis zurückgeben.

### EntityManagerFactory Lifecycle

Die Repositories erzeugen je eine `EntityManagerFactory`:

```java
private final EntityManagerFactory emf = createEntityManagerFactory("eventmanagerPU");
```

Für kleine Lernprojekte ist das verständlich. In grösseren Anwendungen würde man die Factory zentral verwalten oder vom Container injizieren lassen.

### Rollenprüfung

`UserRole` ist bereits vorhanden, aber der Filter prüft aktuell nur, ob ein Benutzer angemeldet ist. Eine spätere Erweiterung könnte zusätzlich Rollen prüfen:

```text
Nur ADMIN darf Benutzer verwalten.
Nur ORGANISATOR darf Events erstellen.
TEILNEHMER darf Events ansehen.
```

### Redirect nach Login

`AuthFilter` hängt einen `redirect`-Parameter an:

```java
login.xhtml?redirect=...
```

`LoginController` leitet aktuell immer nach `/events.xhtml` weiter. Eine Erweiterung wäre, den Redirect-Parameter auszulesen und nach erfolgreichem Login zur ursprünglich angefragten Seite zurückzukehren.

## 12. Zusammenfassung

Das Repository zeigt viele zentrale Konzepte einer Jakarta-Webanwendung:

- JSF-Views mit Facelets, Formularen, Tabellen und Templates
- CDI-Beans mit passenden Scopes
- Dependency Injection mit `@Inject`
- JPA-Entities mit Tabellen- und Spaltenmapping
- Repository-Pattern für Datenbankzugriff
- JPQL-Abfragen mit Parametern
- Transaktionssteuerung mit `begin`, `commit` und `rollback`
- Login mit BCrypt und Session-Verwaltung
- Zugriffsschutz mit Servlet Filter
- Trennung von DTO, Entity, Service und Controller

Wenn ihr dieses Projekt versteht, habt ihr eine solide Grundlage für klassische serverseitige Java-Webanwendungen mit Jakarta EE.

## 13. Übungsfragen

1. Warum ist `AuthController` `@SessionScoped`, aber `LoginController` nur `@ViewScoped`?
2. Warum sollte man ein `User`-Entity nicht komplett in der Session speichern?
3. Was ist der Unterschied zwischen JPQL `SELECT u FROM User u` und SQL `select * from users`?
4. Warum wird `setParameter(...)` verwendet?
5. Was passiert, wenn bei `save(...)` nach `tx.begin()` ein Fehler auftritt?
6. Warum braucht eine JPA-Entity einen leeren Konstruktor?
7. Warum ist `EnumType.STRING` für Rollen meist besser als `EnumType.ORDINAL`?
8. Welche Schicht sollte eine `FacesMessage` erzeugen: Repository, Service oder Controller?
9. Warum muss der Filter JSF-Ressourcen wie `/jakarta.faces.resource/` erlauben?
10. Welche Änderung wäre nötig, damit nur Organisatoren neue Events anlegen dürfen?
