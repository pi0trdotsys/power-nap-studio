import { cn } from "@/lib/utils";
import type { NapPhase } from "../types";

interface SunOrbProps {
  phase: NapPhase;
  /** 0..1, ile czasu minęło. Służy do delikatnego "zachodzenia" słońca. */
  progress?: number;
}

/**
 * Centralne słońce. W idle i napping oddycha (6 s cykl, tempo spokojnego oddechu).
 * W paused zastyga. W waking świeci najjaśniej.
 */
export function SunOrb({ phase, progress = 0 }: SunOrbProps) {
  const still = phase === "paused";
  // Słońce lekko opada wraz z upływem drzemki (wizualny "zachód").
  const sink = phase === "napping" || phase === "paused" ? progress * 18 : 0;

  return (
    <div
      className="relative flex size-72 items-center justify-center sm:size-80"
      style={{ transform: `translateY(${sink}px)` }}
      aria-hidden
    >
      <div
        className={cn(
          "absolute inset-0 rounded-full bg-sun/10",
          !still && "animate-breathe-slow",
        )}
      />
      <div
        className={cn("absolute inset-8 rounded-full bg-sun/15", !still && "animate-breathe")}
      />
      <div className="absolute inset-16 rounded-full bg-sun/20" />
      <div
        className={cn(
          "relative size-36 rounded-full sun-core transition-opacity duration-1000 sm:size-40",
          !still && "animate-breathe",
          phase === "paused" && "opacity-60",
        )}
      />
    </div>
  );
}
