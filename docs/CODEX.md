# Drzemka · przekazanie dla Codexa

Ten projekt zawiera **wyłącznie makiety UI** w TypeScript (React 19, TanStack Start, Tailwind v4).
Twoje zadanie: napisać logikę (audio, timer, wybudzanie, zapis ustawień) bez zmiany wyglądu.

## 1. Idea produktu

Aplikacja do krótkiej drzemki (power nap). Użytkownik wybiera długość (10, 15, 20, 30 min),
wybiera łagodny dźwięk, naciska pomarańczowy przycisk i zasypia. Po czasie aplikacja
**łagodnie** go budzi. Wszystko ma wyciszać: zero nagłych zmian, zero głośnych dźwięków.

Zasady, których nie wolno łamać:

- Paleta: granat (tło), brudna biel (tekst), intensywny pomarańcz (słońce i jedyna główna akcja).
- Bez długich myślników w tekstach (`—`). Używaj kropek lub przecinków.
- Bez efektów szkła (backdrop blur), bez motywów terminalowych, bez fontów mono.
- Kolory tylko przez tokeny z `src/styles.css` (`bg-primary`, `text-muted-foreground`, `bg-sun` itd.).

## 2. Mapa plików

```text
src/
  routes/index.tsx                 jedyna strona; tu podmieniasz useNapMock na useNap
  features/nap/
    types.ts                       KONTRAKT: NapState, NapActions, NapController
    data.ts                        lista dźwięków, długości, teksty faz, formatTime
    useNapMock.ts                  atrapa stanu (do usunięcia po wdrożeniu logiki)
    components/
      NapScreen.tsx                układ ekranu, warianty dla każdej fazy
      SunOrb.tsx                   oddychające słońce (progress 0..1 obniża je)
      DurationPicker.tsx           wybór 10/15/20/30
      PlayButton.tsx               start / pauza
      SoundSheet.tsx               panel dźwięków i głośności
      Stars.tsx                    dekoracja
  styles.css                       tokeny i animacje (breathe, twinkle)
```

Komponenty są **czysto prezentacyjne**. Jedyny lokalny stan UI to otwarcie panelu dźwięków
w `NapScreen`. Nie dodawaj logiki do komponentów; trzymaj ją w hooku.

## 3. Kontrakt (`types.ts`)

```ts
type NapPhase = "idle" | "napping" | "paused" | "waking";

interface NapState {
  phase: NapPhase;
  minutes: 10 | 15 | 20 | 30;
  soundId: SoundId;
  volume: number;        // 0..1
  remainingSec: number;  // w idle = minutes * 60
}

interface NapActions {
  setMinutes(m); setSound(id); setVolume(v);
  start(); pause(); stop(); dismissWake();
}
```

Napisz `src/features/nap/useNap.ts` zwracający `NapController` i w `routes/index.tsx`
zamień `useNapMock(phase)` na `useNap()`. Parametr `?phase=` służy tylko do podglądu makiet;
możesz go usunąć.

## 4. Maszyna stanów

```text
          start()            czas = 0
  idle ───────────▶ napping ──────────▶ waking
   ▲   ◀── stop() ──┘  │  ▲                │
   │                pause() start()        │ dismissWake()
   │                   ▼  │                │
   └──── stop() ──── paused                │
   ◀───────────────────────────────────────┘
```

| Faza    | Audio                          | Timer         | UI                                  |
|---------|--------------------------------|---------------|-------------------------------------|
| idle    | cisza                          | stoi          | wybór minut aktywny, pokazuje "15 min" |
| napping | pętla, fade in 8 s             | odlicza       | wybór minut wyłączony, MM:SS        |
| paused  | fade out 1.5 s, pauza          | stoi          | słońce przygasa i nie oddycha       |
| waking  | dźwięk ścisza się, potem dzwonek | 00:00       | przycisk "Jestem gotów"             |

## 5. Wymagania logiki

1. **Audio**: Web Audio API lub `HTMLAudioElement` z pętlą bez przerwy (gapless). Pliki
   dodaj do `public/sounds/` i wpisz ścieżki w `SOUNDS[].src`. Szumy biały i brązowy
   możesz generować w locie (AudioBuffer z szumem, dla brązowego filtr całkujący).
2. **Fade**: każda zmiana głośności i dźwięku przez rampę (`linearRampToValueAtTime`),
   minimum 1 s. Zmiana dźwięku w trakcie drzemki to crossfade 2 s.
3. **Timer**: licz od `Date.now()` w momencie startu (nie sumuj ticków), żeby działał po
   uśpieniu karty. Odświeżaj `remainingSec` co 1 s.
4. **Wybudzanie**: 60 s przed końcem zacznij ściszać dźwięk do 30%. Na 00:00 przejdź do
   `waking`, zagraj delikatny dzwonek (narastający przez 10 s, nie głośniej niż 0.5).
5. **Wake Lock**: w `napping` poproś `navigator.wakeLock.request("screen")`, zwolnij w pozostałych fazach.
6. **Zapis**: `minutes`, `soundId`, `volume` w `localStorage` (odczyt w `useEffect`, bo SSR).
7. **Autoplay**: audio startuje tylko po kliknięciu przycisku (gest użytkownika).
8. **Media Session API**: tytuł "Drzemka", nazwa dźwięku, obsługa play/pause z ekranu blokady.

## 6. Dostępność

- `DurationPicker` i siatka dźwięków to `role="radiogroup"`; dodaj obsługę strzałek.
- Zegar ma `aria-live="polite"`; aktualizuj go co minutę dla czytników (nie co sekundę).
- `SoundSheet`: zamykanie klawiszem Escape i pułapka fokusu.
- Animacje są wyłączane przy `prefers-reduced-motion` (już w CSS).

## 7. Testy do napisania

- przejścia stanów zgodnie z tabelą w punkcie 4
- `remainingSec` poprawny po symulowanym skoku czasu (fake timers)
- wejście w `waking` dokładnie na 0
- zapis i odczyt ustawień z `localStorage`

## 8. Podgląd makiet

- `/` faza idle
- `/?phase=napping`
- `/?phase=paused`
- `/?phase=waking`
