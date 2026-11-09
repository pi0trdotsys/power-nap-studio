/** Dekoracyjne gwiazdy. Stałe pozycje, żeby SSR i klient renderowały to samo. */
const STARS = [
  [8, 12], [22, 6], [78, 9], [91, 18], [12, 34], [88, 40], [6, 58],
  [94, 62], [18, 74], [82, 80], [50, 4], [64, 22], [34, 20], [70, 88],
] as const;

export function Stars() {
  return (
    <div className="pointer-events-none absolute inset-0" aria-hidden>
      {STARS.map(([x, y], i) => (
        <span
          key={i}
          className="absolute size-0.5 rounded-full bg-star animate-twinkle"
          style={{ left: `${x}%`, top: `${y}%`, animationDelay: `${(i % 5) * 0.8}s` }}
        />
      ))}
    </div>
  );
}
