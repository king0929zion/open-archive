# UI parity contract

The supplied Archive single-file HTML prototype is the visual and interaction source of truth.

Hard constraints:

- White/near-black/gray visual system; no visible card outlines.
- Shadows are subtle and used only for floating surfaces.
- Compose screen keeps text -> photo strip -> three small metadata pills.
- Location opens a bottom sheet with input/search and nearby POIs.
- Weather opens a small anchored floating card.
- Mood opens a small anchored floating card with a five-stop slider: 低落 / 平静 / 悠闲 / 开心 / 活力.
- Detail metadata is two lines: date/time first; location/weather/mood second, with the vertical three-dot entry at the right.
- Detail overflow contains only Share and Delete.
- Provider UI is a compact settings-style list, not a dashboard.

Any change that materially alters those rules is a design regression unless the product prototype is changed first.
