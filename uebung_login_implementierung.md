# Übungsaufgabe: Login-Mechanismus implementieren

In dieser Übung erweitert ihr den aktuellen Stand aus `main` um einen Login-Mechanismus. Die Registrierung ist 
bereits vorhanden: Benutzer können angelegt werden und Passwörter werden als Hash gespeichert. Am Ende sollen 
sich Benutzer anmelden und abmelden können. Geschützte Seiten dürfen nur nach erfolgreichem Login erreichbar sein.

Der Branch `4_user_management` dient als Referenz für den Zielzustand. Nutzt ihn nicht als Kopiervorlage am Anfang, 
sondern erst zum Abgleich, wenn ihr eure eigene Lösung umgesetzt habt. Schaut gerne in den Branch rein, wenn ihr nicht
weiterkommt und Hilfe braucht.

## Lernziele

Nach der Übung könnt ihr:

- eine einfache Authentifizierung mit Benutzername und Passwort umsetzen,
- Passwort-Hashes mit `BCrypt.checkpw(...)` prüfen,
- Login-Informationen in der HTTP-Session speichern,
- JSF-Controller für Login und Logout verwenden,
- Seiten mit einem Servlet-Filter schützen,
- JSF-Views anhand des Login-Status unterschiedlich darstellen.

## Erwarteter Zielzustand

Nach der Umsetzung soll Folgendes funktionieren:

- `/index.xhtml`, `/login.xhtml` und `/register.xhtml` sind ohne Login erreichbar.
- `/events.xhtml` und `/create-event.xhtml` sind nur für angemeldete Benutzer erreichbar.
- Nicht angemeldete Benutzer werden auf `/login.xhtml` umgeleitet.
- Nach erfolgreichem Login wird der Benutzer auf `/events.xhtml` oder zur ursprünglich angefragten Seite weitergeleitet.
- Nach erfolgreicher Registrierung ist der neue Benutzer direkt angemeldet.
- Die Navigation zeigt für angemeldete Benutzer andere Links als für Gäste.
- Logout beendet die Session.

## Gesamtzeit

Wir planen für die gesamte Übung etwa **3 bis 3,5 Stunden** ein.

Die Zeiten pro Teil sind Richtwerte. Nach jedem Teil besprechen wir gemeinsam einen möglichen Lösungsweg und updaten
den `main` Branch, sodass ihr euch auf den neusten Stand aktualisieren könnt. Wenn ihr deutlich länger braucht, macht zuerst einen kleinen Zwischentest, bevor ihr weiterarbeitet.

## Rahmenbedingungen

- Arbeitet ausgehend vom Branch `main`.
- Verändert nur Dateien, die für den Login notwendig sind.
- Speichert niemals Klartextpasswörter.
- Nutzt die vorhandene Registrierung, das vorhandene `UserRepository` und die vorhandene `User`-Entity.
- Achtet darauf, dass `@SessionScoped`-Objekte serialisierbar sein müssen.

## Teil 1: Ausgangszustand analysieren

**Zeit:** 15 Minuten

Verschafft euch einen Überblick über den vorhandenen Registrierungsprozess.

Untersucht besonders diese Dateien:

- `user/User.java`
- `repository/UserRepository.java`
- `auth/AuthService.java`
- `auth/RegistrationController.java`
- `webapp/register.xhtml`

Beantwortet für euch:

- Wo wird ein neuer Benutzer gespeichert?
- Wo wird das Passwort gehasht?
- Welche Methode findet einen Benutzer anhand des Benutzernamens?
- Welche Rolle bekommt ein neu registrierter Benutzer?
- Wohin leitet die Registrierung aktuell nach Erfolg weiter?

**Tipp:** Für den Login braucht ihr nicht die komplette Benutzerverwaltung neu zu bauen. 
Die wichtigste vorhandene Grundlage ist `UserRepository.findByUsername(String username)`.

## Teil 2: SessionUser entwerfen

**Zeit:** 15 Minuten

Legt eine neue Klasse an:

`auth/SessionUser.java`

Diese Klasse soll die Benutzerinformationen enthalten, die während einer Login-Session gebraucht werden.

### Aufgabe

`SessionUser` soll:

- `Serializable` implementieren,
- die Benutzer-ID speichern,
- den Benutzernamen speichern,
- die Rolle speichern,
- keine JPA-Entity sein,
- nur die wirklich notwendigen Daten in der Session halten.

### Vorgegebene Struktur

Die Klasse soll diese Felder besitzen:

```java
private final Long id;
private final String username;
private final UserRole role;
```

Der Konstruktor soll diese Argumente annehmen:

```java
SessionUser(Long id, String username, UserRole role)
```

Erstellt Getter für:

```java
Long getId()
String getUsername()
UserRole getRole()
```

**Tipp:** Speichert nicht das komplette `User`-Entity in der Session. Ein kleines Session-Objekt ist übersichtlicher und vermeidet spätere Probleme mit JPA-Objekten ausserhalb ihres Persistence Contexts.

## Teil 3: AuthService um Passwortprüfung erweitern

**Zeit:** 25 Minuten

Erweitert:

`auth/AuthService.java`

Bisher ist der Service für die Registrierung zuständig. Nun soll er auch prüfen können, ob Login-Daten gültig sind.

### Aufgabe

Fügt eine neue Methode hinzu:

```java
Optional<SessionUser> authenticate(String username, String plainPassword)
```

Die Methode soll:

- mit `userRepository.findByUsername(username)` den Benutzer suchen,
- bei unbekanntem Benutzer `Optional.empty()` zurückgeben,
- das eingegebene Passwort mit dem gespeicherten Hash vergleichen,
- bei falschem Passwort `Optional.empty()` zurückgeben,
- bei Erfolg ein `SessionUser`-Objekt zurückgeben.

### Erwartete Abhängigkeiten

Ihr braucht:

```java
import java.util.Optional;
```

Und ihr nutzt weiterhin:

```java
org.mindrot.jbcrypt.BCrypt
```

**Tipp:** Beim Registrieren wurde `BCrypt.hashpw(...)` verwendet. Beim Login braucht ihr die 
passende Prüfmethode aus derselben Bibliothek. Achtet auf die Reihenfolge der Argumente.

**Tipp:** Die Methode soll kein `FacesMessage` erzeugen. Der Service prüft nur fachlich, 
ob Benutzername und Passwort stimmen. Die Fehlermeldung gehört später in den Controller.

## Teil 4: AuthController als Session-Bean ausbauen

**Zeit:** 25 Minuten

Erweitert:

`auth/AuthController.java`

Diese Klasse soll den Login-Status für die aktuelle Browser-Session verwalten.

### Aufgabe

Der `AuthController` soll:

- `@Named` bleiben,
- `@SessionScoped` sein,
- `Serializable` implementieren,
- den `AuthService` injizieren,
- den aktuell angemeldeten Benutzer speichern,
- Login und Logout anbieten,
- für JSF abfragbar machen, ob ein Benutzer angemeldet ist.

### Vorgegebene Felder

Fügt ein konstantes Session-Key-Feld hinzu:

```java
public static final String SESSION_USER_KEY = "AUTH_USER";
```

Fügt ein Feld für den aktuellen Benutzer hinzu:

```java
private SessionUser currentUser;
```

### Vorgegebene Methoden

Implementiert diese Methoden:

```java
boolean login(String username, String password)
```

Die Methode soll:

- `authService.authenticate(username, password)` aufrufen,
- bei fehlgeschlagener Authentifizierung `false` zurückgeben,
- bei Erfolg `currentUser` setzen,
- den Benutzer zusätzlich in der JSF-/HTTP-Session unter `SESSION_USER_KEY` speichern,
- bei Erfolg `true` zurückgeben.

```java
String logout()
```

Die Methode soll:

- die aktuelle Session invalidieren,
- `currentUser` zurücksetzen,
- zur Login-Seite weiterleiten.

```java
boolean isLoggedIn()
```

Die Methode soll:

- `true` liefern, wenn `currentUser` gesetzt ist,
- sonst `false` liefern.

```java
SessionUser getCurrentUser()
```

Die Methode soll:

- den aktuellen `SessionUser` für JSF-Ausdrücke bereitstellen.

**Tipp:** In JSF kann `#{authController.loggedIn}` auf `isLoggedIn()` zugreifen. Ihr braucht dafür keine Methode `getLoggedIn()`.

**Tipp:** Für die Session Map könnt ihr über `FacesContext.getCurrentInstance().getExternalContext().getSessionMap()` gehen.

## Teil 5: LoginController für das Formular erstellen

**Zeit:** 25 Minuten

Legt eine neue Klasse an:

`auth/LoginController.java`

Diese Klasse ist für das Login-Formular zuständig. Sie nimmt Eingaben aus der View entgegen und ruft den `AuthController` auf.

### Aufgabe

`LoginController` soll:

- `@Named` sein,
- `@ViewScoped` sein,
- `Serializable` implementieren,
- den `AuthController` injizieren,
- Formularfelder für Benutzername und Passwort besitzen,
- bei falschen Login-Daten eine JSF-Fehlermeldung anzeigen,
- bei Erfolg weiterleiten.

### Vorgegebene Felder

```java
private String username;
private String password;
```

### Vorgegebene Methoden

```java
String submitLogin()
```

Die Methode soll:

- `authController.login(username, password)` aufrufen,
- bei `false` eine `FacesMessage` mit Severity `ERROR` erzeugen,
- bei Fehler `null` zurückgeben, damit die Login-Seite angezeigt bleibt,
- bei Erfolg auf `/events.xhtml` weiterleiten,
- optional einen Request-Parameter `redirect` auswerten.

Getter und Setter:

```java
String getUsername()
void setUsername(String username)
String getPassword()
void setPassword(String password)
```

Beispiel:

```text
/login.xhtml?redirect=%2Fevents.xhtml
```

**Tipp:** Lasst nur Redirects zu, die mit `/` beginnen und auf `.xhtml` enden. Damit verhindert ihr, dass beliebige externe URLs als Weiterleitungsziel verwendet werden.

**Tipp:** Der `LoginController` soll nicht selbst Passwort-Hashes prüfen. Dafür ist `AuthService.authenticate(...)` zuständig.

## Teil 6: Login-Seite erstellen

**Zeit:** 20 Minuten

Legt eine neue JSF-Seite an:

`src/main/webapp/login.xhtml`

### Aufgabe

Die Seite soll:

- das bestehende Layout-Template verwenden,
- eine Überschrift "Login" anzeigen,
- ein JSF-Formular enthalten,
- ein Eingabefeld für den Benutzernamen enthalten,
- ein Passwortfeld enthalten,
- beide Felder als Pflichtfelder markieren,
- Feldfehlermeldungen anzeigen,
- beim Absenden `loginController.submitLogin` aufrufen.

### Erwartete Komponenten

Verwendet passende JSF-Komponenten, zum Beispiel:

- `h:form`
- `h:panelGrid`
- `h:outputLabel`
- `h:inputText`
- `h:inputSecret`
- `h:message`
- `h:commandButton`

### Erwartete Bindings

Das Benutzername-Feld soll an diese Property gebunden sein:

```text
#{loginController.username}
```

Das Passwort-Feld soll an diese Property gebunden sein:

```text
#{loginController.password}
```

Der Button soll diese Action aufrufen:

```text
#{loginController.submitLogin}
```

**Tipp:** Orientiert euch an `register.xhtml`. Die Login-Seite ist deutlich kleiner, folgt aber demselben JSF-Muster.

## Teil 7: Registrierung nach Erfolg automatisch einloggen

**Zeit:** 15 Minuten

Erweitert:

`auth/RegistrationController.java`

Nach einer erfolgreichen Registrierung soll der Benutzer nicht nur angelegt, sondern direkt angemeldet werden.

### Aufgabe

Der `RegistrationController` soll:

- den `AuthController` injizieren,
- nach erfolgreicher Registrierung `authController.login(username, password)` aufrufen,
- danach auf `/events.xhtml` weiterleiten.

### Vorgegebene Erweiterung

Fügt ein injiziertes Feld hinzu:

```java
private AuthController authController;
```

Die bestehende Methode bleibt:

```java
String submitRegistration()
```

Ergänzt ihren Ablauf nur am Erfolgsfall.

**Tipp:** Der Login nach der Registrierung nutzt noch das Klartextpasswort aus dem Formular. Das ist in diesem Moment verfügbar. In der Datenbank liegt trotzdem nur der Hash.

**Tipp:** Achtet darauf, dass der Auto-Login nur passiert, wenn die Registrierung wirklich erfolgreich war. Bei vergebenem Benutzernamen oder vergebener E-Mail darf kein Login stattfinden.

## Teil 8: Geschützte Seiten mit AuthenticationFilter absichern

**Zeit:** 35 Minuten

Legt eine neue Filterklasse an:

`auth/AuthenticationFilter.java`

Der Filter entscheidet vor dem Anzeigen einer `.xhtml`-Seite, ob ein Login erforderlich ist.

### Aufgabe

`AuthenticationFilter` soll:

- `jakarta.servlet.Filter` implementieren,
- mit `@WebFilter("*.xhtml")` für JSF-Seiten gelten,
- öffentliche Seiten ohne Login durchlassen,
- JSF-Ressourcen ohne Login durchlassen,
- für alle anderen Seiten prüfen, ob ein Benutzer in der Session liegt,
- nicht angemeldete Benutzer auf `login.xhtml` umleiten,
- den ursprünglichen Pfad als `redirect`-Parameter mitgeben.

### Vorgegebene öffentliche Seiten

Diese Seiten sollen ohne Login erreichbar bleiben:

```java
"/index.xhtml"
"/login.xhtml"
"/register.xhtml"
```

### Vorgegebene Methode

Implementiert:

```java
void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException
```

Die Methode soll:

- `ServletRequest` zu `HttpServletRequest` casten,
- `ServletResponse` zu `HttpServletResponse` casten,
- den Context Path und Request URI auslesen,
- daraus den Pfad innerhalb der Anwendung berechnen,
- öffentliche Pfade durchlassen,
- die bestehende Session mit `getSession(false)` lesen,
- unter `AuthController.SESSION_USER_KEY` nach einem Benutzer suchen,
- bei vorhandener Login-Session `chain.doFilter(...)` ausführen,
- sonst per `sendRedirect(...)` zur Login-Seite umleiten.

### Hilfsmethode

Erstellt eine private Hilfsmethode:

```java
private boolean isPublic(String path)
```

Sie soll `true` liefern, wenn:

- der Pfad in der Liste der öffentlichen Seiten steht,
- oder der Pfad mit `/jakarta.faces.resource/` beginnt.

**Tipp:** Verwendet `getSession(false)`, damit durch die reine Prüfung keine neue Session erzeugt wird.

**Tipp:** Ohne Ausnahme für `/jakarta.faces.resource/` kann es passieren, dass CSS oder JSF-Ressourcen auf der Login-Seite nicht geladen werden.

**Tipp:** Für den Redirect-Parameter sollte der Pfad URL-enkodiert werden.

## Teil 9: Navigation im Layout anpassen

**Zeit:** 20 Minuten

Passt an:

`templates/layout.xhtml`

Die Navigation soll für Gäste und angemeldete Benutzer unterschiedlich aussehen.

### Aufgabe

Die Navigation soll:

- immer einen Link zur Startseite anzeigen,
- für angemeldete Benutzer Links zu Veranstaltungen und neuer Veranstaltung anzeigen,
- für angemeldete Benutzer den Benutzernamen anzeigen,
- für angemeldete Benutzer einen Logout-Button anzeigen,
- für nicht angemeldete Benutzer Links zu Login und Registrierung anzeigen.

### Erwartete JSF-Ausdrücke

Login-Status:

```text
#{authController.loggedIn}
```

Aktueller Benutzername:

```text
#{authController.currentUser.username}
```

Logout-Action:

```text
#{authController.logout}
```

**Tipp:** Nutzt `rendered` auf `h:panelGroup`, um Teile der Navigation nur in bestimmten Zuständen anzuzeigen.

**Tipp:** Ein `h:commandButton` braucht ein `h:form`. Wenn der Logout-Button in der Navigation steht, kann ein kleines Inline-Formular sinnvoll sein.

## Teil 10: Startseite und Styles abrunden

**Zeit:** 15 Minuten

Passt optional an:

- `src/main/webapp/index.xhtml`
- `src/main/webapp/resources/css/style.css`

### Aufgabe für `index.xhtml`

Die Startseite soll je nach Login-Status einen passenden Hinweis anzeigen:

- angemeldet: Benutzername anzeigen,
- nicht angemeldet: Hinweis auf Login oder Registrierung anzeigen.

Verwendet auch hier:

```text
#{authController.loggedIn}
#{authController.currentUser.username}
```

### Aufgabe für `style.css`

Ergänzt Styles für:

- Feldfehlermeldungen,
- ein Inline-Formular in der Navigation,
- den Logout-Button in der Navigation.

Mögliche Klassennamen:

```css
.field-error
.nav-inline-form
.nav-menu-button
```

**Tipp:** Prüft, ob Passwortfelder durch eure bestehenden CSS-Selektoren bereits gestylt werden. Falls nicht, erweitert den Selektor für Formularfelder um `input[type="password"]`.

## Teil 11: Manuell testen

**Zeit:** 25 Minuten

Testet die Anwendung bewusst in mehreren Zuständen.

### Testfälle

1. Öffnet `/index.xhtml` ohne Login.
   Erwartung: Seite ist erreichbar.

2. Öffnet `/events.xhtml` ohne Login.
   Erwartung: Weiterleitung auf die Login-Seite.

3. Öffnet `/create-event.xhtml` ohne Login.
   Erwartung: Weiterleitung auf die Login-Seite.

4. Versucht einen Login mit falschem Passwort.
   Erwartung: Fehlermeldung, keine Weiterleitung.

5. Registriert einen neuen Benutzer.
   Erwartung: Benutzer wird angelegt und ist danach angemeldet.

6. Prüft die Navigation nach der Registrierung.
   Erwartung: Benutzername und Logout sind sichtbar.

7. Klickt Logout.
   Erwartung: Session ist beendet, Login-/Registrierungslinks sind sichtbar.

8. Loggt euch mit dem registrierten Benutzer erneut ein.
   Erwartung: Login klappt mit Benutzername und Passwort.

9. Ruft nach Logout wieder eine geschützte Seite direkt auf.
   Erwartung: erneute Weiterleitung zur Login-Seite.

**Tipp:** Wenn ein Benutzername bereits existiert, nehmt für den Test einen neuen Benutzernamen. Die H2-Datenbank behält lokale Testdaten.