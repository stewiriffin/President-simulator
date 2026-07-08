# Figma Make / FigJam AI Prompt — Nation State Simulator UI Upgrade

**How to use:** Paste this into Figma AI (Make / AI design). Attach or open the existing file first:

- Figma file: https://www.figma.com/design/Qo381pgcV78B9nirKBGOhX/Nation-State-Simulator-UI-Design  
- Local export already in repo: `Nation State Simulator UI Design (3).zip` → opens as `ui_reference/` (React reference for Overview / Economy / Defense / Foreign)

Treat that file as the **baseline visual system**. Do **not** invent a new brand from scratch. Evolve it into a complete, shippable mobile command UI for the *current* Android gameplay.

---

## 1) Product & platform (constraints — respect these)

Redesign for an **Android mobile** nation-governance sim (Kotlin / Jetpack Compose already shipping).

### Device / layout
- Primary artboard: **phone landscape or phone portrait landscape-first HUD** preference historically, but produce **both**:
  - `390 × 844` (portrait phone)
  - `844 × 390` (landscape phone — important; game often played landscape)
- Safe areas for status bar / gesture bar / notches
- Fixed chrome: top **Global HUD** + bottom **Ministry nav** (or landscape side rail equivalent)

### Interaction density
- This is a **dense strategy game**, not a marketing app
- Cards carry photo headers, KPI pills, progress bars, CTAs
- Prefer readable hierarchy over sparse “wellness app” whitespace
- Every screen should answer: *what matters this month?* and *what can I do next?*

---

## 2) Visual system (LOCK — match existing Figma / zip tokens)

### Palette (exact)
| Token | Hex | Use |
|---|---|---|
| Background | `#F0E8D4` | App parchment / page |
| Card | `#FFFDF5` / white `#FFFFFF` for game cards | Panels |
| Foreground | `#1C1810` | Body text |
| Muted text | `#7A6F5A` | Labels / captions |
| Primary / navy | `#1E3A6E` | Headers, primary actions, bottom nav |
| Accent / gold | `#C4882A` | CTAs, XP, rank chips, invest buttons |
| Border | `#D4C8A8` | Hairlines, chips |
| Emerald | `#1D6940` | Positive / ready / surplus |
| Red | `#A52828` | Crisis / hostile / deficit |
| Violet | `#6B4FA0` | Science / air branch accents |
| Amber warn | `#D97706` | Soft warnings |

### Style vocabulary (keep)
- Warm **parchment command center**, not dark cyberpunk, not purple SaaS
- Photo-led cards with **navy gradient scrims** over Unsplash-like imagery
- **Cinzel / display serif** for country name & screen titles (“VELTRIA”, “ECONOMY”)
- Mono / tabular for money & KPIs
- Rounded cards `~16–20px`, soft shadow, optional amber **warning border** when vital is at risk
- Game bars: inset track + animated fill; XP bars gold gradient
- Status chips: soft tinted pills (ALLY green, HOSTILE red, etc.)
- Motion language: short entrance fades / scale; tap press on primary buttons

### DO NOT
- Swap to cream+terracotta brochure look, purple-on-white AI default, or broadsheet newspaper
- Flatten into generic Material dashboard without photo headers
- Add floating promo stickers/badges over hero media
- Use more than **one** primary CTA color family per section (gold OR navy)

---

## 3) What already exists (from the Figma zip) — refine, don’t discard

Improve these existing frames:

### A. Overview / Dashboard
- Hero map + “Republic of VELTRIA” wordmark
- Event count / world rank chips (rank should look *earned*, not decorative)
- Empire Status 2×2 vitals (Treasury, Stability, Mil Power, Approval) with warn pulse
- Active Situations list (CRISIS / WARNING / OPPORTUNITY) with photo strip + CTA
- Ministry jump tiles (Economy / Defense / Foreign / Domestic…)

### B. Economy
- Photo screen header with GDP / growth pills
- Tabs: Sectors / Policy / Budget / Trade (and Industry)
- Sector cards with photo, GDP share, growth, level badge, XP bar, Invest CTA
- GDP breakdown stacked bar

### C. Defense
- Branch filters (Army / Navy / Air)
- Unit cards with readiness stars, maintenance, status chips
- Recruit / logistics feeling command screens

### D. Foreign Affairs
- Rival nation cards with relation bars, threat, stance chips
- Soft-power / treaty action density

**Your job:** make these feel premium, consistent, and **complete** for the full shippable game loop listed below.

---

## 4) Gameplay that must be visible in the new UI (gaps vs zip)

Expand the design system to cover these **live Android features** (currently underserved or missing in the zip):

### Shell / chrome
1. **Launch / Title** — Continue, New Game, Save slots 1–3  
2. **Global HUD** — Budget, Approval, Date, Election countdown chip, Auto-tick Play/Pause, End Turn  
3. **Bottom nav (5)** — Overview · Economy · Defense · Foreign · Intel (+ alert badges)  
4. **Dashboard secondary tiles** — Science · Domestic/Laws · UN · Analytics · Demographics · Settings  

### Modal stack (design as a consistent dialog system)
Priority order overlays:
1. Crisis Event dialog (choices)  
2. War Outcome settlement (victory/defeat)  
3. Mission Result (success/fail + concrete loot text)  
4. Monthly Bulletin / Turn Summary (KPI deltas + “What happened” bullet list)  
5. Campaign End (victory/loss, Load Save, Return to Title)

### Ministry depth screens (full redesigns or new frames)
| Screen | Must show |
|---|---|
| Economy → **Industry** | Energy/Food/Materials/Goods stocks + produced/consumed bars; shortage banners (“30% output”); build Power Plants / Mines with amount slider |
| Economy → **Trade** | Tariffs, spot quotes, active deals, Propose Contract |
| Military → Logistics | Deployment posture, salary funding slider with **forecast** morale/upkeep, DEFCON 1–5 chips |
| Diplomacy | Aid, State Visit, Negotiate Trade/NAP, Break treaty, Grain export, War room with last-battle note |
| Security / Intel | Coup risk / instability meters, recruit spy, deploy mission types, mission progress |
| Science | Active research XP, Extra Funding, tech tree rows, **Start Research** + **Unlock Instantly** |
| Domestic / Laws | Ideology chips, Parliament queue (pending / rush / cancel), law switches by tab, SOCIETY funding/religion/university |
| Governance / UN | Propose resolution, bribe votes, coalitions |
| Analytics | GDP/Approval/Budget charts + multi-slot save/load |
| Demographics | Bloc approval bars + spendable Campaign Actions before election |

---

## 5) UX problems to solve (explicit improvement brief)

Critique and fix the current UI / zip weaknesses:

1. **Modal stampede** — Turn summary + missions + war can stack. Design calm, scannable dialogs with clear hierarchy and short bulletin lists (max ~6–8 lines).  
2. **Dense ministry overload** — Reduce “sea of identical cards.” Introduce stronger section rhythm, sticky sub-tabs, and better empty / locked / pending states.  
3. **Threat readability** — Crisis vs opportunity vs war must be unmistakable at a glance (color, icon, left rail accent, photo treatment).  
4. **Action affordance** — Primary CTAs (Invest, Deploy, Enact, End Turn) must feel identical across ministries; secondary actions outlined.  
5. **Photo + text contrast** — Scrims must guarantee white text ≥ WCAG AA on headers; no bare photo titles.  
6. **Landscape ergonomics** — HUD and bottom nav must not crush content; propose landscape layouts that keep 2-column cards usable.  
7. **Election tension** — Surface countdown and campaign actions so January elections don’t feel sudden.  
8. **Feedback loop** — Monthly bulletin and mission loot should look like official “Situation Room” printouts, not generic alerts.  
9. **World cast** — Rival cards must scale to **7 nations** without chaos (grid / carousel / list hybrid).  
10. **Consistency** — Unify corners, chip sizes, icon weights, and header heights across all screens.

---

## 6) Deliverables (what Figma AI should create)

Produce a tidy page structure:

### Page: `NSS Design System`
- Color styles matching tokens above  
- Text styles: Display (Cinzel), Title, Section label (tracked uppercase), Body, Mono KPI, Chip  
- Components: HUD chip, Status chip, GameBar, XPBar, Level badge, Photo header, Panel, Sector card, Nation card, Vital card, Ministry tile, Tab bar, Bottom nav item (w/ badge), Primary/Secondary/Destructive buttons, Dialog shell, Alert banner, Empty state  

### Page: `Mobile Portrait Flows`
Full screens: Launch, Dashboard, Economy (all tabs), Defense, Foreign, Intel, Science, Laws, UN, Analytics, Demographics, Settings  

### Page: `Mobile Landscape Flows`
At least: Dashboard, Economy Sectors, Foreign, War Room overlay  

### Page: `Overlays & Feedback`
Crisis Event, Monthly Bulletin, Mission Result, War Settlement, Game Over  

### Page: `States`
Loading photo, Offline fallback (gradient only), Warning/critical vital, Locked ministry, Pending law queue, Empty trade deals  

### Annotations
On each key frame, add short notes: spacing tokens, component names, and which Android screen it maps to.

### Export guidance for engineering
- Name layers cleanly (`Hud/Chip`, `Card/Sector`, `Dialog/Bulletin`)  
- Prefer Auto Layout  
- Mark 8pt spacing rhythm  
- Include a 1-page “diff vs old zip” notes frame listing visual upgrades  

---

## 7) Content sample (use realistic sim copy, not lorem)

Nation: **Veltria**  
Date example: **March 2032 · Q1**  
Treasury declining, Stability at risk, Rank #N vs rivals  
Rivals include: Northland, Eastmark, Southreach, Westoria, Aurum Coast, Kryos, Verdehaan  
Sample bulletin lines:
- “Energy shortage — industrial output penalized to 30%.”
- “War: Front advanced 8 pts · our losses 1.2K · enemy 2.4K”
- “Technology unlocked: Fusion Pilot”
- “Covert op succeeded in Eastmark”

---

## 8) Success criteria

The redesign succeeds if:
1. A player unfamiliar with the zip can identify **ministry + threat level + next action** in under 3 seconds on Dashboard and Foreign.  
2. All new gameplay surfaces (Industry, Bulletin, Mission loot, DEFCON, Campaign actions, Save slots) look native to the parchment/navy/gold system.  
3. Portrait and landscape both feel intentional (not stretched).  
4. Components reuse ≥ 80% across screens.  
5. No purple-gradient / cold SaaS / newspaper aesthetic creep.

---

## 9) Generation instruction (paste as the one-liner kickoff)

> Upgrade the existing Nation State Simulator Figma file (Qo381pgcV78B9nirKBGOhX / zip UI Design) into a complete Android mobile design system and screen set. Keep the warm parchment, navy (#1E3A6E), and gold (#C4882A) command-center look with photo-led cards and Cinzel titles. Fill gaps for Launch, HUD (election countdown + auto-tick), Industry/Trade, Science/Laws/UN/Analytics/Demographics, and Situation Room overlays (crisis, monthly bulletin, mission loot, war settlement). Improve hierarchy, landscape layouts, threat readability, and CTA consistency. Deliver design-system components, portrait + landscape frames, overlay states, and engineering-ready annotations—evolution of the current UI, not a brand reboot.

---

## Optional attachments for Figma AI
1. This prompt  
2. Screenshots of the current Android app (Dashboard / Economy / Diplomacy / Turn Summary) if available  
3. The zip contents / linked Figma file above as the source of truth for visuals
