# Bauen und Installieren

GitHub Actions verwendet Ubuntu 24.04, Temurin Java 17 und Gradle 8.10. Der Workflow
führt zuerst die JVM-Unit-Tests und danach den Debug-APK-Build aus. Er lädt das
APK zusammen mit seiner SHA-256-Prüfsumme als CI-Artefakt hoch.

Für die lokale Prüfung werden Java 17 und Gradle 8.10 benötigt:

```sh
gradle testDebugUnitTest
gradle assembleDebug
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

Das Repository enthält derzeit keinen vollständigen Gradle Wrapper. Verwende für
lokale Builds daher Gradle 8.10 aus einer vertrauenswürdigen Distribution; CI
bleibt der festgelegte Referenzbuild. Ein Wrapper-JAR wird erst ergänzt, wenn es
aus der offiziellen Gradle-Distribution bezogen und seine Prüfsumme verifiziert
werden kann.

Das APK ist ein Debug-Build und nicht für eine produktive Verteilung signiert.
Vor einer Übergabe müssen Kiosk, Sprache, Offline-Fallback, Lernprofil-Löschung,
Einwilligungs-Widerruf, WLAN-Admin, Backup/Restore und Wiederanlauf auf dem
Zieltablet manuell geprüft werden. Modelle und native Bibliotheken benötigen
zusätzlich eine geklärte Lizenz, Herkunft und verifizierte Prüfsumme.
