# Lokale Modelllaufzeit

Die App lädt Modelle ausschließlich aus dem lokalen App-Speicher. Vor der Nutzung wird die SHA-256-Prüfsumme gegen `model-manifest.json` verglichen. Fehlt das Modell oder stimmt der Hash nicht, bleibt Lia im sicheren Text-Fallback.

Die Zahl der Android-logischen CPUs wird für die Inferenz berücksichtigt. Das ist die Android-Entsprechung zur Nutzung von SMT/Hyperthreading; Android liefert keinen separaten SMT-Schalter. Temperatur, Akku und Speicherdruck dürfen die Threadzahl jederzeit reduzieren.

Automatische Downloads und unbestätigtes Nachtrainieren des Grundmodells sind ausgeschlossen.
