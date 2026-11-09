# SpritRadar – Android-Projekt

SpritRadar ist ein Android-WebView-Container mit einer nativen Android-Brücke für Standortfreigabe, Tankstellenabfragen und Navigation.

## Funktionen
- Kartenansicht mit OpenStreetMap/Leaflet
- Standortfreigabe über Android
- Suche nach Tankstellen im Umkreis
- Diesel, Super E5 und Super E10
- Preise in der Kartenmarkierung und Ergebnisliste
- Sortierung nach Preis
- Navigation über Google Maps

## APK ohne eigenen PC erstellen (über GitHub im Handy-Browser)
1. Lade dieses ZIP auf dein Handy herunter und entpacke es mit der Dateien-App.
2. Öffne https://github.com und melde dich an oder erstelle ein Konto.
3. Erstelle ein neues Repository, zum Beispiel `SpritRadar`.
4. Öffne im Repository **Add file → Upload files** und lade alle Dateien und Ordner aus diesem Projektordner hoch. Wichtig: Auch den Ordner `.github/workflows` mit der Datei `build-apk.yml` hochladen. Falls dein Dateimanager versteckte Ordner ausblendet, aktiviere „Versteckte Dateien anzeigen“.
5. Tippe auf **Commit changes**. GitHub startet dann den APK-Build automatisch. Alternativ im Reiter **Actions** den Workflow **Build SpritRadar APK** auswählen und **Run workflow** tippen.
6. Warte, bis der Lauf grün als **Success** markiert ist. Öffne den Lauf, scrolle zu **Artifacts** und lade **SpritRadar-APK** herunter.
7. Entpacke das heruntergeladene Artifact-ZIP. Darin liegt `app-debug.apk`. Tippe darauf, erlaube falls nötig die Installation aus dieser Quelle und installiere die App.

Hinweis: GitHub benötigt ein Konto. Der Build läuft auf GitHub-Servern; ein Computer ist dafür nicht nötig. Die APK ist eine Debug-Version und nicht im Play Store veröffentlicht.

## Datenquelle
Die App verwendet `https://api.tankpuls.de/api/stations/search`. Für Kartenkacheln und Live-Daten ist Internet erforderlich. Die Quellenangabe ist in der App enthalten.

## Technische Daten
- Android SDK 35
- Java 17
- Gradle 8.9
- Mindestversion Android 6.0 (API 23)

Ohne Standortfreigabe verwendet die App den im App-Code hinterlegten Kartenmittelpunkt. Preise werden für diesen Mittelpunkt gesucht.
