import type { NapMinutes, Sound } from "./types";

export const DURATIONS: NapMinutes[] = [10, 15, 20, 30];

export const SOUNDS: Sound[] = [
  { id: "rain", label: "Deszcz", hint: "Miękkie krople na parapecie" },
  { id: "waves", label: "Fale", hint: "Spokojny oddech morza" },
  { id: "wind", label: "Wiatr", hint: "Lekki szum w koronach drzew" },
  { id: "forest", label: "Las", hint: "Cisza z odległymi ptakami" },
  { id: "brown", label: "Szum brązowy", hint: "Głęboki, ciepły szum" },
  { id: "white", label: "Szum biały", hint: "Równy szum, który zasłania świat" },
];

/** Krótkie zdania pod zegarem, zależne od fazy. */
export const PHASE_COPY = {
  idle: "Zamknij oczy. Reszta poczeka.",
  napping: "Oddychaj powoli. Nic nie musisz.",
  paused: "Chwila przerwy. Wróć, kiedy chcesz.",
  waking: "Dzień dobry. Wstań powoli.",
} as const;

export function formatTime(totalSec: number): string {
  const m = Math.floor(totalSec / 60);
  const s = totalSec % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}
