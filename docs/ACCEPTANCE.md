# Abnahmekriterien

## Heute automatisierbar

- [ ] GitHub Actions führt JVM-Tests vor dem APK-Build aus und veröffentlicht APK plus SHA-256 als ein CI-Artefakt.
- [ ] Websuche ist standardmäßig aus, wird erst nach verständlicher Einwilligung freigegeben und lässt sich sofort widerrufen.
- [ ] Netzwerk bleibt auch bei gespeicherter Freigabe für persönliche/unklare Fragen und für nicht unterstützte Webmodi gesperrt.
- [ ] Verweigerte Mikrofonberechtigung und Spracherkennungsfehler führen sichtbar zur jederzeit verfügbaren Texteingabe.
- [ ] Admin-Sitzung wird beim Verlassen gesperrt; PIN-Eingabe ist nach Rückkehr wieder verfügbar.
- [ ] Tagesfragen können übersprungen, lokal gespeichert und über die vorhandene Admin-Funktion gelöscht werden.
- [ ] Lern- und bestätigte Wissensdaten bleiben verschlüsselt, versioniert und löschbar.

## Nur mit echten Artefakten oder Zielgerät abnehmbar

- [ ] Lokales Modell antwortet ohne Cloud; das ausgelieferte Modell und die native Bibliothek haben geprüfte SHA-256-Werte und eine geklärte freie Lizenz.
- [ ] Alle logischen CPU-Kerne werden erkannt; thermische Drosselung ist auf dem Gerät beobachtet.
- [ ] Kiosk, WLAN-Wechsel, Audio und Rotation auf dem Zieltablet geprüft.
- [ ] Backup/Restore und Wiederanlauf nach Fehler-Injection auf dem Zieltablet durchgeführt.

Kein CI- oder JVM-Test ersetzt den realen Tablet-Nachweis. Fehlende Modelle,
native Bibliotheken, Signaturschlüssel oder Geräte werden nicht simuliert.
