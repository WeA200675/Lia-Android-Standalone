# Roadmap-Punkte 6–10

Diese fünf Phasen bilden den nächsten Integrationsblock. Die App darf eine Phase
erst als abgeschlossen anzeigen, wenn die zugehörigen Nachweise wirklich
vorliegen. Fehlende native Bibliotheken, GGUF-Dateien oder Samsung-Hardware
werden nicht simuliert.

| Punkt | Inhalt | Nachweis |
|---|---|---|
| 6 | Native Android-LLM-Artefakte | signierte ABI-Artefakte, Modellmanifest, SHA-256-Prüfung und Starttest |
| 7 | Vollständige App-Integration | Main-/Admin-Flows, Lernfeedback, Offline-Fallback und verschlüsselte Zustände |
| 8 | Anonymisiertes Wissensgateway | Datenminimierung, Einwilligungs-/Policy-Prüfung, Quellenprovenienz und Offline-Kaskade |
| 9 | Samsung-Abnahme | Android-16-Gerätetest, Kiosk-/WLAN-Konfiguration, Audio, Rotation und Wiederanlauf |
| 10 | Release- und Recovery-Härtung | reproduzierbarer APK-Build, Signatur-/Digest-Prüfung, Backup/Restore und Fehler-Injektion |

## Sicherheitsregeln

- Kein Netzwerkzugriff ohne die bestehende Policy-Entscheidung.
- Keine persönlichen Daten im anonymisierten Gateway.
- Keine automatische Freigabe ungeprüfter Modelle oder nativer Bibliotheken.
- Hardware- und Artefaktphasen bleiben offen, bis ein überprüfbarer Nachweis
  aus CI oder dem realen Samsung-Gerät vorliegt.

Die Nachweise werden in CI erneut geprüft, bevor PR #116 zusammengeführt wird.
