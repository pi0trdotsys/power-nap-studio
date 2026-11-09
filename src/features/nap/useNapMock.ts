import { useState } from "react";
import type { NapController, NapMinutes, NapPhase, SoundId } from "./types";

/**
 * MAKIETA. Trzyma wyłącznie stan wizualny, żeby dało się klikać po ekranach.
 * Brak timera, brak audio. Codex zastępuje ten hook prawdziwym `useNap()`
 * o tej samej sygnaturze (NapController).
 */
export function useNapMock(initialPhase: NapPhase = "idle"): NapController {
  const [phase, setPhase] = useState<NapPhase>(initialPhase);
  const [minutes, setMinutesState] = useState<NapMinutes>(15);
  const [soundId, setSound] = useState<SoundId>("rain");
  const [volume, setVolume] = useState(0.6);

  // TODO(codex): realne odliczanie. Tu statyczna wartość dla wyglądu.
  const remainingSec = phase === "idle" ? minutes * 60 : phase === "waking" ? 0 : minutes * 60 - 263;

  return {
    phase,
    minutes,
    soundId,
    volume,
    remainingSec,
    setMinutes: (m) => setMinutesState(m),
    setSound,
    setVolume,
    start: () => setPhase("napping"),
    pause: () => setPhase("paused"),
    stop: () => setPhase("idle"),
    dismissWake: () => setPhase("idle"),
  };
}
