# power nap studio
*Rest, recalibrated.*

[EN] | [PL]

---

## [EN] vision
A minimalist, native Android application designed to encourage restorative power naps and interrupt digital fatigue. Engineered in off-white, navy blue, and vibrant orange. No liquid glass, no terminal themes—just pure focus and calm. 

## core mechanics
▪ **Acoustic Isolation** — Curated, seamless soundscapes (transit, nature, synthetic noise) generated and mixed locally.
▪ **Adaptive Wake Protocols** — *Gentle* mode for light sleepers, and a high-intensity *Heavy Sleeper* override with tactile feedback.
▪ **Mindful Scrolling** — Optional local OS-level usage tracking that gently suggests a power nap when prolonged activity is detected.
▪ **Bilingual Native** — Built from the ground up for English and Polish (UI, accessibility, notifications).

## architecture
Prototyped in React (TanStack Start, Tailwind 4) via Lovable. Transitioning to a **Native Android** (Kotlin + Jetpack Compose) foundation to ensure robust background audio persistence, exact alarm scheduling, and secure `UsageStatsManager` integration.

## environment setup
Current web prototype iteration:
```bash
git clone [https://github.com/pi0trdotsys/power-nap-studio.git](https://github.com/pi0trdotsys/power-nap-studio.git)
cd power-nap-studio
npm i
npm run dev
