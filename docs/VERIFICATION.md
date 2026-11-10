# Verification / Weryfikacja

## Environment / Środowisko

- Windows, JDK 21, Android SDK 36, Gradle Wrapper 8.14.3.
- Emulator: Pixel 6, Android 15 / API 35, 1080 × 2400.
- Native application ID: `pl.powernap.studio`.
- Verification date / data weryfikacji: 10 listopada 2026 / 10 November 2026.

## Checks / Kontrole

- `python tools/check-locales.py`: 104 matching Polish/English string resources and formatting placeholders.
- `:app:assembleDebug`, `:app:assembleDebugAndroidTest`: APK and test APK built successfully.
- `:app:testDebugUnitTest`: 8 tests, zero failures. Deadline calculation, clock jumps, pause/resume, event flooding, sustained scroll, inactivity, quiet hours, opt-out, session suppression, cooldown and bounded audio synthesis for all 12 scenes.
- `:app:lintDebug`: zero errors, 38 warnings. Warnings include available dependency updates, KTX style suggestions, intentional synchronous preference writes and compatibility attributes. They are not suppressed globally.
- Android instrumentation: 3 tests passed, zero failures (`OK (3 tests)`, 120.366 seconds). Includes PL/EN sessions and notifications, alarm preview/persistence, and wake-up with the screen off. The notification screenshot capture also waits for the actual notification title before capture.

Polski: 8 testów jednostkowych oraz 3 testy na emulatorze przeszły bez błędów. Zweryfikowano pobudkę przy wygaszonym ekranie i fakt, że otwarcie aplikacji nie zatrzymuje alarmu. Android Lint: 0 błędów, 38 ostrzeżeń nieblokujących.

APK SHA-256: `60ed8648fe8d3289baa425d245a8254ba3d9d8e0b1cb28d214d8c85882b30f72`.

Build reports remain in ignored `android/app/build/reports/` and unit-test XML in `android/app/build/test-results/`.

## Reproduce device tests / Powtórzenie testów urządzenia

Use a dedicated emulator. The following commands install the development app and grant test permissions on that emulator. Normal users grant permissions through Android settings, not ADB.

Użyj osobnego emulatora. Poniższe polecenia instalują aplikację deweloperską i nadają uprawnienia testowe. Zwykły użytkownik przyznaje uprawnienia w ustawieniach Androida.

```sh
cd android
bash gradlew :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell pm grant pl.powernap.studio android.permission.POST_NOTIFICATIONS
adb shell appops set pl.powernap.studio SCHEDULE_EXACT_ALARM allow
adb shell am instrument -w -r pl.powernap.studio.test/androidx.test.runner.AndroidJUnitRunner
adb pull /sdcard/Android/data/pl.powernap.studio/files/screenshots/. ../docs/screenshots/
```

On Windows use `.\gradlew.bat` instead of `bash gradlew`.

The tests exercise PL/EN screens, actual foreground playback and session transitions, automatic wake-up, a single snooze, reminder deferral, alarm preview termination, persistence after activity recreation, and waking while the screen is off. Screenshots are captured from the running application and notification shade by Android UiAutomation. The emulator was launched without host audio output.

Testy obejmują ekrany PL/EN, rzeczywisty serwis audio i przejścia sesji, automatyczną pobudkę, jednorazową dodatkową drzemkę, odroczenie propozycji, zakończenie testu alarmu, zachowanie ustawień po odtworzeniu aktywności oraz pobudkę przy wygaszonym ekranie. Zrzuty zapisuje Android UiAutomation z działającej aplikacji i panelu powiadomień. Emulator działał bez odtwarzania dźwięku na komputerze.

## Not verified / Nieweryfikowane

- Perceived audio quality, real speaker loudness, Bluetooth/headphone routing and vibration on a physical phone.
- Manufacturer battery restrictions, Do Not Disturb combinations and event availability in every third-party app.
- Runtime behavior on Android 8 through 14 or Android 16. Compile target is API 36; the emulator test is API 35.
- Google Play approval, release signing or store publication.

Nie potwierdzono odsłuchu, rzeczywistej głośności głośnika, działania słuchawek/Bluetooth i wibracji fizycznego telefonu, ograniczeń baterii producentów, wszystkich kombinacji Nie przeszkadzać ani zdarzeń przewijania we wszystkich aplikacjach. Testy nie oznaczają zatwierdzenia przez Google Play.
