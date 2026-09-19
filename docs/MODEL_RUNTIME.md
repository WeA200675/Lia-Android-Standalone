# Lokale Modelllaufzeit

## Erweiterbarer Modellspeicher

Modelle werden zunächst im privaten internen App-Speicher gesucht. Wenn dort weniger als 1 GiB frei ist, verwendet Lia den app-eigenen externen Speicher nur dann, wenn Android ihn als eingehängt meldet. Es werden keine öffentlichen Verzeichnisse und keine beliebigen SD-Kartenpfade verwendet.

Die Laufzeit bewertet den gewählten Speicher zusätzlich vor jeder Nutzung:

- HEALTHY: mindestens 512 MiB frei.
- LOW_SPACE: weniger als 512 MiB frei; Lia warnt, lädt aber kein unbestätigtes Modell.
- UNAVAILABLE: keine verwertbare Kapazität; Lia bleibt im sicheren Offline-Fallback.

Die Bewertung ist nur eine Diagnose- und Schutzschicht. Sie ersetzt niemals die SHA-256-Prüfung aus model-manifest.json. Ein Modell wird weiterhin nur als VerifiedModel an den nativen Adapter übergeben, wenn Datei, Dateiname, Modell-ID, Ressourcenanforderung und Hash passen.

Die Speichergröße darf mit app-eigenem externem Speicher wachsen, aber niemals durch Zugriff auf öffentliche oder fremde Verzeichnisse. Ein voller Speicher führt zu einer verständlichen Warnung und zum vorhandenen Offline-/Recovery-Verhalten, nicht zu automatischen Downloads oder Löschungen.

## Laufzeit-Sicherheit

Die Laufzeit versucht zuerst das Primärmodell und danach genau einmal das Recovery-Modell. Fehler reduzieren ein begrenztes Restart-Budget; bei erschöpftem Budget wird BLOCKED gesetzt. Es gibt keine Endlosschleife.

Die Zahl der Android-logischen CPUs wird für die Inferenz berücksichtigt; das ist die Android-Entsprechung zur Nutzung von SMT/Hyperthreading. Temperatur, Akku und Speicherdruck dürfen die Threadzahl jederzeit reduzieren.

Automatische Downloads und unbestätigtes Nachtrainieren des Grundmodells sind ausgeschlossen.
