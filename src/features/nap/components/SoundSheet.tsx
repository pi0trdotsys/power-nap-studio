import { CloudRain, Trees, Waves, Wind, AudioLines, Radio, X, type LucideIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import { SOUNDS } from "../data";
import type { SoundId } from "../types";

const ICONS: Record<SoundId, LucideIcon> = {
  rain: CloudRain,
  waves: Waves,
  wind: Wind,
  forest: Trees,
  brown: AudioLines,
  white: Radio,
};

interface SoundSheetProps {
  open: boolean;
  onClose: () => void;
  value: SoundId;
  onChange: (id: SoundId) => void;
  volume: number;
  onVolumeChange: (v: number) => void;
}

/** Wysuwany od dołu panel wyboru dźwięku i głośności. Pełne krycie, bez rozmycia. */
export function SoundSheet({ open, onClose, value, onChange, volume, onVolumeChange }: SoundSheetProps) {
  return (
    <>
      <div
        onClick={onClose}
        className={cn(
          "fixed inset-0 z-40 bg-background/70 transition-opacity duration-500",
          open ? "opacity-100" : "pointer-events-none opacity-0",
        )}
        aria-hidden
      />
      <section
        role="dialog"
        aria-modal="true"
        aria-label="Wybierz dźwięk"
        className={cn(
          "fixed inset-x-0 bottom-0 z-50 mx-auto max-w-md rounded-t-3xl bg-card px-6 pt-5 pb-10 transition-transform duration-500 ease-out",
          open ? "translate-y-0" : "translate-y-full",
        )}
      >
        <div className="mx-auto mb-5 h-1 w-10 rounded-full bg-border" />
        <header className="mb-6 flex items-center justify-between">
          <h2 className="font-display text-2xl">Dźwięk</h2>
          <button type="button" onClick={onClose} aria-label="Zamknij" className="rounded-full p-2 text-muted-foreground hover:text-foreground">
            <X className="size-5" />
          </button>
        </header>

        <div role="radiogroup" className="grid grid-cols-2 gap-3">
          {SOUNDS.map((s) => {
            const Icon = ICONS[s.id];
            const active = s.id === value;
            return (
              <button
                key={s.id}
                type="button"
                role="radio"
                aria-checked={active}
                onClick={() => onChange(s.id)}
                className={cn(
                  "flex flex-col items-start gap-3 rounded-2xl p-4 text-left transition-colors duration-300",
                  active ? "bg-primary text-primary-foreground" : "bg-secondary hover:bg-accent",
                )}
              >
                <Icon className="size-6" strokeWidth={1.5} />
                <span>
                  <span className="block text-sm font-semibold">{s.label}</span>
                  <span className={cn("block text-xs", active ? "text-primary-foreground/75" : "text-muted-foreground")}>
                    {s.hint}
                  </span>
                </span>
              </button>
            );
          })}
        </div>

        <label className="mt-8 block">
          <span className="mb-3 flex justify-between text-xs tracking-[0.2em] text-muted-foreground uppercase">
            <span>Głośność</span>
            <span className="tabular-nums">{Math.round(volume * 100)}</span>
          </span>
          <input
            type="range"
            min={0}
            max={1}
            step={0.01}
            value={volume}
            onChange={(e) => onVolumeChange(Number(e.target.value))}
            className="w-full accent-primary"
          />
        </label>
      </section>
    </>
  );
}

export { ICONS as SOUND_ICONS };
