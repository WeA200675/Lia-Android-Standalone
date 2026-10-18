# Autarke Android-Roadmap

## Umsetzungsstand

| Reihenfolge | Meilenstein | Stand | Was noch nötig ist |
|---|---|---|---|
| 1 | Build- und CI-Grundlage | JVM-Tests, llama.cpp-NDK-Build, Debug-APK und SHA-256 erfolgreich (CI #361, Code-Head `a91e801`) | Weiterhin eigenes checksum-geprüftes Bootstrap-Skript statt offiziellem Wrapper-JAR. |
| 2 | Admin- und Mikrofon-Flows | Code umgesetzt | Samsung-Abnahme für Admin-Rückkehr, WLAN und verweigerte Berechtigungen |
| 3 | Anonymisiertes Wissensgateway | Code und JVM-Policytests umgesetzt | Consent- und Datenfluss auf Android abnehmen |
| 4 | Tagesfragen und Erinnerungen | Kernlogik, Verschlüsselung und JVM-Tests umgesetzt | Neustart, Widerruf und Löschung auf Zielgerät abnehmen |
| 5 | Release und Recovery | Debug-APK-Digest in CI; MIT-Projektlizenz; passwortgeschützter Export/Wiederherstellung von Lernprofil und bestätigtem Wissen; Format- und Integritätstests grün | Recovery-Fehler-Injection auf Android und private Produktionssignatur |
| 6 | Lokale KI | llama.cpp-JNI-Backend, gepinnter Qwen-Katalog, Nutzerimport mit SHA-256-Check, asynchrones Laden, Offline-Fallback und nativer CI-Build erfolgreich | Offline-Antwort, Laufzeit und Ressourcen auf Emulator/Samsung prüfen; Gewichte werden separat heruntergeladen |
| 7 | App-Integration | Importdialog, Modellstatus, asynchrones Laden und Text-Fallback; JVM und Debug-APK Build grün | Laufzeit, Lifecycle, Speicherverbrauch und Fehlerszenarien auf Geräten prüfen |
| 8 | Wissensgateway-Abnahme | Fail-closed Policy und expliziter Widerruf im Code | Android-Netzwerkabläufe und Quellenanzeige abnehmen |
| 9 | Samsung-Gerät | **Wartet auf das Zielgerät** | Kiosk/WLAN, Audio, Rotation, Laufzeit und Thermik |
| 10 | Release-Härtung | Teilweise umgesetzt; portable verschlüsselte Sicherung im Code | Geräte-Fehler-Injection, reproduzierbare Produktionssignatur und Gerätebelege |

## Heutige Reihenfolge

1. JVM-Tests, passwortgeschützte Backup-/Restore-Formatprüfungen und Debug-APK mit beiden nativen ABIs sind im CI-Lauf #361 erfolgreich.
2. Softwareseitige Meilensteine 1–8 sind implementiert; Android- und Emulatorabnahme bleiben offen.
3. Nach Verfügbarkeit des Samsung-Tablets: Admin/WLAN, Berechtigungen, Offline-Inferenz, Thermik und Recovery-Fehler-Injection abnehmen.
4. Für ein Release: private Produktionssignatur und reproduzierbare Build-Informationen ergänzen.

Die Abnahmekriterien stehen in [ACCEPTANCE.md](ACCEPTANCE.md). Details zu lokaler KI
und Abhängigkeiten stehen in [ROADMAP_POINTS_6_10.md](ROADMAP_POINTS_6_10.md).
Ein Codepfad gilt erst nach erfolgreichem Build und den jeweiligen Tests als
technisch abgenommen. Der reale Gerätetest bleibt separat.
