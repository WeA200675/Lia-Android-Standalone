# Roadmap-Punkte 6–10

Diese Phasen werden nur dann als abgeschlossen markiert, wenn die genannten
Nachweise vorliegen. Fehlende native Bibliotheken, GGUF-Dateien, freie Lizenzen
oder Samsung-Hardware werden nicht simuliert.

| Punkt | Inhalt | Heutiger Stand | Abnahmekriterium |
|---|---|---|---|
| 6 | Native Android-LLM-Artefakte | **BLOCKED: Artefakte fehlen** | frei lizenzierte Modelle und ABI-Bibliotheken, Herkunft, echte SHA-256-Werte, Verifikation und Starttest |
| 7 | Vollständige App-Integration | **IN ARBEIT** | Main-/Admin-Flows, Lernfeedback, Offline-Fallback und verschlüsselte Zustände auf Emulator und Gerät geprüft |
| 8 | Anonymisiertes Wissensgateway | **IN ARBEIT** | standardmäßig offline; explizite widerrufbare Einwilligung, fail-closed Datenminimierung, Quellenprovenienz und Offline-Kaskade |
| 9 | Samsung-Abnahme | **BLOCKED: Zielgerät fehlt** | Android-16-Gerätetest für Kiosk/WLAN, Audio, Rotation, Berechtigungsablehnung und Wiederanlauf |
| 10 | Release- und Recovery-Härtung | **TEILWEISE UMGESETZT** | CI-Tests, Debug-APK-Digest, reproduzierbarer Build, Backup/Restore und Fehler-Injection; Produktionssignatur bleibt separat |

## Sicherheitsregeln

- Kein Netzwerkzugriff ohne Policy-Freigabe **und** gespeicherte Einwilligung.
- Nur allgemeine, anonymisierte Fragen dürfen die Online-Kaskade erreichen.
- Keine automatische Freigabe ungeprüfter Modelle oder nativer Bibliotheken.
- Modellgewichte, native Binaries und Schlüssel werden nicht eingecheckt, solange Lizenz und Herkunft nicht geklärt sind.
- Geräte- und Releasephasen bleiben offen, bis ein überprüfbarer Nachweis aus CI oder dem realen Zieltablet vorliegt.

Jeder CI-Lauf testet den Quellstand des jeweiligen Pushes oder Pull Requests.
