# Autarke Android-Roadmap

## Zuletzt umgesetzte Meilensteine (2026-10-04)

| Reihenfolge | Meilenstein | Status | Nachweis / offener Punkt |
|---|---|---|---|
| 1 | Build- und CI-Grundlage | **CI umgesetzt** | Ubuntu 24.04, Java 17, Gradle 8.10; Tests vor APK-Build; APK und SHA-256 werden als CI-Artefakt bereitgestellt. Der vollständige Gradle Wrapper fehlt noch, weil hier kein verifiziertes offizielles Wrapper-JAR vorliegt. |
| 2 | Admin- und Mikrofon-Flows | **Code umgesetzt; Geräteabnahme offen** | Admin-PIN-Eingabe wird beim Sperren wieder aktiviert; Berechtigungsablehnung und Speech-Fehler bleiben bei Texteingabe. WLAN-Rückkehr und Berechtigungsdialog auf Android-Gerät prüfen. |
| 3 | Anonymisiertes Wissensgateway | **Code und JVM-Tests umgesetzt** | Offline als Standard, explizite widerrufbare Einwilligung, generischer Modus als einzige Netzwerkfreigabe, persönlicher/unbekannter Kontext bleibt lokal. |
| 4 | Tagesfragen und Erinnerungen | **Kernlogik und JVM-Tests vorhanden; Geräteabnahme offen** | Speichern/Überspringen, verschlüsselte Ablage und Admin-Löschpfad sind implementiert. Ablauf nach Neustart sowie Widerruf/Löschung auf dem Zielgerät abnehmen. |
| 5 | Release- und Recovery-Paket | **Teilweise umgesetzt** | CI baut Debug-APK und SHA-256. Produktionssignatur, Backup/Restore und Fehler-Injection benötigen Schlüssel bzw. Zielgerät und bleiben offen. |

## Nächster Integrationsblock

- [ ] 6. Native Android-LLM-Artefakte
- [ ] 7. Vollständige App-Integration und Emulator-/Geräteabnahme
- [ ] 8. Anonymisiertes Wissensgateway vollständig abnehmen
- [ ] 9. Reales Samsung-Tablet-Abnahmeprogramm
- [ ] 10. Release- und Recovery-Härtung abschließen

Die Abnahmekriterien stehen in [ACCEPTANCE.md](ACCEPTANCE.md). Details zu Artefakt-
und Geräteabhängigkeiten stehen in [ROADMAP_POINTS_6_10.md](ROADMAP_POINTS_6_10.md).
Kein Modell, keine native Bibliothek und kein Hardwaretest wird als vorhanden
ausgegeben, solange der jeweilige Nachweis fehlt.
