# Abnahmekriterien

## Automatisierbar

- [x] GitHub Actions lädt Gradle 8.10 nur nach erfolgreicher offizieller SHA-256-Prüfung.
- [x] JVM-Tests laufen vor dem APK-Build.
- [x] Android-NDK baut `lia_llama` für arm64-v8a und x86_64.
- [x] Debug-APK plus SHA-256 werden als CI-Artefakt veröffentlicht.
- [ ] Modellimport legt nur eine privat kopierte Datei ab und aktiviert sie nur nach Prüfung des im Katalog gepinnten SHA-256.
- [x] Backupformat round-tripped Profil und bestätigtes Wissen; falsche Passphrase, Manipulation und ungültige Nutzdaten werden abgewiesen.
- [ ] Wiederherstellung validiert den gesamten Inhalt vor dem Schreiben; ungültige Sicherungen verändern keine vorhandenen Daten.
- [ ] Ein fehlendes, falsches oder nicht ladbares Modell lässt die App im lokalen Grundmodus weiterlaufen.
- [ ] Websuche ist standardmäßig aus, verlangt verständliche Einwilligung und lässt sich widerrufen.
- [ ] Mikrofonfehler und verweigerte Berechtigung lassen Texteingabe verfügbar.
- [ ] Admin-Sitzung wird beim Verlassen gesperrt; die PIN-Eingabe ist bei Rückkehr verfügbar.
- [ ] Lern- und bestätigte Wissensdaten bleiben verschlüsselt, versioniert und löschbar.

CI-Nachweis: [Android build #361](https://github.com/WeA200675/Lia-Android-Standalone/actions/runs/37226647705), Head `a91e8017abf8076bd398dd302ae30f2ce7cf7cd5`. APK-SHA-256: `539607eabc1a68d28c17c6a59da4800f9b762533e0b00ff2d111a43cd7d6920b`.\n\n## Emulator oder echtes Gerät\n
- [ ] Qwen3 lädt und antwortet vollständig offline.
- [ ] Startzeit, Antwortlatenz, Speicherverbrauch, Abbruch und Wiederstart sind akzeptabel.
- [ ] Kiosk, WLAN-Wechsel, Audio, Rotation und Berechtigungsdialoge funktionieren.
- [ ] Thermische Drosselung, Absturzverhalten und Wiederanlauf sind beobachtet.
- [ ] Portabler Backup-/Restore-Ablauf und Fehler-Injection funktionieren auf Emulator/Zieltablet.
- [ ] Backup lässt sich auf einem zweiten Gerät mit der richtigen Passphrase wiederherstellen; falsche Passphrase erhält vorhandene Daten.

## Release

- [ ] Produktionssignatur, Digest und reproduzierbare Build-Informationen sind veröffentlicht.
- [ ] Drittanbieterhinweise und Qwen-Modelllizenz bleiben beim jeweiligen Artefakt erhalten.

Ein CI- oder JVM-Test ersetzt keinen realen Tablet-Nachweis. Das Modell wird
separat bereitgestellt und ist kein Bestandteil des Debug-APK.
