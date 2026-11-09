# Android architecture / Architektura Androida

## PL

Natywna aplikacja znajduje się w `android/`. Oryginalny projekt React/TanStack pozostaje referencją wizualną i nie jest warstwą wykonawczą Androida.

- `MainActivity.kt`: prezentacja Jetpack Compose, semantyka dostępności i nawigacja.
- `NapController.kt`: akcje interfejsu, aktualny stan ustawień i uprawnień.
- `Store.kt`: lokalne ustawienia, wybrany język i trwały stan sesji. Kopia zapasowa Androida jest wyłączona.
- `Model.kt`: obliczanie czasu i detektor aktywnych minut przewijania, niezależne od UI.
- `NapService.kt`: foreground service typu mediaPlayback, zarządzanie audio focus, sesją multimedialną, blokadą CPU i pobudką.
- `Notifications.kt`: kanały i akcje powiadomień, AlarmManager i odbiorniki.
- `AudioEngine.kt` / `Synth.kt`: strumieniowa synteza PCM 22,05 kHz. Bez plików zewnętrznych i pobierania nagrań. Sceny są proceduralne, nie są autentycznymi nagraniami terenowymi.
- `ScrollService.kt`: opcjonalna usługa Accessibility. Odbiera wyłącznie zdarzenia przewijania i zmiany okna. Nie pobiera drzewa widoków ani tekstu zdarzeń.

Sesja drzemki przechowuje termin końca, pozostały czas po pauzie oraz token alarmu. `setAlarmClock` uruchamia pobudkę również po utracie procesu aplikacji, jeśli Android nadal ma zaplanowany alarm i pozwala go wykonać. Odbiornik weryfikuje token i stan. Zatrzymanie i pauza anulują zaplanowany alarm. Ponowne uruchomienie urządzenia przerywa sesję; przy następnym otwarciu aplikacja wyjaśnia przerwanie. Nie uruchamiamy automatycznie audio po restarcie telefonu.

Tryb dla śpiochów używa powtarzanego dwutonowego sygnału, narastania przez 2 sekundy oraz wibracji. Łagodny dzwonek narasta 10 sekund. Oba kończą się po świadomym zatrzymaniu lub po 10 minutach; test trwa 5 sekund. Dodatkowa drzemka trwa 2 minuty i jest dostępna raz w sesji. Nie zmieniamy systemowej głośności alarmów ani ustawień Nie przeszkadzać. Głośność aplikacji mnoży głośność strumienia alarmowego Androida.

Detektor nie diagnozuje zmęczenia. Liczy różne minuty zawierające zdarzenie przewijania w zaznaczonych aplikacjach. Domyślny próg wynosi 20 takich minut, w tym minimum 3 w ostatnich 5 minutach. Przerwa ponad 5 minut lub przejście do niewybranej aplikacji zeruje okno. Sugestie mają godzinny limit, opcję odroczenia do jutra i opcjonalną ciszę 22:00–08:00 według czasu telefonu. „Za godzinę” oznacza wyciszenie sugestii przez godzinę, a nie bezwarunkowe wysłanie powiadomienia po godzinie. Nie ma ukrytego monitoringu opartego na czasie używania; bez zgody na Accessibility obserwacja nie działa.

Google Play wymaga deklaracji Accessibility oraz usług pierwszoplanowych. Aplikacja pokazuje zgodę przed skierowaniem do ustawień usługi. Przed publikacją należy wypełnić formularze Play Console, przygotować politykę prywatności i zweryfikować zgodność bieżącej dystrybucji. Nie deklarujemy `isAccessibilityTool=true`. Instalacja APK poza sklepem może wymagać dopuszczenia ograniczonych ustawień przez użytkownika.

## EN

The native app lives in `android/`. The existing React/TanStack app remains a design reference, not the Android runtime.

Compose renders the interface; `NapController` owns UI actions; `Store` persists local settings and session state. `NapService` owns foreground audio playback, audio focus, the media session and the CPU wake lock. `AlarmManager.setAlarmClock` schedules the wake-up independently of UI ticks. The receiver validates the session token before starting the alarm. Pausing or stopping cancels the alarm. Rebooting interrupts the session; the next app launch reports this instead of restarting audio automatically.

All twelve soundscapes are original procedural synthesis, generated as streaming 22.05 kHz PCM. They are not field recordings. No internet permission, account, analytics SDK or third-party audio files are included.

Heavy sleeper mode uses repeating dual tones, a two-second rise and vibration. Gentle mode uses a ten-second chime rise. Alarms stop on explicit dismissal or after ten minutes. The preview lasts five seconds. One two-minute snooze is allowed. The app does not override Android alarm volume or Do Not Disturb.

The optional Accessibility service reads scroll/window-change event types, package identifiers and timestamps only. It does not retrieve screen text, typed input, screenshots or the view hierarchy. It counts active scroll minutes in selected apps. The default threshold is twenty minutes, with at least three active minutes in the last five. More than five minutes without scrolling or switching to an unselected app resets the window. Reminders have a one-hour cooldown, optional 22:00–08:00 quiet hours and a dismiss-until-tomorrow action. “In an hour” mutes suggestions for an hour rather than scheduling an unconditional notification. No monitoring runs without opt-in. This is a break heuristic, not fatigue detection.

Google Play distribution requires Accessibility and foreground-service declarations and review. The app includes prominent disclosure and affirmative consent. It does not claim to be a disability accessibility tool. Sideloaded APKs may require the user to allow restricted settings. Physical-device audio, Bluetooth, battery-management and lock-screen verification remain essential before relying on the alarm.

## References / Źródła

- [Android alarm scheduling](https://developer.android.com/develop/background-work/services/alarms)
- [Android notification permission](https://developer.android.com/develop/ui/views/notifications/notification-permission)
- [Google Play Accessibility policy](https://support.google.com/googleplay/android-developer/answer/10964491)
- [Foreground service declarations](https://support.google.com/googleplay/android-developer/answer/13392821)
