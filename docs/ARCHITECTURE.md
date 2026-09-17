# Lia Android Standalone

## Ziel
Eine vollständig lokale Android-Begleiterin für eine ältere Nutzerin.

## Sicherheitsmodell
Das Grundmodell wird nicht automatisch umtrainiert. Tägliche Antworten landen nach Einwilligung in einem verschlüsselten, versionierten Lernprofil. Nur bestätigte Einträge beeinflussen den Dialog. Webanfragen werden vor dem Versand redigiert und sind standardmäßig deaktiviert.

## CPU/SMT
`CpuProfiles.detect()` nutzt alle von Android gemeldeten logischen Prozessoren. Die finale Inferenzschicht erhält zusätzlich Temperatur-, Akku- und Speicherschutzgrenzen.

## Kiosk/WLAN
Der Admin-Modus wird nur nach PIN-Freigabe geöffnet. WLAN-Einstellungen sind niemals aus dem normalen Bedienmodus erreichbar.
