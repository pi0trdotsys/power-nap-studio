import { createFileRoute } from "@tanstack/react-router";
import { NapScreen } from "@/features/nap/components/NapScreen";
import { useNapMock } from "@/features/nap/useNapMock";
import type { NapPhase } from "@/features/nap/types";

const PHASES: NapPhase[] = ["idle", "napping", "paused", "waking"];

/** `?phase=waking` itd. pozwala obejrzeć każdą fazę makiety bez logiki. */
export const Route = createFileRoute("/")({
  validateSearch: (s: Record<string, unknown>): { phase?: NapPhase } => {
    const p = s["phase"] as NapPhase;
    return PHASES.includes(p) ? { phase: p } : {};
  },
  head: () => ({
    meta: [
      { title: "Drzemka · łagodne dźwięki do power nap" },
      { name: "description", content: "Prosta aplikacja z kojącymi dźwiękami, która pomaga zasnąć na krótką drzemkę." },
      { property: "og:title", content: "Drzemka · łagodne dźwięki do power nap" },
      { property: "og:description", content: "Kojące dźwięki i spokojny minutnik do krótkiej drzemki." },
      { property: "og:type", content: "website" },
      { name: "twitter:card", content: "summary_large_image" },
    ],
  }),
  component: Index,
});

function Index() {
  const { phase } = Route.useSearch();
  // TODO(codex): zamień na useNap() z prawdziwą logiką.
  const nap = useNapMock(phase);
  return <NapScreen key={phase} nap={nap} />;
}
