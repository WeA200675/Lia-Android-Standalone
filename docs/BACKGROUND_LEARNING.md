# Hintergrundlernen

```kotlin
DailyLearningWorker.schedule(this)
```

Die eindeutige WorkManager-Aufgabe läuft stündlich, benötigt kein Netzwerk und wird bei niedrigem Akku pausiert. Sie erzeugt keine Benachrichtigungen und startet keine Sprachausgabe; die Nutzerin sieht Impulse erst bei der nächsten normalen Interaktion.


## Resilienz und Begrenzung

Die stündliche Aufgabe ist idempotent: Existiert bereits ein Plan für den aktuellen Kalendertag, wird er unverändert behalten. Nach einem Geräte- oder App-Neustart wird der gespeicherte Plan wiederverwendet; erst am nächsten Tag wird genau ein neuer Plan erzeugt. Temporäre Speicher- oder Verschlüsselungsfehler führen zu einem exponentiellen Retry mit 15 Minuten Mindestabstand statt zu einem stillen Verlust. Die Planung bleibt auf höchstens 25 Impulse pro Tag begrenzt.
