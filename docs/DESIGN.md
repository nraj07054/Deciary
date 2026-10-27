# Deciary design summary

A short summary of the v2 design. The full design document lives on claude.ai and stays the source of truth.

## Core idea

Every decision is a sealed letter to future you. Saving seals it, the review date opens it, and a verdict stamps it. Once sealed, the prediction and how sure you were are locked; a typo fix is allowed for 10 minutes.

## Principles

| Goal | In practice |
| --- | --- |
| Trust from the first minute | Privacy is shown, not claimed: offline badge, "0 bytes ever sent", privacy blur, quick lock |
| Logging in under 60 seconds | Only the question and choice are required; the rest can be skipped |
| Bring people back to review | The review is the payoff moment and gets the richest design |
| Playful, never silly | Delight lives in motion and wording; layouts stay calm |
| Kind | No streaks, guilt or nagging; hard outcomes are met gently |

## Colour tokens

| Token | Light | Dark | Use |
| --- | --- | --- | --- |
| `bg` | `#F5F2FB` | `#15122A` | Screen background |
| `surface` | `#FFFFFF` | `#221E3D` | Cards, sheets |
| `ink` | `#1D1A33` | `#EEEAF8` | Main text, icons |
| `ink-quiet` | `#5E587A` | `#B3ADCC` | Secondary text, status lines |
| `line` | `#E4DEF2` | `#35305A` | Dividers, borders |
| `tint` | `#EDE7FD` | `#2C2547` | Illustration panels, selected backgrounds |
| `accent` | `#6E4FE6` | `#A48BFF` | Primary buttons, selected states |
| `accent-text` | `#5A3DD0` | `#C9B8FA` | Links, accent words |
| `wax` | `#C23C72` | `#E07AA3` | Seals and verdict marks only |
| `danger` | `#C0392B` | `#E06A5C` | Delete, destructive confirms |

All text pairs were checked at 4.5:1 contrast or better.

Mood colours (always shown with the word): Calm `#2F9C95`, Rushed `#E5674F`, Anxious `#8E7CC3`, Excited `#D9578C`, Tired `#7A8699`.

## Diary cover

| Leather | Base | Snap |
| --- | --- | --- |
| Ink (default) | `#2A2547` | Rose gold |
| Plum | `#5B3FB0` | Silver |
| Natural tan | `#A8693A` | Brass |

The cover shows the person's name pressed into the leather and "Only you can open this". Tapping the cover locks the diary. The lock screen is the same leather, closed. `design/tools/leathergen.py` draws the cover.

## Type, shape and icons

- Fraunces (headings) and Noto Sans (text); Devanagari and Bengali Noto fallbacks for v1.1.
- Cards 20–24 dp corners, buttons fully rounded, touch targets at least 48 dp.
- Icons: Material Symbols Rounded, weight 450.
- Scene art: soft 3D illustrations drawn in Compose (`ui/components/Illustrations.kt`), lit from the top left. Settings and data rows use Material icons on tinted tiles.
- Verdict objects: Fluent Emoji 3D.

## Verdicts

| Verdict | Meaning | Object |
| --- | --- | --- |
| Earned it | Sound thinking, went well | Medal |
| Bad luck | Sound thinking, went badly | Rain cloud |
| Lucky break | Shaky thinking, went well | Four-leaf clover |
| Lesson | Shaky thinking, went badly | Light bulb |

## Voice

A curious, kind friend: short sentences, plain words, never a lecture, never tells the user what to choose. Playfulness switches off on the safety sheet, "worse" outcomes, delete and restore, and errors.

## Research behind the cover and wording

People quit journaling apps when they aren't sure who can see their entries, so the screens answer "Who can see this?" in plain words. A quick lock and a calm, beautiful look both matter; privacy alone doesn't win people over.
