# Fehlerprävention vor jedem Merge

Diese Invarianten leiten sich aus den bisherigen Fehlerklassen ab.

## Verbindliche Reihenfolge

1. JVM-Tests müssen erfolgreich laufen.
2. Danach wird das Debug-APK gebaut.
3. Das APK muss als Artefakt hochgeladen werden.
4. Der Artefakt-Digest wird dokumentiert.
5. Merge erfolgt nur mit grünem Lauf und unverändertem PR-Head.

## Abgedeckte Fehlerklassen

- fehlende Hilfsfunktionen oder Imports: Kotlin-Kompilierung und Tests
- asynchrone Test-Hilfen: echte testDebugUnitTest-Ausführung
- Recovery-Endlosschleifen: Restart-Budget und BLOCKED-Tests
- Modellpfad-/Hash-Fehler: Manifest- und SHA-256-Tests
- ungültige Speichergrößen: Speicheraufnahme-Tests
- fehlender nativer Adapter: Offline-Fallback und JNI-Zustandsstatus
- korrupte Lernablage: sicherer Rückfall auf FOUNDATION
- veraltete PR-Basis oder Konflikte: PR-Head und CI vor Merge prüfen

Die statische Prüfung verify_safety_invariants.py ergänzt die JVM-Tests. Sie lädt keine Modelle und keine persönlichen Daten herunter.
