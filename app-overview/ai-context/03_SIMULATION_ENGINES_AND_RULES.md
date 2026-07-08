# Simulation Engines and Rules

## Orchestrator: `GameViewModel`

`GameViewModel` exposes all gameplay actions and routes each action to one or more feature engines.  
Most action methods are guarded by:

- `if (_currentActiveEvent.value != null) return`
- `if (_state.value.gameOver.isGameOver) return`

This means active crisis events and game-over block most user actions.

---

## 1) Diplomacy Engine (`DiplomacyViewModel`)

### Main responsibilities

- Passive geopolitical relation drift each tick
- War declaration and war battle simulation
- Treaty negotiation and armistice resolution
- Defense posture and procurement operations

### Key mechanics

- Relation drift is affected by military pressure (spending, mobilization, DEFCON)
- War progress changes monthly by skirmish outcome
- Victory/defeat end war at +/-100 progress thresholds
- Armistice cost scales with negative war progress
- Hardware purchase is blocked by governance bans (nuclear embargo / weapons ban)

### Important constants

- `WAR_MONTHLY_COST`
- `ARMISTICE_BASE_COST`
- `RECRUIT_COST_PER_SOLDIER`
- funding range: `0.5..1.5`

---

## 2) Production + Law Engine (`ProductionLawViewModel`)

### Main responsibilities

- Monthly production pipeline (energy -> food/materials -> goods -> revenue)
- Shortage penalties and demographic/approval consequences
- Law enact/repeal lifecycle
- Build power plants and mines

### Pipeline highlights

- Energy shortage applies severe output penalty multiplier
- Food shortage reduces approval and population
- Goods are fully sold each tick to compute goods revenue
- Per-resource flow stats are stored for UI and analytics

### Law rules

Enact requires:

- law not already active
- enough budget
- approval above threshold

Repeal removes ongoing modifiers; no activation refund.

---

## 3) Advancement Engine (`AdvancementViewModel`)

### Main responsibilities

- Science generation and research progression
- Tech unlock and prerequisite enforcement
- Ministry funding impact on social levels
- Religion conversion impacts
- University expansion

### Key mechanics

- Science generated each tick = base + education + university bonuses, scaled by religion/tech
- Active research consumes generated science via progress
- Extra funding spends budget for progress boosts
- Religion conversion applies immediate approval and instability costs
- Underfunded social ministries decay over time

### Restrictions

- `nuclear_fission` cannot be started/unlocked when UN nuclear embargo is active

---

## 4) Espionage + Security Engine (`EspionageSecurityViewModel`)

### Main responsibilities

- Instability and coup-risk dynamics
- Protocol suppression effects
- Mission deployment and resolution
- Spy recruitment/intel regeneration
- Coup terminal state trigger

### Key mechanics

- Instability increases from low approval, shortages, and high tax
- Security protocols reduce instability but incur approval penalties
- Coup risk accelerates when instability crosses threshold
- `coupRisk >= 100` triggers game-over state
- Mission outcomes alter rival relations, military strength, budget, approval, and spy count

---

## 5) Trade + Market Engine (`TradeMarketViewModel`)

### Main responsibilities

- Monthly market price updates by volatility
- Deal settlement for imports/exports
- Tariff application and approval penalties
- Spot market buy/sell operations

### Key mechanics

- Import tariffs generate revenue but can hurt approval at high rates
- Export deals require sufficient stock each month
- Import deals require sufficient budget each month
- Commodity demand modifiers influence next prices

### Important constraints

- Deals with very hostile rivals are rejected
- Tariff range is clamped to `0.0..0.5`

---

## 6) Governance Engine (`GovernanceViewModel`)

### Main responsibilities

- UN resolution lifecycle
- AI voting simulation
- Vote bribery
- Alliance formation/dissolution
- Passive governance effects and influence regen

### Key mechanics

- One active resolution at a time
- AI vote probability depends on relation, resolution type, and alliance context
- Resolution pass/fail compares vote counts at timer expiry
- Passed resolutions can activate global modifiers and immediate effects
- Alliances require relation threshold + influence cost

---

## 7) Analytics + Save Engine (`AnalyticsSaveViewModel`)

### Main responsibilities

- Monthly KPI snapshot recording
- GDP estimation
- JSON export/import of full `GameState`
- Save/load feedback messages

### Persistence location

`SharedPreferences` keys are used by `GameViewModel` for automated save state storage and restore.

---

## Cross-Engine Coupling Notes

- Governance toggles alter military and purchasing behavior indirectly.
- Research and religion multipliers affect production/combat/population dynamics.
- Production shortage flags feed internal security instability.
- Diplomacy war state affects audio mood and multiple UI panels.
- Economy taxes influence security/coup pressure and approval.

