# Kiosk- und Admin-Modus

Der normale Lia-Modus bleibt im Lock-Task. Ein Admin-PIN besteht aus 6 bis 12 Ziffern. Neue PINs werden mit einem zufälligen 128-Bit-Salt und PBKDF2-HMAC-SHA-256 (120.000 Iterationen) verifiziert; der PIN selbst wird nie gespeichert oder angezeigt.

Bestehende SHA-256-PIN-Hashes bleiben kompatibel, werden jedoch ausschließlich nach einer erfolgreichen PIN-Eingabe automatisch auf den gesalzenen PBKDF2-Verifier migriert.

Nach fünf falschen Eingaben wird die Admin-Freigabe für fünf Minuten gesperrt. Fehlversuche und Sperrzeit liegen persistent in privaten App-Einstellungen, sodass ein App-Neustart die Sperre nicht umgeht. Nach Ablauf ist ein neuer Versuch möglich; eine korrekte PIN setzt das Fehlversuchsbudget zurück.

Erst nach erfolgreicher PIN-Prüfung darf der Kiosk verlassen und die Android-Einstellungs-App geöffnet werden, um ein neues WLAN zu konfigurieren. Dauerhafte Löschaktionen benötigen zusätzlich einen zweiten, gleichartigen Tastendruck innerhalb von 30 Sekunden.

Die App darf WLAN nicht heimlich wechseln. Auf Geräten ohne Device-Owner-Verwaltung muss der erste Kiosk-Start einmalig durch den Nutzer bestätigt werden.

## Persönliche Inhalte und Sitzungsende

Lernprofil und bestätigtes Wissen werden im Admin-Bereich erst nach erfolgreicher
PIN-Prüfung aus der verschlüsselten Ablage gelesen und angezeigt.
Beim Verlassen des Admin-Bereichs (auch zu den WLAN-Einstellungen), beim Wechsel
in eine andere App und beim Pausieren der Activity verfällt die Freigabe.
Die Anzeige wird verdeckt, PIN und Budget-Eingabe werden geleert und offene
Löschbestätigungen verworfen. Bei der Rückkehr ist erneut die PIN erforderlich.

Tablet-Abnahme: Ein persönliches Lernprofil anlegen, Admin ohne PIN öffnen und
prüfen, dass keine Antworten sichtbar sind. Nach Freigabe zu WLAN wechseln und
zurückkehren: Inhalte müssen erneut gesperrt sein. Eine angefangene Löschbestätigung
darf den Wechsel nicht überleben. Auch Bildschirm-Aus/Ein und App-Wechsel prüfen.
