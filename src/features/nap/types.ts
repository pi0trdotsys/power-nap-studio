/**
 * Kontrakt UI ↔ logika dla aplikacji Drzemka.
 *
 * Makiety (komponenty w ./components) są czysto prezentacyjne: dostają dane
 * przez propsy i zgłaszają zamiary przez callbacki. Codex implementuje
 * `NapController` (patrz docs/CODEX.md) i podmienia `useNapMock`.
 */

/** Identyfikator dźwięku. Rozszerzalny: dodaj nowy wpis w SOUNDS. */
export type SoundId = "rain" | "waves" | "wind" | "brown" | "white" | "forest";

export interface Sound {
  id: SoundId;
  /** Nazwa widoczna dla użytkownika (PL). */
  label: string;
  /** Jedno zdanie opisu, bez myślników. */
  hint: string;
  /** Ścieżka do pliku audio w pętli. Uzupełnia Codex. */
  src?: string;
}

/** Dozwolone długości drzemki w minutach. */
export type NapMinutes = 10 | 15 | 20 | 30;

/**
 * Faza sesji:
 * - idle: ekran startowy, nic nie gra
 * - napping: gra dźwięk, odlicza czas
 * - paused: zatrzymane przez użytkownika
 * - waking: czas minął, łagodne wybudzanie (wyciszenie dźwięku, potem dzwonek)
 */
export type NapPhase = "idle" | "napping" | "paused" | "waking";

export interface NapState {
  phase: NapPhase;
  minutes: NapMinutes;
  soundId: SoundId;
  /** Głośność 0..1 */
  volume: number;
  /** Pozostały czas w sekundach. W fazie idle równy minutes * 60. */
  remainingSec: number;
}

export interface NapActions {
  setMinutes: (m: NapMinutes) => void;
  setSound: (id: SoundId) => void;
  setVolume: (v: number) => void;
  /** Start lub wznowienie. */
  start: () => void;
  pause: () => void;
  /** Przerwanie sesji i powrót do idle. */
  stop: () => void;
  /** Potwierdzenie pobudki na ekranie waking. */
  dismissWake: () => void;
}

export type NapController = NapState & NapActions;
