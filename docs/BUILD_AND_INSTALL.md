# Bauen und Installieren

GitHub Actions baut bei jedem Push und Pull Request ein Debug-APK. Das Artefakt heißt `lia-debug-apk`.

Für die lokale Prüfung wird Java 17 und Gradle 8.10 benötigt:

```text
gradle assembleDebug
```

Das APK darf zunächst nur auf einem Testgerät installiert werden. Vor einer produktiven Übergabe müssen Kiosk, Sprache, Offline-Modell, Lernprofil-Löschung und WLAN-Admin auf dem Zieltablet manuell geprüft werden.
