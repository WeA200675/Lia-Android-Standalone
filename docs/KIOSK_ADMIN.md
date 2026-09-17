# Kiosk- und Admin-Modus

Der normale Lia-Modus bleibt im Lock-Task. Ein mindestens sechsstelliger Admin-PIN wird nur als SHA-256-Hash gespeichert. Erst nach erfolgreicher PIN-Prüfung darf der Kiosk verlassen und die Android-Einstellungs-App geöffnet werden, um ein neues WLAN zu konfigurieren.

Die App darf WLAN nicht heimlich wechseln und darf den Admin-PIN nicht anzeigen. Auf Geräten ohne Device-Owner-Verwaltung muss der erste Kiosk-Start einmalig durch den Nutzer bestätigt werden.
