# Persönliches Lernprofil

Die Antworten aus den Tagesfragen werden mit AES-GCM verschlüsselt und im App-internen Speicher abgelegt. Der AES-Schlüssel liegt ausschließlich im Android Keystore.

Jede Antwort hat eine Version/Zeitmarke und den Status `confirmed`. Nur bestätigte Einträge dürfen in künftige Prompts einfließen. Die Bewohnerin kann einzelne Einträge bestätigen oder das gesamte Profil löschen.

Das Grundmodell wird nicht nachtrainiert. Dadurch bleiben Modellverhalten, Sicherheitstests und Wiederherstellung reproduzierbar; Lia lernt dennoch persönliche Vorlieben und Routinen über den separaten Speicher.
