import { cn } from "@/lib/utils";
import { DURATIONS } from "../data";
import type { NapMinutes } from "../types";

interface DurationPickerProps {
  value: NapMinutes;
  onChange: (m: NapMinutes) => void;
  disabled?: boolean;
}

/** Oś z czterema punktami: 10, 15, 20, 30 min. Radiogroup dla dostępności. */
export function DurationPicker({ value, onChange, disabled }: DurationPickerProps) {
  return (
    <div className="w-full max-w-xs">
      <div role="radiogroup" aria-label="Długość drzemki" className="relative flex justify-between">
        <div className="absolute inset-x-3 top-3 h-px bg-border" aria-hidden />
        {DURATIONS.map((m) => {
          const active = m === value;
          return (
            <button
              key={m}
              type="button"
              role="radio"
              aria-checked={active}
              disabled={disabled}
              onClick={() => onChange(m)}
              className="group relative flex flex-col items-center gap-3 disabled:opacity-40"
            >
              <span className="flex size-6 items-center justify-center">
                <span
                  className={cn(
                    "rounded-full transition-all duration-300",
                    active ? "size-4 bg-primary shadow-play" : "size-2.5 bg-muted-foreground/50 group-hover:bg-foreground/70",
                  )}
                />
              </span>
              <span className={cn("text-sm tabular-nums", active ? "text-foreground" : "text-muted-foreground")}>
                {m}
              </span>
            </button>
          );
        })}
      </div>
      <p className="mt-4 text-center text-xs tracking-[0.2em] text-muted-foreground uppercase">minut</p>
    </div>
  );
}
