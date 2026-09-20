# Integrations-Gates für die Punkte 1–5

## Native Android-LLM

Die JNI-Grenze ist implementiert. Der Status wird nur dann READY, wenn Bibliothek, lokales Modell und SHA-256-Prüfung vorhanden sind. Fehlt ein Artefakt, bleibt der Offline-Fallback aktiv. Es werden keine Modelle automatisch heruntergeladen.

## Feedback und Admin

Feedback darf erst als integriert gelten, wenn die UI es an den verschlüsselten Lernservice übergibt. Entwicklungsstufen dürfen im Admin-Bereich nur nach PIN-Freigabe sichtbar und zurücksetzbar sein.

## Wissens-Gateway

Jede Quelle muss Provenienz liefern oder als unbestätigte Referenz behandelt werden. Bei Fehlern übernimmt die lokale/offline Kaskade.

## Samsung-Abnahme

Die echte Abnahme bleibt ein Geräteschritt: Android 16, Spracheingabe/-ausgabe, Kiosk-Modus und WLAN-Wechsel müssen auf dem Samsung-Gerät geprüft werden. Ein grüner JVM-Test ersetzt diese Abnahme nicht.

Die Readiness-Evaluierung dokumentiert diese Grenzen maschinenlesbar und verhindert, dass fehlende Artefakte als fertig ausgegeben werden.
