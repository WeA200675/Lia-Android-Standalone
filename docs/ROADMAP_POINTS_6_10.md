# Roadmap-Punkte 6–10

Diese Phasen werden nur dann als abgeschlossen markiert, wenn die genannten
Nachweise vorliegen. Fehlende Samsung-Hardware und Produktionsschlüssel werden
nicht simuliert.

| Punkt | Inhalt | Implementiert | Noch offen |
|---|---|---|---|
| 6 | Lokale Android-KI | llama.cpp aus gepinntem MIT-Quellstand, JNI CPU-Inferenz, Qwen3 GGUF-Katalog mit Apache-2.0, Nutzerimport in App-Speicher und SHA-256-Verifikation | NDK-CI muss grün sein; Inferenz, Speicher/Hitze und echte Antwort auf Emulator und Zielgerät prüfen |
| 7 | App-Integration | Modellstatus, Importdialog, nativer Runtime-Pfad, Text-Fallback | CI-/Emulatorlauf, Start/Ladezeit, Lifecycle und Wiederherstellung testen |
| 8 | Anonymisiertes Wissensgateway | standardmäßig offline; explizite widerrufbare Einwilligung; generische Fragen als einzige Onlinefreigabe | Android-Netzwerkfluss, Quellenprovenienz und Widerruf praktisch abnehmen |
| 9 | Samsung-Abnahme | nicht begonnen, Zielgerät fehlt | Kiosk/WLAN, Audio, Rotation, Berechtigungsablehnung, Laufzeit und Thermik |
| 10 | Release- und Recovery-Härtung | CI-Digest für Debug-APK und Open-Source-Lizenzen | Backup/Restore, Fehler-Injection, reproduzierbarer Release, Signatur und Geräteevidence |

## Lokale KI: Herkunft und Auslieferung

- Runtime: [ggml-org/llama.cpp](https://github.com/ggml-org/llama.cpp), Commit
  `2e7c58c5477478c8cf6e199cfaa5dcd5a4319c81`, MIT.
- Modellkandidat: [Qwen3-0.6B-GGUF Q8_0](https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/blob/main/Qwen3-0.6B-Q8_0.gguf), Apache-2.0.
- SHA-256: `9465e63a22add5354d9bb4b99e90117043c7124007664907259bd16d043bb031`.
- Modellgewichte sind nicht im Git-Repository oder APK enthalten. Der Nutzer wählt die Datei selbst; die App kopiert sie privat und aktiviert sie nur nach Hashprüfung.
- Kein Modellnetzwerkzugriff und kein Cloud-Inferenzpfad. Persönliche Eingaben laufen durch lokale Inferenz oder den lokalen Grundmodus.
- Drittanbieterhinweise liegen in [THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md) und im App-Asset.

Jeder CI-Lauf prüft JVM-Tests und den für den jeweiligen Stand konfigurierten Android-Build.
