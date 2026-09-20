# Phase 6+7: Native KI und App-Integration

Die beiden Phasen werden gemeinsam freigegeben. Der Startvertrag prüft sechs
Nachweise: SHA-256-Prüfung des GGUF-Modells, Integrität der nativen Bibliothek,
gesunde native Sitzung, verfügbarer Offline-Fallback, verschlüsselte
Feedbackspeicherung und geschützter Admin-Lernstatus.

Erst wenn alle Nachweise vorliegen, darf die Oberfläche native Bereitschaft
anzeigen. Fehlen native Artefakte, bleibt Lia im sicheren lokalen Offline-Modus.
Ein fehlender Offline-Fallback blockiert die gesamte Freigabe.

Die Klasse NativeAppIntegrationGate ist zustandsfrei und in JVM-Tests prüfbar.
Die tatsächliche native Bibliotheks-/GGUF-Abnahme bleibt ein separater Build-
und Gerätetest und wird nicht durch Testdaten simuliert.
