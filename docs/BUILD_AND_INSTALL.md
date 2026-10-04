# Bauen und Installieren

## Build

Voraussetzungen: Java 17, `curl` oder `wget`, `sha256sum` und `unzip`.
Der POSIX-Bootstrap `gradlew` lädt die offizielle Gradle-8.10-Distribution und
prüft sie gegen die veröffentlichte SHA-256-Prüfsumme. Er ist kein offizielles
Gradle-Wrapper-JAR. Unter Linux/macOS:

```sh
sh ./gradlew testDebugUnitTest
sh ./gradlew assembleDebug
sha256sum app/build/outputs/apk/debug/app-debug.apk
```

Für die native Bibliothek benötigt der Build Android SDK, NDK `27.2.12479018`
und CMake `3.22.1`. CMake holt llama.cpp vom festgelegten Commit und baut CPU-
Bibliotheken für `arm64-v8a` und `x86_64`. CI lädt das Debug-APK zusammen mit
seiner Prüfsumme hoch.

## Optionales lokales Modell

Das APK enthält keine Gewichte. Verwende den Modellimport in der App und die
offizielle Datei [Qwen3-0.6B-Q8_0.gguf](https://huggingface.co/Qwen/Qwen3-0.6B-GGUF/blob/main/Qwen3-0.6B-Q8_0.gguf).
Die App prüft beim Import den im Katalog gepinnten SHA-256; Dateien mit anderem
Inhalt werden verworfen. Der Download ist manuell und erfolgt nicht durch Lia.
Lizenz: Apache-2.0. Runtime-Lizenz und vollständige Hinweise stehen in
[THIRD_PARTY_NOTICES.md](../THIRD_PARTY_NOTICES.md).

## Portable Datensicherung

In der App kannst du das Lernprofil und bestätigtes lokales Wissen als
passwortgeschützte Datei exportieren oder auf einem anderen Gerät
wiederherstellen. Verwende mindestens 12 Zeichen und bewahre die Passphrase
getrennt von der Datei auf; sie kann nicht zurückgesetzt werden. Ungültige
Archive oder eine falsche Passphrase werden abgewiesen. Modellgewichte,
Geräteschlüssel und Einstellungen sind nicht Teil der Sicherung.

## Abnahmegrenzen

Das APK aus CI ist ein Debug-Build ohne Produktionssignatur. Samsung-Kiosk,
Audio, thermische Leistung, Backup/Restore und Fehler-Injection müssen auf dem
Zieltablet geprüft werden. Siehe [Abnahmekriterien](ACCEPTANCE.md).
