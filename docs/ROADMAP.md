# Autarke Android-Roadmap

## Umsetzungsstand

| Reihenfolge | Meilenstein | Stand | Was noch nötig ist |
|---|---|---|---|
| 1 | Build- und CI-Grundlage | Bestehende JVM-/APK-CI; checksum-geprüfter Gradle-Bootstrap ergänzt | Neue CI muss den gepinnten NDK-/llama.cpp-Build erfolgreich durchlaufen. Es ist ein Bootstrap-Skript, kein offizielles Wrapper-JAR. |
| 2 | Admin- und Mikrofon-Flows | Code umgesetzt | Samsung-Abnahme für Admin-Rückkehr, WLAN und verweigerte Berechtigungen |
| 3 | Anonymisiertes Wissensgateway | Code und JVM-Policytests umgesetzt | Consent- und Datenfluss auf Android abnehmen |
| 4 | Tagesfragen und Erinnerungen | Kernlogik, Verschlüsselung und JVM-Tests umgesetzt | Neustart, Widerruf und Löschung auf Zielgerät abnehmen |
| 5 | Release und Recovery | Debug-APK-Digest in CI; MIT-Projektlizenz; passwortgeschützter Export/Wiederherstellung von Lernprofil und bestätigtem Wissen | Recovery-Fehler-Injection auf Android und private Produktionssignatur |
| 6 | Lokale KI | llama.cpp-JNI-Backend, gepinnter Qwen-Katalog, Nutzerimport mit SHA-256-Check, asynchrones Laden und Offline-Fallback ergänzt | NDK-CI-Build und Offline-Inferenz auf Emulator/Samsung; Gewichte werden separat heruntergeladen |
| 7 | App-Integration | Modellstatus und Importdialog integriert | Frischer CI-Build; Laufzeit, Speicherverbrauch und Fehlerszenarien auf Geräten |
| 8 | Wissensgateway-Abnahme | Fail-closed Policy und expliziter Widerruf im Code | Android-Netzwerkabläufe und Quellenanzeige abnehmen |
| 9 | Samsung-Gerät | **Wartet auf das Zielgerät** | Kiosk/WLAN, Audio, Rotation, Laufzeit und Thermik |
| 10 | Release-Härtung | Teilweise umgesetzt; portable verschlüsselte Sicherung im Code | Geräte-Fehler-Injection, reproduzierbare Produktionssignatur und Gerätebelege |

## Heutige Reihenfolge

1. CI/NDK-Build grün bekommen.
2. JVM-Tests für Modellkatalog, Import, Offline-Fallback und Backupformat ausführen.
3. Den passwortgeschützten Backup-/Restore-Code gegen CI-Build und ungültige Archive prüfen.
4. Emulatorprüfung durchführen, sofern Android-Emulator in CI verfügbar ist.
5. Nach Verfügbarkeit: Samsung-Abnahme, Recovery-Fehler-Injection und Release-Signatur.

Die Abnahmekriterien stehen in [ACCEPTANCE.md](ACCEPTANCE.md). Details zu lokaler KI
und Abhängigkeiten stehen in [ROADMAP_POINTS_6_10.md](ROADMAP_POINTS_6_10.md).
Ein Codepfad gilt erst nach erfolgreichem Build und den jeweiligen Tests als
technisch abgenommen. Der reale Gerätetest bleibt separat.
