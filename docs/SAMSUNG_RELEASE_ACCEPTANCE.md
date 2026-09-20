# Phasen 9+10: Samsung-Abnahme und Release-Härtung

Die Freigabe benötigt sieben überprüfbare Nachweise:

1. Android-16-Version auf dem Zielgerät
2. Kiosk- und WLAN-Konfiguration
3. Audio, Sprache und Rotation
4. sicherer Neustart nach Prozessabbruch
5. APK-Signatur und Digest
6. verschlüsselter Backup-/Restore-Drill
7. kontrollierte Fehler-Injektion mit Offline-Fallback

Fehlt ein Nachweis, bleibt die Veröffentlichung BLOCKED. CI kann JVM- und APK-
Nachweise liefern; die vier Geräteprüfungen müssen zusätzlich auf dem echten
Samsung-Tablet ausgeführt und dokumentiert werden. Keine Hardwareabnahme wird
durch Testdaten vorgetäuscht.
