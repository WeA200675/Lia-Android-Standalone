undefined

## Erweiterbarer Modellspeicher

Modelle werden zunächst im privaten internen App-Speicher gesucht. Wenn dort weniger als 1 GiB frei ist, verwendet Lia den app-eigenen externen Speicher nur dann, wenn Android ihn als eingehängt meldet. Es werden keine öffentlichen Verzeichnisse und keine beliebigen SD-Kartenpfade verwendet. Jede Datei bleibt vor der Nutzung SHA-256-geprüft; fehlt die Datei oder stimmt der Hash nicht, bleibt der Offline-Fallback aktiv.
