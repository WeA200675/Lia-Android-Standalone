# Phase 8: Anonymisiertes Wissensgateway

Netzwerkzugriff wird nur freigegeben, wenn alle sechs Nachweise vorliegen:

- Anfrage ist anonymisiert
- aktuelle Policy erlaubt den Zugriff
- Einwilligung ist dokumentiert
- Quellenprovenienz kann gespeichert werden
- Wissenspuffer ist begrenzt
- Offline-Kaskade ist verfügbar

Fehlt ein Nachweis, bleibt die lokale Antwort nutzbar und der Gateway-Zugriff
wird verweigert. Antworten dürfen keine Live-Quelle behaupten, wenn nur Cache
oder Offline-Wissen verwendet wurde. Die Prüfung erfolgt vor jedem Abruf.
