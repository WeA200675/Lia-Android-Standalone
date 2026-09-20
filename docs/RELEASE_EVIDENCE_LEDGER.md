# Release-Evidence-Ledger

Jede Freigabe wird an einen vollständigen, nachvollziehbaren Nachweis gebunden:

- erfolgreicher CI-Lauf mit positiver Lauf-ID,
- echte 64-stellige SHA-256-Prüfsumme des APK,
- erfolgreicher Backup-/Restore-Test,
- ausgeführte Fehler-Injection mit Recovery-Nachweis,
- bestätigte Abnahme auf dem Samsung-Android-16-Gerät,
- UTC-Zeitstempel.

Fehlt ein einzelner Beleg, bleibt die Entscheidung **BLOCKED**. Platzhalter,
unbekannte Digests und reine Simulationen werden abgelehnt. Das Ledger ist ein
Entscheidungsvertrag; es ersetzt nicht die reale Geräteprüfung und führt keine
Hardwaretests vor.

Die Tests prüfen insbesondere, dass ein fehlender APK-Digest oder fehlender
Samsung-Nachweis niemals als releasefähig gilt.
