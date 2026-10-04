# Lia-Android-Standalone

Autarke Android-App namens „Lia“, eine empathische, geduldige und herzliche Alltagsbegleiterin für eine 84-jährige Dame.

## Aktueller Stand

Die App enthält eine große Offline-Oberfläche, tägliche Fragen, CPU/SMT-Erkennung, eine Keystore-Grundlage und eine lokale Modell-Laufzeitschnittstelle mit sicherem Text-Fallback. Die Websuche bleibt standardmäßig offline und benötigt eine ausdrückliche, widerrufbare Einwilligung.

## Prinzipien

- Kein PC erforderlich.
- Persönliche Antworten bleiben lokal und verschlüsselt.
- Internetzugriff erfolgt nur für freigegebene, allgemeine und anonymisierte Wissensfragen.
- Das Grundmodell wird nicht unkontrolliert selbsttrainiert; bestätigte Erinnerungen und Präferenzen werden separat gelernt.
- Modelle müssen vor Ausführung hash-geprüft werden.
- Modell- und native Runtime-Artefakte werden erst hinzugefügt, wenn Herkunft, SHA-256 und Lizenz nachgewiesen sind.

## Build und Abnahme

Siehe [Bauen und Installieren](docs/BUILD_AND_INSTALL.md), [Abnahmekriterien](docs/ACCEPTANCE.md) und [Roadmap-Punkte 6–10](docs/ROADMAP_POINTS_6_10.md). GitHub Actions führt JVM-Tests, APK-Build und Digest-Erzeugung aus. Das Debug-APK ist kein signierter Produktionsrelease.

## Lizenz

Projektcode und Dokumentation stehen unter MIT-Lizenz; siehe [LICENSE](LICENSE). Drittanbieter-Abhängigkeiten und später hinzugefügte Modellartefakte behalten ihre jeweiligen Lizenzen und müssen separat geprüft werden.
