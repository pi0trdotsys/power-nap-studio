import { Pause, Play } from "lucide-react";
import type { NapPhase } from "../types";

interface PlayButtonProps {
  phase: NapPhase;
  onStart: () => void;
  onPause: () => void;
}

/** Główna akcja. Pomarańczowy, jedyny tak mocny element na ekranie. */
export function PlayButton({ phase, onStart, onPause }: PlayButtonProps) {
  const playing = phase === "napping";
  return (
    <button
      type="button"
      onClick={playing ? onPause : onStart}
      aria-label={playing ? "Wstrzymaj drzemkę" : "Zacznij drzemkę"}
      className="flex size-20 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-play transition-transform duration-300 hover:scale-105 active:scale-95 focus-visible:ring-4 focus-visible:ring-ring/40 focus-visible:outline-none"
    >
      {playing ? <Pause className="size-8 fill-current" /> : <Play className="ml-1 size-8 fill-current" />}
    </button>
  );
}
