import { cn } from "@/lib/utils";
import type { NapPhase } from "../types";

interface SunOrbProps {
  phase: NapPhase;
  /** 0..1, ile czasu minęło. Rysuje cienki pierścień postępu i lekko obniża słońce. */
  progress?: number;
}

const R = 46;
const C = 2 * Math.PI * R;

/**
 * Centralne słońce. W idle i napping oddycha (6 s cykl, tempo spokojnego oddechu).
 * W trakcie sesji otacza je cienki pierścień postępu. W paused zastyga.
 */
export function SunOrb({ phase, progress = 0 }: SunOrbProps) {
  const still = phase === "paused";
  const inSession = phase === "napping" || phase === "paused";
  const sink = inSession ? progress * 14 : 0;

  return (
    <div className="relative flex size-72 items-center justify-center sm:size-80" aria-hidden>
      {/* pierścień postępu */}
      <svg
        viewBox="0 0 100 100"
        className={cn(
          "absolute inset-0 -rotate-90 transition-opacity duration-1000",
          inSession ? "opacity-100" : "opacity-0",
        )}
      >
        <circle cx="50" cy="50" r={R} fill="none" className="stroke-border" strokeWidth="0.6" />
        <circle
          cx="50"
          cy="50"
          r={R}
          fill="none"
          className="stroke-primary transition-[stroke-dashoffset] duration-1000"
          strokeWidth="0.9"
          strokeLinecap="round"
          strokeDasharray={C}
          strokeDashoffset={C * (1 - progress)}
        />
      </svg>

      <div
        className="relative flex size-full items-center justify-center transition-transform duration-1000"
        style={{ transform: `translateY(${sink}px)` }}
      >
        <div className={cn("absolute inset-6 rounded-full bg-sun/10", !still && "animate-breathe-slow")} />
        <div className={cn("absolute inset-12 rounded-full bg-sun/15", !still && "animate-breathe")} />
        <div
          className={cn(
            "relative size-32 rounded-full sun-core transition-opacity duration-1000 sm:size-36",
            !still && "animate-breathe",
            still && "opacity-55",
            phase === "waking" && "scale-110",
          )}
        />
      </div>
    </div>
  );
}
