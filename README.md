# Lia-Android-Standalone

Autarke Android-App namens „Lia“, eine geduldige deutschsprachige Alltagsbegleiterin.

## Aktueller Stand

Die App enthält eine Offline-Oberfläche, verschlüsselte Erinnerungen, Tagesfragen,
Spracheingabe/-ausgabe, ein standardmäßig deaktiviertes Wissensgateway und eine
lokale Modelllaufzeit mit Text-Fallback. Die native llama.cpp-Runtime ist in den
Android-Build integriert. Ein offenes Qwen3-Modell kann separat installiert und
wird nur nach SHA-256-Prüfung aktiviert.

## Prinzipien

- Persönliche Eingaben bleiben auf dem Gerät.
- Es gibt keine Cloud-Inferenz und keine automatische Modelldownloads.
- Webzugriff verlangt eine ausdrückliche, widerrufbare Einwilligung und ist auf
  allgemeine Wissensfragen begrenzt.
- Bestätigte Erinnerungen werden separat und verschlüsselt gespeichert.
- Projektcode steht unter MIT; llama.cpp und das optionale Qwen-Modell haben
  eigene Drittanbieterlizenzen. Siehe [Drittanbieterhinweise](THIRD_PARTY_NOTICES.md).

## Bauen und Abnahme

Siehe [Build-Anleitung](docs/BUILD_AND_INSTALL.md), [Abnahmekriterien](docs/ACCEPTANCE.md)
und [Roadmap](docs/ROADMAP.md). Modellgewichte sind nicht im Repository oder APK
enthalten. Geräte- und Releaseabnahmen bleiben offen, bis ihre Nachweise vorliegen.
