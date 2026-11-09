# Prywatność / Privacy

## Polski

Drzemka działa lokalnie na Androidzie. Nie wymaga konta, nie zawiera reklam ani analityki i nie deklaruje uprawnienia do internetu.

W pamięci aplikacji zapisuje wybrany język, ustawienia drzemki i alarmu, listę zaznaczonych aplikacji, ustawienia propozycji przerwy oraz bieżący stan sesji. Okno aktywnych minut przewijania pozostaje w pamięci procesu; nie powstaje historia odwiedzanych treści. Czas odroczenia propozycji jest zapisany, aby przetrwał ponowne uruchomienie aplikacji.

Opcjonalna usługa ułatwień dostępu odbiera typ zdarzenia, identyfikator aplikacji i czas. Przetwarza zdarzenia wyłącznie dla wybranych aplikacji, aby proponować odpoczynek po długim przewijaniu. Nie czyta tekstów, wiadomości, wpisywanych danych ani zrzutów ekranu. Nie korzysta z możliwości pobierania drzewa widoków. Dane nie są wysyłane na serwer.

Możesz wyłączyć propozycje w aplikacji, wyłączyć usługę w ustawieniach dostępności Androida lub usunąć liczniki aktywności. Wyłączenie propozycji usuwa bieżące okno obserwacji przy następnym zdarzeniu; żadne nowe zdarzenia nie są wtedy liczone. Usunięcie danych aplikacji lub jej odinstalowanie usuwa ustawienia. Kopie zapasowe i transfer danych aplikacji są wyłączone w konfiguracji Androida.

Powiadomienia oraz dokładne alarmy wymagają odpowiednich zezwoleń systemowych. Aplikacja nie diagnozuje zmęczenia ani zaburzeń snu.

Ten dokument opisuje implementację w repozytorium. Przed dystrybucją w sklepie wydawca powinien dodać dane kontaktowe i publiczny adres polityki prywatności oraz wypełnić zgodne z implementacją deklaracje Google Play.

## English

Drzemka runs locally on Android. It requires no account, includes no advertising or analytics, and declares no internet permission.

The app stores language, nap and alarm preferences, selected app identifiers, break reminder settings and the current session state. The active-scroll-minute window stays in process memory; it does not create a browsing-content history. Reminder cooldown timestamps are persisted so they survive restarts.

The optional Accessibility service receives event types, app identifiers and timestamps. It processes events only for selected apps to suggest rest after sustained scrolling. It does not read screen text, messages, typed input or screenshots and cannot retrieve the view hierarchy. Activity data is not sent to a server.

You can disable suggestions in the app, disable the service in Android Accessibility settings or clear activity counters. Disabling suggestions clears the observation window on the next event; no new events are counted while disabled. Clearing app data or uninstalling removes the stored preferences. Android backup and device transfer are disabled in the app configuration.

Notifications and precise alarms require the relevant system permissions. The app does not diagnose fatigue or sleep disorders.

This document describes the repository implementation. Before store distribution, the publisher should add contact details and a public privacy-policy URL and complete Google Play declarations matching the app’s behavior.
