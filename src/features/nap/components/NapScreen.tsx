import { useState } from "react";
import { Moon, Square } from "lucide-react";
import { cn } from "@/lib/utils";
import { PHASE_COPY, SOUNDS, formatTime } from "../data";
import type { NapController } from "../types";
import { DurationPicker } from "./DurationPicker";
import { PlayButton } from "./PlayButton";
import { SoundSheet, SOUND_ICONS } from "./SoundSheet";
import { Stars } from "./Stars";
import { SunOrb } from "./SunOrb";

/** Makieta: godzina pobudki liczona od teraz. Codex może zostawić lub liczyć od startu. */
function wakeAt(remainingSec: number) {
  const d = new Date(Date.now() + remainingSec * 1000);
  return `${String(d.getHours()).padStart(2, "0")}:${String(d.getMinutes()).padStart(2, "0")}`;
}

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

      <div className="relative mt-6 h-6">
        {inSession && (
          <p className="text-xs tracking-[0.2em] text-muted-foreground uppercase">
            {/* TODO(codex): godzina końca z prawdziwego timera */}
            Pobudka o {wakeAt(nap.remainingSec)}
          </p>
        )}
      </div>

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
          <div
            className={cn(
              "relative mt-4 flex w-full justify-center transition-all duration-700",
              inSession ? "pointer-events-none h-0 opacity-0" : "h-24 opacity-100",
            )}
            aria-hidden={inSession}
          >
            <DurationPicker value={nap.minutes} onChange={nap.setMinutes} disabled={inSession} />
          </div>

          <nav className="relative mt-10 grid w-full grid-cols-3 items-center">
            <button
              type="button"
              onClick={() => setSoundsOpen(true)}
              aria-label={`Dźwięk: ${sound.label}. Zmień`}
              className="mx-auto flex flex-col items-center gap-2 rounded-2xl px-3 py-2 text-foreground/85 transition-colors hover:bg-secondary hover:text-foreground"
            >
              <span className="flex size-10 items-center justify-center rounded-full bg-secondary">
                <SoundIcon className="size-5" strokeWidth={1.5} />
              </span>
              <span className="text-[11px] tracking-[0.15em] uppercase">{sound.label}</span>
            </button>

            <div className="flex justify-center">
              <PlayButton phase={nap.phase} onStart={nap.start} onPause={nap.pause} />
            </div>

            <button
              type="button"
              onClick={nap.stop}
              disabled={!inSession}
              className="mx-auto flex flex-col items-center gap-2 rounded-2xl px-3 py-2 text-foreground/85 transition-all duration-500 hover:bg-secondary hover:text-foreground disabled:pointer-events-none disabled:opacity-0"
            >
              <span className="flex size-10 items-center justify-center rounded-full bg-secondary">
                <Square className="size-4" strokeWidth={1.5} />
              </span>
              <span className="text-[11px] tracking-[0.15em] uppercase">Zakończ</span>
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
