import { useState } from "react";
import { Moon, Square } from "lucide-react";
import { PHASE_COPY, SOUNDS, formatTime } from "../data";
import type { NapController } from "../types";
import { DurationPicker } from "./DurationPicker";
import { PlayButton } from "./PlayButton";
import { SoundSheet, SOUND_ICONS } from "./SoundSheet";
import { Stars } from "./Stars";
import { SunOrb } from "./SunOrb";

interface NapScreenProps {
  nap: NapController;
}

/**
 * Jedyny ekran aplikacji. Wszystkie fazy (idle, napping, paused, waking)
 * to warianty tego samego układu, żeby nic nie "skakało" i nie budziło.
 */
export function NapScreen({ nap }: NapScreenProps) {
  const [soundsOpen, setSoundsOpen] = useState(false);
  const sound = SOUNDS.find((s) => s.id === nap.soundId)!;
  const SoundIcon = SOUND_ICONS[nap.soundId];
  const total = nap.minutes * 60;
  const progress = total ? 1 - nap.remainingSec / total : 0;
  const inSession = nap.phase === "napping" || nap.phase === "paused";

  return (
    <main className="relative mx-auto flex min-h-screen max-w-md flex-col items-center overflow-hidden px-6 pt-8 pb-10">
      <Stars />

      <header className="relative flex w-full items-center justify-between">
        <span className="w-9" />
        <div className="text-center">
          <h1 className="font-display text-2xl tracking-wide">Drzemka</h1>
          <p className="mt-1 text-[11px] tracking-[0.3em] text-muted-foreground uppercase">odpocznij krótko</p>
        </div>
        <Moon className="size-6 text-foreground/80" strokeWidth={1.5} aria-hidden />
      </header>

      <section className="relative mt-6 flex flex-1 flex-col items-center justify-center">
        <SunOrb phase={nap.phase} progress={progress} />

        <p className="mt-2 font-display text-6xl tabular-nums" aria-live="polite">
          {nap.phase === "idle" ? (
            <>
              {nap.minutes}
              <span className="ml-2 font-sans text-2xl text-muted-foreground">min</span>
            </>
          ) : (
            formatTime(nap.remainingSec)
          )}
        </p>
        <p className="mt-3 text-sm text-muted-foreground">{PHASE_COPY[nap.phase]}</p>
      </section>

      {nap.phase === "waking" ? (
        <div className="relative mt-10 flex w-full flex-col items-center gap-4">
          <button
            type="button"
            onClick={nap.dismissWake}
            className="w-full max-w-xs rounded-full bg-primary py-4 font-semibold text-primary-foreground shadow-play transition-transform hover:scale-[1.02] active:scale-95"
          >
            Jestem gotów
          </button>
        </div>
      ) : (
        <>
          <div className="relative mt-8 flex w-full justify-center">
            <DurationPicker value={nap.minutes} onChange={nap.setMinutes} disabled={inSession} />
          </div>

          <nav className="relative mt-10 grid w-full grid-cols-3 items-center">
            <button
              type="button"
              onClick={() => setSoundsOpen(true)}
              className="flex flex-col items-center gap-2 text-muted-foreground transition-colors hover:text-foreground"
            >
              <SoundIcon className="size-5" strokeWidth={1.5} />
              <span className="text-[11px] tracking-[0.2em] uppercase">{sound.label}</span>
            </button>

            <div className="flex justify-center">
              <PlayButton phase={nap.phase} onStart={nap.start} onPause={nap.pause} />
            </div>

            <button
              type="button"
              onClick={nap.stop}
              disabled={!inSession}
              className="flex flex-col items-center gap-2 text-muted-foreground transition-colors hover:text-foreground disabled:opacity-0"
            >
              <Square className="size-5" strokeWidth={1.5} />
              <span className="text-[11px] tracking-[0.2em] uppercase">Zakończ</span>
            </button>
          </nav>
        </>
      )}

      <SoundSheet
        open={soundsOpen}
        onClose={() => setSoundsOpen(false)}
        value={nap.soundId}
        onChange={nap.setSound}
        volume={nap.volume}
        onVolumeChange={nap.setVolume}
      />
    </main>
  );
}
