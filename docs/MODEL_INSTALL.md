# Modellinstallation auf Android

Modelle werden nicht automatisch aus dem Internet geladen. Ein Release muss die GGUF-Datei und ihre SHA-256-Prüfsumme separat bereitstellen. Die App kopiert nur Dateien in ihren privaten Modellordner und akzeptiert ausschließlich Hashes aus `model-manifest.json`.

Vor der Installation muss ausreichend freier Speicher vorhanden sein. Fehlt das Primärmodell oder ist es beschädigt, darf nur das verifizierte Recovery-Modell gestartet werden; sonst bleibt Lia im Offline-Textmodus.
