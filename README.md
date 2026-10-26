<p align="center">
  <img src="brand/deciary-icon.png" width="120" alt="Deciary app icon">
</p>

<h1 align="center">Deciary</h1>
<p align="center"><em>Letters to future you.</em></p>

Deciary is a private, offline decision diary. You write down a decision and what you expect to happen, seal it like a letter, and open it later to see how it really went.

This repository holds the **v2 design**: interactive screen mockups, the logo, artwork and the design notes. There is no app code yet.

## What's here

| Folder | Contents |
| --- | --- |
| `brand/` | App icon (light and night, SVG and PNG) and the three leather cover options |
| `design/screens/` | 19 interactive screen mockups (`.dc.html`) |
| `design/img/` | 3D artwork used in the screens |
| `design/tools/leathergen.py` | Generates the stitched leather cover as SVG |
| `design/canvas.json` | Layout of the screens on the design canvas |
| `docs/DESIGN.md` | Summary of the design system and decisions |

The living design canvas and the full design document are on claude.ai (private links, owner access only).

## Screens

| Flow | Screens |
| --- | --- |
| First run | Welcome (3 slides) · Cover setup · Home on first launch |
| Home | Home · Home at night · Lock screen |
| Writing a letter | How sure are you · Challenge questions · Sealed |
| Opening a letter | Letter · Then & now · Verdict |
| Shelf and control | Your shelf · Settings · Your data |
| Edge states | Five letters due with reminders off · Restore with wrong password · Safety sheet |

The `.dc.html` files are Design Component sources made for the claude.ai design canvas. They use its runtime (`support.js`), so they don't open as plain web pages.

## Look and feel

- **Colours:** lilac paper `#F5F2FB`, deep violet ink `#1D1A33`, violet accent `#6E4FE6`, rose seals `#C23C72`.
- **Type:** Fraunces for headings, Noto Sans for text.
- **Diary cover:** stitched leather in Ink (rose-gold snap), Plum (silver snap) or Natural tan (brass snap). The name is pressed into the leather, and tapping the snap locks the diary.
- **Icon:** a D-shaped envelope whose flap and seal read as clock hands.

## Credits

- 3D artwork: [Microsoft Fluent Emoji](https://github.com/microsoft/fluentui-emoji), MIT licence.
- Icons: [Material Symbols Rounded](https://fonts.google.com/icons) by Google, Apache 2.0 licence.
- Fonts: [Fraunces](https://fonts.google.com/specimen/Fraunces) and [Noto Sans](https://fonts.google.com/noto/specimen/Noto+Sans), SIL Open Font Licence.
