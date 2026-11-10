# Drzemka: analiza i propozycja rozwoju

Stan analizy: 9 listopada 2026. Poniższe funkcje są propozycją, nie opisem gotowej implementacji.

Aktualizacja 10 listopada 2026: implementacja Androida jest w `android/`. Ten dokument zachowuje historię analizy; aktualny, wykonany zakres opisuje [README](../README.md). Wersja Android używa natywnego audio i opcjonalnego Accessibility, bez rozszerzenia przeglądarki i bez monitorowania przez UsageStats.

## Uzgodniony zakres: Android, PL i EN

Aktualizacja po doprecyzowaniu przez użytkownika: jedyną platformą docelową jest Android. Obserwacja ma obejmować korzystanie z innych aplikacji na telefonie. Poniższe wcześniejsze warianty web/rozszerzenie/desktop pozostają zapisem analizy, a rekomendacja rozszerzenia jest nieaktualna. Nadal obowiązuje etap analizy i propozycji przed budową.

### Proponowana architektura

Natywna aplikacja w Kotlin i Jetpack Compose, z natywnym odtwarzaniem, obsługą sesji multimedialnej, alarmów i powiadomień. Obecny projekt React pozostaje referencją wyglądu. Kod Androida należy umieścić w osobnym katalogu, bez naruszania integracji Lovable. To rekomendacja architektoniczna, nie dokonana migracja.

- `UsageStatsManager` dostarcza statystyki użycia aplikacji po przyznaniu dostępu. Sam nie wykrywa przewijania.
- Opcjonalny `AccessibilityService` odbiera zdarzenia przewijania z obsługujących je aplikacji. Ograniczyć nasłuch do wybranych pakietów i nie pobierać tekstów ani drzewa widoków. Nie można obiecać identycznej skuteczności we wszystkich aplikacjach.
- Bez dostępu do zdarzeń przewijania dostępny wariant oparty na czasie używania. Interfejs musi uczciwie wskazywać aktywny tryb.
- Propozycje odpoczynku jako zwykłe powiadomienia z akcjami, bez wymuszania otwierania ekranu nad innymi aplikacjami. Widoczność zależy również od ustawień kanału, uprawnień i trybu Nie przeszkadzać.
- Audio i pobudka działają w warstwie natywnej. Projekt musi uwzględnić ograniczenia pracy w tle, uprawnienia alarmów, przerwania audio i blokadę ekranu. Weryfikacja na fizycznym telefonie jest częścią odbioru.
- Dane o aktywności przetwarzane lokalnie, opcja zatrzymania obserwacji i usunięcia liczników. Bez automatycznego rozpoznawania zmęczenia jako faktu.

Dystrybucja przez Google Play z AccessibilityService wymaga deklaracji zastosowania oraz odpowiedniego objaśnienia i zgody w aplikacji. Akceptacja nie jest gwarantowana. Nie oznaczać aplikacji jako narzędzia dla osób z niepełnosprawnościami, jeśli nie jest to jej głównym celem.

### Dwa języki od pierwszej implementacji

### Aktualizacja wymagań pobudki: tryb dla śpiochów

Użytkownik wymaga opcji bardzo stanowczego i głośnego wybudzania. To wymaganie zastępuje wcześniejsze założenie, że każda pobudka musi być łagodna, oraz limit głośności 0,5 w trybie dla śpiochów. Dźwięki do zasypiania nadal pozostają spokojne.

- Dwa profile: „Łagodny” / “Gentle” oraz „Dla śpiochów” / “Heavy sleeper”.
- Tryb dla śpiochów: mocny, powtarzany sygnał alarmowy, krótki wzrost do wybranej głośności i powtarzana wibracja, jeśli urządzenie ją obsługuje.
- Osobna regulacja pobudki, niezależna od głośności tła drzemki. Przycisk testowania alarmu przed rozpoczęciem sesji, uruchamiany świadomie przez użytkownika.
- Wyraźny ekran pobudki z potwierdzeniem „Już nie śpię” / “I'm awake”. Samo otwarcie powiadomienia nie zatrzymuje alarmu. Dostępna, świadoma akcja zatrzymania również w powiadomieniu.
- Opcjonalna drzemka dodatkowa 2 minuty, z konfigurowalnym limitem powtórzeń. Alarm ma skończony, udokumentowany limit czasu działania jako zabezpieczenie przed pozostawieniem telefonu bez opieki; wartość do ustalenia w implementacji.
- Zapisywać wybrany profil, głośność i ustawienia drzemki. Testować oba profile oraz ich tłumaczenia PL/EN.
- Nie obiecywać obejścia wyciszenia systemowego, Nie przeszkadzać ani ograniczeń urządzenia. Przed sesją pokazać istotny stan konfiguracji alarmu. Zweryfikować zachowanie z głośnikiem, słuchawkami i Bluetooth na rzeczywistym Androidzie.

### Lokalizacja

- Polski (`pl`) i angielski (`en`), domyślnie język systemu, angielski jako fallback.
- W ustawieniach wybór: systemowy, polski, English. Wybór trwały po ponownym uruchomieniu.
- Lokalizacja wszystkich ekranów, nazw scen, błędów, opisów uprawnień, kanałów i treści powiadomień, akcji, etykiet dostępności oraz pobudki.
- Zasoby Android `values/strings.xml` dla EN oraz `values-pl/strings.xml` dla PL; prawidłowe liczby mnogie i formatowanie czasu. Bez tekstów zaszytych w komponentach.
- Przykład PL: „Jesteś zmęczony? To może power nap i do roboty?”. EN: “Feeling tired? Take a power nap and get back to your day.”
- Test kompletności tłumaczeń, zmiany języka i układów z dłuższymi tekstami w obu językach.

### Dokumentacja końcowa i About repozytorium

Po implementacji uzupełnić README po polsku i angielsku: rzeczywiście dostępne funkcje, instalacja APK, budowanie, uprawnienia i prywatność, ograniczenia, testy oraz licencje audio. Zrzuty ekranu mają pochodzić z uruchomionej aplikacji, osobno PL i EN: start, biblioteka dźwięków, aktywna drzemka, sugestia przerwy i ustawienia. Umieścić je w `docs/screenshots/pl` i `docs/screenshots/en`, z podpisami i tekstem alternatywnym w odpowiednim języku. Nie zastępować zrzutów renderami koncepcyjnymi.

Repozytorium: https://github.com/pi0trdotsys/power-nap-studio. Po zakończeniu zaktualizować również jego rzeczywiste pole About, nie tylko README. Proponowany zwięzły opis dwujęzyczny: „Drzemki i spokojne dźwięki. Mniej scrollowania na Androidzie. / Power naps, calming sounds and mindful scrolling on Android. PL/EN.” README pomieści zrzuty i pełny opis, pole About służy krótkiej charakterystyce. Nie oznaczać planowanych funkcji jako gotowych.

Źródła dla zakresu Android:
- [Android: UsageStatsManager](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [Android: AccessibilityEvent](https://developer.android.com/reference/android/view/accessibility/AccessibilityEvent)
- [Google Play: AccessibilityService](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Android: uprawnienia powiadomień](https://developer.android.com/develop/ui/views/notifications/notification-permission)
- [Android: planowanie alarmów](https://developer.android.com/develop/background-work/services/alarms/schedule)

## Stan projektu

- React 19, TanStack Start, Tailwind 4. Gotowy spójny ekran w granacie, brudnej bieli i pomarańczu.
- `useNapMock` zmienia fazy, ale nie uruchamia audio ani zegara. Parametr `phase` służy do prezentacji makiety.
- Sześć pozycji w katalogu dźwięków nie ma źródeł audio. W `public` nie ma nagrań.
- Nie ma zapisu ustawień, powiadomień, integracji z ekranem blokady ani obserwacji przewijania.
- Panel dźwięków wymaga pułapki fokusu i Escape. Grupy wyboru wymagają obsługi strzałek. Zegar powinien ogłaszać czytnikowi minuty, nie sekundy.
- Jedyny test sprawdza dopasowanie trasy. Próba `npm test` zakończyła się brakiem Vitest, ponieważ nie zainstalowano zależności.

## Trzy warianty

| Wariant | Zakres | Ograniczenie |
| --- | --- | --- |
| 1. Spokojna drzemka | Działająca aplikacja webowa, dźwięki, zegar, pobudka, ustawienia i ikona | Widzi przewijanie tylko we własnej stronie |
| 2. Towarzysz przeglądarki | Wariant 1 oraz rozszerzenie wykrywające długie przewijanie na wybranych stronach | Wymaga instalacji rozszerzenia i dostępu do wybranych witryn |
| 3. Towarzysz komputera | Wariant 1 oraz aplikacja desktopowa z integracją systemową | Osobna dystrybucja, uprawnienia i testy dla każdego systemu |

Rekomendacja: najpierw wspólny rdzeń wariantu 1, potem wariant 2, jeśli sugestia ma pojawiać się podczas korzystania z innych stron. Samo przewijanie jest sygnałem do propozycji przerwy, nie dowodem zmęczenia.

## Projekt wizualny i audio

Zachować obecny ekran i paletę. Ikona: pomarańczowe słońce schowane częściowo za kremową linią horyzontu, granatowe zaokrąglone pole. Przygotować SVG, favicon oraz eksporty PNG do instalacji i powiadomień. Znak musi być czytelny również w 16 i 32 px.

Biblioteka: samolot (spokojna kabina), pociąg (równy rytm kół), łąka, las, deszcz, burza, morze, sztorm, kominek, wiatr, szum biały i brązowy. Burza i sztorm powinny mieć wyrównaną dynamikę, bez nagłych głośnych uderzeń. Nagrania wymagają udokumentowanej licencji umożliwiającej dystrybucję. Dźwięki syntetyczne należy wyraźnie oznaczyć. Szumy można generować przez Web Audio.

Każde nagranie sprawdzić odsłuchowo, przygotować bezszwową pętlę i wyrównać odczuwaną głośność. Ładować wybraną scenę, bez pobierania całej biblioteki na wejściu. Błąd odtwarzania powinien być widoczny, z możliwością ponowienia.

## Rdzeń implementacji

`useNap(): NapController` zarządza stanem i wywołuje pomocnicze moduły audio, zapisu oraz integracji przeglądarki. Komponenty pozostają prezentacyjne zgodnie z AGENTS.md.

1. Timer oparty na czasie zakończenia, z poprawnym zatrzymaniem i wznowieniem oraz synchronizacją po powrocie do karty.
2. Audio startowane gestem użytkownika, fade-in 8 s, pauza 1,5 s, crossfade scen 2 s. Przez ostatnią minutę wyciszenie do 30%, potem delikatna pobudka narastająca 10 s.
3. Walidowany zapis długości, sceny i głośności w localStorage. Obsłużyć brak dostępu do magazynu i uszkodzone dane.
4. Wake Lock, jeśli dostępny, oraz Media Session dla sterowania odtwarzaniem. Zwolnienie zasobów przy zakończeniu i odmontowaniu.
5. Dostępny panel dźwięków, komunikaty błędów i ograniczone ogłaszanie zegara.

## Sugestia odpoczynku

Proponowany domyślny mechanizm po włączeniu przez użytkownika: co najmniej 20 minut aktywności i przewijanie w kilku różnych minutach ostatniego pięciominutowego okna. Zliczać krótkie przedziały aktywności, zamiast każdego zdarzenia. Nie traktować jednego ruchu kółkiem jako powodu do komunikatu.

Komunikat: „Jesteś zmęczony? To może power nap i do roboty?”. Akcje: „Drzemka 15 min”, „Za godzinę”, „Nie dzisiaj”. Nie uruchamiać sesji automatycznie. Limit jednej sugestii na godzinę, wyłączenie podczas drzemki, respektowanie godzin ciszy i odroczenia również po odświeżeniu.

Do tego celu wystarczą lokalne liczniki czasu i przewijania. Odczytywanie tekstów, zrzutów ekranu czy wpisywanych danych nie jest potrzebne. Rozszerzenie powinno działać wyłącznie na witrynach wybranych przez użytkownika. Pełne rozumienie treści ekranu byłoby odrębną funkcją wymagającą osobnego zakresu.

## Powiadomienia i ograniczenia platformy

- O zgodę pytać po kliknięciu „Włącz powiadomienia”. Obsłużyć zgodę, odmowę i brak wsparcia; zachować komunikat w aplikacji jako alternatywę.
- Rozdzielić powiadomienie o końcu drzemki i sugestię przerwy. Kliknięcie powinno otwierać aplikację, bez automatycznego startu audio.
- Dla mobilnych powiadomień użyć service workera. Sam service worker nie jest niezawodnym harmonogramem alarmu.
- Przeglądarki ograniczają wykonywanie timerów w tle. Obliczanie czasu z zegara poprawia stan po wznowieniu, ale nie gwarantuje pobudki przy uśpieniu urządzenia lub zamknięciu aplikacji. Gwarancje alarmu należy ustalić osobno dla platformy natywnej.

Źródła techniczne:
- [MDN: Notifications API](https://developer.mozilla.org/en-US/docs/Web/API/Notifications_API/Using_the_Notifications_API)
- [MDN: ograniczenia kart w tle](https://developer.mozilla.org/en-US/docs/Web/API/Page_Visibility_API)
- [Chrome: skrypty rozszerzeń](https://developer.chrome.com/docs/extensions/develop/concepts/content-scripts)
- [MDN: przechwytywanie ekranu i zgoda użytkownika](https://developer.mozilla.org/en-US/docs/Web/API/MediaDevices/getDisplayMedia)

## Kryteria odbioru

- Testy przejść stanów, skoku czasu, pauzy, wznowienia i pojedynczego wybudzenia.
- Testy odczytu i zapisu ustawień, odmowy powiadomień, limitów sugestii i wyłączenia obserwacji.
- Test błędu audio oraz zmiany sceny i głośności w trakcie sesji.
- Build produkcyjny, TypeScript i sprawdzenie klawiatury, mobilnego panelu, ograniczonego ruchu oraz odtwarzania w docelowych przeglądarkach.
- Odsłuch wszystkich pętli i pobudki. Test blokady ekranu na rzeczywistych urządzeniach; udokumentowane ograniczenia.

Implementacja nie wymaga obecnie zmian schematu bazy ani sekretów. W razie późniejszego dodania synchronizacji lub push zmiany bazy należy zlecić Lovable zgodnie z zasadami repozytorium.
