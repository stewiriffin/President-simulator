import { useState } from "react";
import { motion } from "motion/react";
import {
  Star, Shield, Globe, DollarSign, Users, Eye,
  TrendingUp, TrendingDown, AlertTriangle, Swords,
  Anchor, Wind, ChevronRight, Zap, Clock,
  Plus, Minus, Landmark, Lock, Scroll,
} from "lucide-react";

// ─── IMAGES ──────────────────────────────────────────────────────────────────
const IMG = {
  agriculture:   "https://images.unsplash.com/photo-1508175688576-0c076b47b5b5?w=500&h=240&fit=crop&auto=format",
  industry:      "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=500&h=240&fit=crop&auto=format",
  manufacturing: "https://images.unsplash.com/photo-1700727448686-b314cb5f9948?w=500&h=240&fit=crop&auto=format",
  services:      "https://images.unsplash.com/photo-1627327053419-fe894c4650ed?w=500&h=240&fit=crop&auto=format",
  technology:    "https://images.unsplash.com/photo-1651340608985-d25cc73156e8?w=500&h=240&fit=crop&auto=format",
  energy:        "https://images.unsplash.com/photo-1588011930968-eadac80e6a5a?w=500&h=240&fit=crop&auto=format",
  defense_ind:   "https://images.unsplash.com/photo-1693515156811-25156b5345a3?w=500&h=240&fit=crop&auto=format",
  infantry:      "https://images.unsplash.com/photo-1630534658718-395efda906cb?w=500&h=240&fit=crop&auto=format",
  armored:       "https://images.unsplash.com/photo-1693515157462-e217eec5e786?w=500&h=240&fit=crop&auto=format",
  artillery:     "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=500&h=240&fit=crop&auto=format",
  special_ops:   "https://images.unsplash.com/flagged/photo-1560177776-295b9cd779de?w=500&h=240&fit=crop&auto=format",
  destroyer:     "https://images.unsplash.com/photo-1719553946838-1190abdeee92?w=500&h=240&fit=crop&auto=format",
  frigate:       "https://images.unsplash.com/photo-1708342421457-9c59f4843fe1?w=500&h=240&fit=crop&auto=format",
  submarine:     "https://images.unsplash.com/photo-1775384222998-c3b458424353?w=500&h=240&fit=crop&auto=format",
  fighter:       "https://images.unsplash.com/photo-1689182314475-ff55f109b430?w=500&h=240&fit=crop&auto=format",
  bomber:        "https://images.unsplash.com/photo-1536714303373-a2114b28b6b7?w=500&h=240&fit=crop&auto=format",
  drone:         "https://images.unsplash.com/photo-1514598800938-f7125ea1aa1c?w=500&h=240&fit=crop&auto=format",
  map:           "https://images.unsplash.com/photo-1543191879-742cb35a3a4e?w=1200&h=400&fit=crop&auto=format",
  parliament:    "https://images.unsplash.com/photo-1524634036752-81ec41a4f1ea?w=500&h=240&fit=crop&auto=format",
  economy_bg:    "https://images.unsplash.com/photo-1605702012553-e954fbde66eb?w=500&h=240&fit=crop&auto=format",
  defense_bg:    "https://images.unsplash.com/photo-1678818048682-44b5cc5375a1?w=500&h=240&fit=crop&auto=format",
  foreign_bg:    "https://images.unsplash.com/photo-1770308144171-77831cf9130a?w=500&h=240&fit=crop&auto=format",
} as const;

// ─── DATA ────────────────────────────────────────────────────────────────────
const SECTORS = [
  { id: "services",      img: IMG.services,      name: "Services",       gdp: 37.6, growth: +3.1, level: 4 },
  { id: "industry",      img: IMG.industry,      name: "Heavy Industry", gdp: 21.4, growth: +1.8, level: 3 },
  { id: "manufacturing", img: IMG.manufacturing, name: "Manufacturing",  gdp: 12.8, growth: +0.9, level: 3 },
  { id: "technology",    img: IMG.technology,    name: "Technology",     gdp: 11.4, growth: +6.7, level: 2 },
  { id: "agriculture",   img: IMG.agriculture,   name: "Agriculture",    gdp: 8.2,  growth: -0.3, level: 3 },
  { id: "energy",        img: IMG.energy,        name: "Energy",         gdp: 5.3,  growth: -1.2, level: 2 },
  { id: "defense_ind",   img: IMG.defense_ind,   name: "Defense Ind.",   gdp: 3.3,  growth: +2.1, level: 4 },
];

const MILITARY = [
  { branch: "ARMY", img: IMG.infantry,   unit: "Infantry Division",  count: 12, str: 94,  status: "READY",    stars: 4, maint: 2.4 },
  { branch: "ARMY", img: IMG.armored,    unit: "Armored Brigade",    count: 4,  str: 88,  status: "READY",    stars: 3, maint: 5.1 },
  { branch: "ARMY", img: IMG.artillery,  unit: "Artillery Regiment", count: 6,  str: 91,  status: "TRAINING", stars: 3, maint: 1.8 },
  { branch: "ARMY", img: IMG.special_ops,unit: "Special Operations", count: 2,  str: 100, status: "READY",    stars: 5, maint: 3.7 },
  { branch: "NAVY", img: IMG.destroyer,  unit: "Destroyer",          count: 8,  str: 82,  status: "PATROL",   stars: 3, maint: 8.4 },
  { branch: "NAVY", img: IMG.frigate,    unit: "Frigate",            count: 14, str: 79,  status: "PATROL",   stars: 2, maint: 4.2 },
  { branch: "NAVY", img: IMG.submarine,  unit: "Submarine",          count: 4,  str: 95,  status: "READY",    stars: 4, maint: 11.2 },
  { branch: "NAVY", img: IMG.frigate,    unit: "Carrier Group",      count: 1,  str: 87,  status: "REFIT",    stars: 5, maint: 28.6 },
  { branch: "AIR",  img: IMG.fighter,    unit: "Fighter Squadron",   count: 6,  str: 91,  status: "READY",    stars: 4, maint: 9.3 },
  { branch: "AIR",  img: IMG.bomber,     unit: "Bomber Wing",        count: 2,  str: 76,  status: "TRAINING", stars: 3, maint: 14.7 },
  { branch: "AIR",  img: IMG.drone,      unit: "Drone Fleet",        count: 3,  str: 98,  status: "ACTIVE",   stars: 5, maint: 2.1 },
];

const RECRUITS = [
  { id: "inf", img: IMG.infantry,    name: "Infantry Div.", branch: "ARMY", cost: 4.2,  months: 6  },
  { id: "arm", img: IMG.armored,     name: "Armored Brig.", branch: "ARMY", cost: 18.7, months: 12 },
  { id: "sof", img: IMG.special_ops, name: "Special Ops",   branch: "ARMY", cost: 11.4, months: 18 },
  { id: "des", img: IMG.destroyer,   name: "Destroyer",     branch: "NAVY", cost: 34.8, months: 24 },
  { id: "sub", img: IMG.submarine,   name: "Submarine",     branch: "NAVY", cost: 47.3, months: 30 },
  { id: "fri", img: IMG.frigate,     name: "Frigate",       branch: "NAVY", cost: 18.2, months: 18 },
  { id: "fiq", img: IMG.fighter,     name: "Fighter Sqdn.", branch: "AIR",  cost: 22.6, months: 15 },
  { id: "drn", img: IMG.drone,       name: "Drone Fleet",   branch: "AIR",  cost: 8.9,  months: 9  },
];

const NATIONS = [
  { nation: "Korathia",     flag: "🟦", status: "ALLY",    rel: 87, bg: "from-blue-200 to-blue-100",    threat: "LOW"      },
  { nation: "Ozantia",      flag: "🟩", status: "PARTNER", rel: 72, bg: "from-emerald-200 to-green-100", threat: "LOW"      },
  { nation: "Nortegra",     flag: "🟪", status: "PARTNER", rel: 68, bg: "from-purple-200 to-violet-100", threat: "LOW"      },
  { nation: "Sulvane",      flag: "🟨", status: "NEUTRAL", rel: 61, bg: "from-yellow-200 to-amber-100",  threat: "LOW"      },
  { nation: "Meldova",      flag: "⬜", status: "NEUTRAL", rel: 54, bg: "from-slate-200 to-gray-100",    threat: "MEDIUM"   },
  { nation: "Telmiran",     flag: "🟫", status: "NEUTRAL", rel: 44, bg: "from-stone-300 to-stone-100",   threat: "MEDIUM"   },
  { nation: "Drexan Conf.", flag: "🟧", status: "RIVAL",   rel: 23, bg: "from-orange-200 to-amber-100",  threat: "HIGH"     },
  { nation: "Veskovia",     flag: "🔴", status: "HOSTILE", rel: 8,  bg: "from-red-300 to-red-100",       threat: "CRITICAL" },
];

const EVENTS = [
  {
    id: "veskovia",
    severity: "CRISIS" as const,
    title: "Veskovia Advances on Border",
    desc: "Enemy armored units have breached the northern corridor. Military response required within this turn.",
    img: IMG.infantry,
    action: "Deploy Forces",
    ministry: "defense",
  },
  {
    id: "naval",
    severity: "WARNING" as const,
    title: "Drexan Naval Buildup",
    desc: "Intelligence confirms a 34% increase in Drexan naval activity near Sector 7. Diplomatic channel advised.",
    img: IMG.destroyer,
    action: "Open Dialogue",
    ministry: "foreign",
  },
  {
    id: "tech",
    severity: "OPPORTUNITY" as const,
    title: "Technology Sector Surge",
    desc: "Q3 growth reached 6.7%. Investing now could unlock the Innovation Level — boosting GDP by an estimated 2%.",
    img: IMG.technology,
    action: "Invest Now",
    ministry: "economy",
  },
];

// ─── HELPERS ─────────────────────────────────────────────────────────────────
function sCls(s: string) {
  const u = s.toUpperCase();
  if (["ALLY","READY","ACTIVE","LOW"].some(k => u.includes(k)))      return "bg-green-100 text-green-800 border-green-300";
  if (["PARTNER","PATROL","MEDIUM"].some(k => u.includes(k)))        return "bg-blue-100 text-blue-800 border-blue-300";
  if (["NEUTRAL","TRAINING","REFIT"].some(k => u.includes(k)))       return "bg-stone-100 text-stone-600 border-stone-300";
  if (["RIVAL","HIGH","WARNING"].some(k => u.includes(k)))           return "bg-amber-100 text-amber-800 border-amber-300";
  if (["HOSTILE","CRITICAL","CRISIS"].some(k => u.includes(k)))      return "bg-red-100 text-red-800 border-red-300";
  return "bg-stone-100 text-stone-600 border-stone-300";
}
function strClr(v: number) { return v >= 90 ? "bg-green-500" : v >= 75 ? "bg-amber-500" : "bg-red-500"; }
function strTxt(v: number) { return v >= 90 ? "text-green-700" : v >= 75 ? "text-amber-700" : "text-red-700"; }
function relClr(v: number) { return v >= 70 ? "bg-green-500" : v >= 40 ? "bg-amber-500" : "bg-red-500"; }
function relTxt(v: number) { return v >= 70 ? "text-green-700" : v >= 40 ? "text-amber-700" : "text-red-700"; }
function bClr(b: string)   {
  if (b === "ARMY") return { text: "text-green-700",  bg: "bg-green-50",  border: "border-green-200", Icon: Swords };
  if (b === "NAVY") return { text: "text-blue-700",   bg: "bg-blue-50",   border: "border-blue-200",  Icon: Anchor };
  return                   { text: "text-violet-700", bg: "bg-violet-50", border: "border-violet-200",Icon: Wind   };
}

// ─── ATOMS ───────────────────────────────────────────────────────────────────
function Badge({ label }: { label: string }) {
  return <span className={`inline-block text-[10px] font-semibold px-2 py-0.5 rounded border ${sCls(label)}`}>{label}</span>;
}

function Stars({ n, max = 5 }: { n: number; max?: number }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: max }).map((_, i) => (
        <Star key={i} className={`w-3 h-3 ${i < n ? "text-accent fill-accent" : "text-border"}`} />
      ))}
    </div>
  );
}

function AnimBar({ pct, color, delay = 0, h = "h-2" }: { pct: number; color: string; delay?: number; h?: string }) {
  return (
    <div className={`w-full bg-secondary rounded-full overflow-hidden ${h}`}>
      <motion.div className={`h-full rounded-full ${color}`}
        initial={{ width: 0 }} animate={{ width: `${Math.min(100, pct)}%` }}
        transition={{ duration: 0.9, ease: "easeOut", delay }} />
    </div>
  );
}

// ─── SECTOR CARD ─────────────────────────────────────────────────────────────
function SectorCard({ s, i }: { s: typeof SECTORS[0]; i: number }) {
  const [boosted, setBoosted] = useState(false);
  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
      transition={{ delay: i * 0.06 }}
      className="bg-card rounded-xl overflow-hidden border border-border shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex flex-col group">
      <div className="relative h-32 overflow-hidden">
        <img src={s.img} alt={s.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-black/10 to-transparent" />
        <div className="absolute top-2 right-2">
          <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${s.growth >= 0 ? "bg-green-500 text-white" : "bg-red-500 text-white"}`}>
            {s.growth >= 0 ? "▲" : "▼"} {Math.abs(s.growth).toFixed(1)}%
          </span>
        </div>
        <div className="absolute bottom-2 left-3">
          <h3 className="text-sm font-bold text-white drop-shadow">{s.name}</h3>
          <Stars n={s.level} />
        </div>
      </div>
      <div className="p-3 flex-1 flex flex-col gap-2">
        <div className="flex justify-between items-center">
          <span className="text-xs text-muted-foreground font-medium">GDP Share</span>
          <span className="text-xl font-mono font-bold text-foreground">{s.gdp.toFixed(1)}%</span>
        </div>
        <AnimBar pct={s.gdp} color="bg-primary" delay={i * 0.06} />
        <div className="text-[10px] text-muted-foreground">Investment Level {s.level}/5</div>
      </div>
      <button onClick={() => setBoosted(v => !v)}
        className={`w-full py-2.5 text-xs font-bold tracking-wide rounded-b-xl transition-all duration-200 ${
          boosted ? "bg-green-600 text-white" : "bg-accent text-white hover:bg-accent/90"
        }`}>
        {boosted ? "✓ Investing" : "⬆ Invest"}
      </button>
    </motion.div>
  );
}

// ─── UNIT CARD ───────────────────────────────────────────────────────────────
function UnitCard({ u, i }: { u: typeof MILITARY[0]; i: number }) {
  const c = bClr(u.branch);
  return (
    <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
      transition={{ delay: i * 0.06 }}
      className={`bg-card rounded-xl overflow-hidden border ${c.border} shadow-sm hover:shadow-md hover:-translate-y-0.5 transition-all duration-300 flex flex-col group`}>
      <div className="relative h-28 overflow-hidden">
        <img src={u.img} alt={u.unit} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
        <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
        <div className="absolute top-2 left-2"><Badge label={u.status} /></div>
        <div className="absolute top-2 right-2 bg-black/60 rounded-full px-2 py-0.5 text-sm font-mono font-bold text-white">×{u.count}</div>
        <div className="absolute bottom-2 left-3">
          <p className="text-xs font-bold text-white">{u.unit}</p>
          <Stars n={u.stars} />
        </div>
      </div>
      <div className="p-3 flex-1 space-y-2">
        <div className="flex items-center gap-2">
          <span className="text-[10px] text-muted-foreground w-14 shrink-0">Strength</span>
          <AnimBar pct={u.str} color={strClr(u.str)} h="h-1.5" />
          <span className={`text-xs font-mono font-bold w-8 text-right ${strTxt(u.str)}`}>{u.str}%</span>
        </div>
        <p className="text-[10px] text-muted-foreground">${u.maint.toFixed(1)}B/yr maintenance</p>
      </div>
      <div className="grid grid-cols-2 border-t border-border">
        <button className="py-2 text-[10px] font-semibold text-primary hover:bg-primary/8 transition-colors rounded-bl-xl">Redeploy</button>
        <button className="py-2 text-[10px] font-semibold text-accent hover:bg-accent/8 border-l border-border transition-colors rounded-br-xl">⬆ Upgrade</button>
      </div>
    </motion.div>
  );
}

// ─── NATION CARD ─────────────────────────────────────────────────────────────
function NationCard({ n, i, onClick }: { n: typeof NATIONS[0]; i: number; onClick: () => void }) {
  const isHostile = n.status === "HOSTILE";
  const isRival   = n.status === "RIVAL";
  return (
    <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
      transition={{ delay: i * 0.055 }}
      className={`bg-card rounded-xl overflow-hidden border shadow-sm hover:shadow-lg hover:-translate-y-1 transition-all duration-300 flex flex-col group cursor-pointer ${
        isHostile ? "border-red-300 shadow-red-100" : isRival ? "border-amber-300" : "border-border"
      }`} onClick={onClick}>
      <div className={`h-24 bg-gradient-to-br ${n.bg} flex items-center justify-center relative overflow-hidden`}>
        <span className="text-5xl group-hover:scale-110 transition-transform duration-400">{n.flag}</span>
        {isHostile && (
          <motion.div animate={{ opacity: [0.4, 1, 0.4] }} transition={{ repeat: Infinity, duration: 1.5 }}
            className="absolute inset-0 border-4 border-red-400/50 rounded-t-xl pointer-events-none" />
        )}
        <div className="absolute top-2 right-2"><Badge label={n.status} /></div>
      </div>
      <div className="p-3 flex-1 space-y-2">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-bold text-foreground">{n.nation}</h3>
          <Badge label={n.threat} />
        </div>
        <div className="space-y-1">
          <div className="flex justify-between text-[10px]">
            <span className="text-muted-foreground">Relations</span>
            <span className={`font-mono font-bold ${relTxt(n.rel)}`}>{n.rel}/100</span>
          </div>
          <AnimBar pct={n.rel} color={relClr(n.rel)} delay={i * 0.055} h="h-1.5" />
        </div>
      </div>
      <button className={`w-full py-2 text-[10px] font-bold tracking-wide rounded-b-xl transition-colors ${
        isHostile ? "bg-red-600 text-white hover:bg-red-700"
        : isRival ? "bg-amber-500 text-white hover:bg-amber-600"
        : "bg-primary text-white hover:bg-primary/90"
      }`}>
        {isHostile ? "⚔ Respond to Threat" : isRival ? "🤝 Open Negotiations" : "💬 Engage"}
      </button>
    </motion.div>
  );
}

// ─── DASHBOARD ───────────────────────────────────────────────────────────────
function Dashboard({ setView }: { setView: (v: string) => void }) {
  const vitals = [
    { Icon: DollarSign, label: "Treasury",    value: "$124.7B", trend: -3.2, warn: true,  color: "bg-amber-50 border-amber-200" },
    { Icon: Swords,     label: "Mil. Power",  value: "8,400",   trend: +120, warn: false, color: "bg-blue-50 border-blue-200"  },
    { Icon: Shield,     label: "Stability",   value: "73%",     trend: -1.2, warn: true,  color: "bg-orange-50 border-orange-200" },
    { Icon: Users,      label: "Approval",    value: "61%",     trend: +1.8, warn: false, color: "bg-green-50 border-green-200" },
  ];

  const severityStyle = {
    CRISIS:      { header: "bg-red-600",    btn: "bg-red-600 hover:bg-red-700 text-white",     badge: "bg-red-600 text-white" },
    WARNING:     { header: "bg-amber-500",  btn: "bg-amber-500 hover:bg-amber-600 text-white", badge: "bg-amber-500 text-white" },
    OPPORTUNITY: { header: "bg-green-600",  btn: "bg-green-600 hover:bg-green-700 text-white", badge: "bg-green-600 text-white" },
  };

  const shortcuts = [
    { id: "economy",  label: "Economy",       sub: "GDP $892.4B · +2.3%",      img: IMG.economy_bg,  Icon: DollarSign },
    { id: "defense",  label: "Defense",       sub: "8,400 power · 87% ready",  img: IMG.defense_bg,  Icon: Shield     },
    { id: "foreign",  label: "Foreign Affs.", sub: "3 allies · 1 crisis",       img: IMG.foreign_bg,  Icon: Globe      },
    { id: "domestic", label: "Domestic",      sub: "Clearance required",        img: IMG.parliament,  Icon: Landmark   },
  ];

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      {/* Map hero */}
      <div className="relative h-44 overflow-hidden">
        <img src={IMG.map} alt="Strategic Map" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-b from-background/60 via-transparent to-background/80" />
        <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.6 }}
          className="absolute inset-0 flex flex-col items-center justify-center text-center">
          <p className="text-xs font-semibold tracking-[0.4em] text-primary/80 uppercase mb-1">The Republic of</p>
          <h1 className="text-5xl font-[Cinzel] font-black text-primary drop-shadow-sm">VELTRIA</h1>
          <p className="text-sm text-foreground/70 mt-1 font-medium">Chancellor M. Draven · {EVENTS.length} matters require attention</p>
        </motion.div>
      </div>

      <div className="p-5 space-y-6">
        {/* Empire vitals */}
        <div>
          <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase mb-3">Empire Status</h2>
          <div className="grid grid-cols-4 gap-3">
            {vitals.map(({ Icon, label, value, trend, warn, color }, i) => (
              <motion.div key={label} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.07 }}
                className={`rounded-xl border-2 p-4 text-center ${color}`}>
                <Icon className={`w-6 h-6 mx-auto mb-2 ${warn ? "text-amber-600" : "text-primary"}`} />
                <div className="text-2xl font-mono font-bold text-foreground">{value}</div>
                <div className="text-[10px] font-semibold text-muted-foreground mt-0.5">{label}</div>
                <div className={`text-xs font-mono mt-1 font-semibold ${trend > 0 ? "text-green-600" : "text-red-600"}`}>
                  {trend > 0 ? "▲" : "▼"} {Math.abs(trend)}
                </div>
              </motion.div>
            ))}
          </div>
        </div>

        {/* Active situations */}
        <div>
          <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase mb-3">Active Situations</h2>
          <div className="grid grid-cols-3 gap-4">
            {EVENTS.map((ev, i) => {
              const s = severityStyle[ev.severity];
              return (
                <motion.div key={ev.id} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.2 + i * 0.08 }}
                  className="bg-card rounded-xl overflow-hidden border border-border shadow-sm hover:shadow-md transition-all duration-300">
                  <div className="relative h-28 overflow-hidden">
                    <img src={ev.img} alt={ev.title} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                    <div className="absolute top-2 left-2">
                      <span className={`text-[10px] font-bold px-2 py-0.5 rounded ${s.badge}`}>{ev.severity}</span>
                    </div>
                  </div>
                  <div className="p-3">
                    <h3 className="text-sm font-bold text-foreground leading-snug mb-1">{ev.title}</h3>
                    <p className="text-[11px] text-muted-foreground leading-relaxed mb-3">{ev.desc}</p>
                    <button onClick={() => setView(ev.ministry)}
                      className={`w-full py-2 text-xs font-bold rounded-lg transition-colors ${s.btn}`}>
                      {ev.action} →
                    </button>
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>

        {/* Ministry shortcuts */}
        <div>
          <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase mb-3">Ministries</h2>
          <div className="grid grid-cols-4 gap-3">
            {shortcuts.map(({ id, label, sub, img, Icon }, i) => (
              <motion.div key={id} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.35 + i * 0.07 }}
                onClick={() => setView(id)}
                className="bg-card rounded-xl overflow-hidden border border-border shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300 cursor-pointer group">
                <div className="relative h-20 overflow-hidden">
                  <img src={img} alt={label} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent" />
                  <Icon className="absolute bottom-2 left-3 w-5 h-5 text-white drop-shadow" />
                </div>
                <div className="p-3">
                  <div className="text-sm font-bold text-foreground">{label}</div>
                  <div className="text-[10px] text-muted-foreground mt-0.5">{sub}</div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── ECONOMY PANEL ───────────────────────────────────────────────────────────
function EconomyPanel() {
  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <div className="relative h-32 overflow-hidden">
        <img src={IMG.economy_bg} alt="Economy" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/90 to-transparent" />
        <div className="absolute inset-0 flex items-end p-5">
          <div>
            <p className="text-[9px] font-bold tracking-[0.4em] text-primary/70 uppercase">Ministry of</p>
            <h1 className="text-3xl font-[Cinzel] font-bold text-primary">ECONOMY</h1>
          </div>
          <div className="ml-auto flex gap-3">
            {[{ l: "GDP", v: "$892.4B" }, { l: "Growth", v: "+2.3%" }, { l: "Inflation", v: "3.7%" }].map(s => (
              <div key={s.l} className="bg-white/80 backdrop-blur rounded-lg px-3 py-2 text-center shadow-sm">
                <div className="text-[9px] text-muted-foreground font-semibold">{s.l}</div>
                <div className="text-sm font-mono font-bold text-foreground">{s.v}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="p-5 space-y-4">
        <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase">Economic Sectors</h2>
        <div className="grid grid-cols-4 gap-3">
          {SECTORS.map((s, i) => <SectorCard key={s.id} s={s} i={i} />)}
        </div>
      </div>
    </div>
  );
}

// ─── DEFENSE PANEL ───────────────────────────────────────────────────────────
function DefensePanel() {
  const [branch, setBranch] = useState<"ARMY" | "NAVY" | "AIR">("ARMY");
  const [qty, setQty] = useState<Record<string, number>>({});
  const upd = (id: string, d: number) => setQty(q => ({ ...q, [id]: Math.max(0, (q[id] ?? 0) + d) }));
  const totalCost = RECRUITS.filter(r => r.branch === branch).reduce((s, u) => s + (qty[u.id] ?? 0) * u.cost, 0);

  const branches: Array<"ARMY" | "NAVY" | "AIR"> = ["ARMY", "NAVY", "AIR"];
  const branchStats = {
    ARMY: { units: 24, str: 91, icon: Swords, color: "bg-green-600" },
    NAVY: { units: 27, str: 85, icon: Anchor, color: "bg-blue-600"  },
    AIR:  { units: 11, str: 88, icon: Wind,   color: "bg-violet-600"},
  };

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <div className="relative h-32 overflow-hidden">
        <img src={IMG.defense_bg} alt="Defense" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/90 to-transparent" />
        <div className="absolute inset-0 flex items-end p-5">
          <div>
            <p className="text-[9px] font-bold tracking-[0.4em] text-primary/70 uppercase">Ministry of</p>
            <h1 className="text-3xl font-[Cinzel] font-bold text-primary">DEFENSE</h1>
          </div>
          <div className="ml-auto flex gap-3">
            {[{ l: "Power", v: "8,400 pts" }, { l: "Personnel", v: "847K" }, { l: "Readiness", v: "87.3%" }].map(s => (
              <div key={s.l} className="bg-white/80 backdrop-blur rounded-lg px-3 py-2 text-center shadow-sm">
                <div className="text-[9px] text-muted-foreground font-semibold">{s.l}</div>
                <div className="text-sm font-mono font-bold text-foreground">{s.v}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="p-5 space-y-5">
        {/* Branch selector */}
        <div className="grid grid-cols-3 gap-3">
          {branches.map(b => {
            const { units, str, icon: BIcon, color } = branchStats[b];
            const c = bClr(b);
            const active = branch === b;
            return (
              <button key={b} onClick={() => setBranch(b)}
                className={`rounded-xl border-2 p-4 text-left transition-all duration-200 ${
                  active ? `${c.border} ${c.bg} shadow-md` : "border-border bg-card hover:border-border/70"
                }`}>
                <div className="flex items-center gap-2 mb-2">
                  <div className={`p-1.5 rounded-lg ${active ? color : "bg-secondary"}`}>
                    <BIcon className={`w-4 h-4 ${active ? "text-white" : "text-muted-foreground"}`} />
                  </div>
                  <span className={`font-bold text-sm ${active ? c.text : "text-foreground"}`}>{b}</span>
                </div>
                <div className="text-xs text-muted-foreground">{units} units deployed</div>
                <AnimBar pct={str} color={active ? color : "bg-secondary-foreground/40"} h="h-1.5" />
                <div className={`text-[10px] font-mono font-bold mt-1 ${active ? c.text : "text-muted-foreground"}`}>{str}% ready</div>
              </button>
            );
          })}
        </div>

        {/* Units */}
        <div>
          <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase mb-3">
            {branch} Units
          </h2>
          <motion.div key={branch} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.2 }}
            className="grid grid-cols-4 gap-3">
            {MILITARY.filter(u => u.branch === branch).map((u, i) => <UnitCard key={u.unit} u={u} i={i} />)}
          </motion.div>
        </div>

        {/* Recruitment */}
        <div>
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase">Recruit {branch}</h2>
            {totalCost > 0 && (
              <button className="bg-primary text-white text-xs font-bold px-4 py-1.5 rounded-lg hover:bg-primary/90 transition-colors">
                Commission · ${totalCost.toFixed(1)}B
              </button>
            )}
          </div>
          <div className="grid grid-cols-4 gap-3">
            {RECRUITS.filter(r => r.branch === branch).map((u, i) => (
              <motion.div key={u.id} initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: i * 0.06 }}
                className={`bg-card rounded-xl overflow-hidden border transition-all duration-200 ${
                  (qty[u.id] ?? 0) > 0 ? "border-primary shadow-md shadow-primary/15" : "border-border"
                }`}>
                <div className="relative h-24 overflow-hidden">
                  <img src={u.img} alt={u.name} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/70 to-transparent" />
                  <div className="absolute bottom-2 left-2">
                    <p className="text-xs font-bold text-white">{u.name}</p>
                    <p className="text-lg font-mono font-black text-accent">${u.cost.toFixed(1)}B</p>
                  </div>
                </div>
                <div className="p-2.5">
                  <div className="flex items-center gap-1 text-[9px] text-muted-foreground mb-2">
                    <Clock className="w-3 h-3" />{u.months}mo build time
                  </div>
                  <div className="flex items-center gap-1">
                    <button onClick={() => upd(u.id, -1)} className="w-7 h-7 rounded-lg bg-secondary text-foreground hover:bg-secondary/70 flex items-center justify-center transition-colors">
                      <Minus className="w-3 h-3" />
                    </button>
                    <span className="flex-1 text-center text-base font-mono font-bold text-foreground">{qty[u.id] ?? 0}</span>
                    <button onClick={() => upd(u.id, +1)} className="w-7 h-7 rounded-lg bg-primary text-white hover:bg-primary/80 flex items-center justify-center transition-colors">
                      <Plus className="w-3 h-3" />
                    </button>
                  </div>
                </div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── FOREIGN PANEL ───────────────────────────────────────────────────────────
function ForeignPanel() {
  const [selected, setSelected] = useState<string | null>(null);
  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <div className="relative h-32 overflow-hidden">
        <img src={IMG.foreign_bg} alt="Foreign Affairs" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-r from-background/90 to-transparent" />
        <div className="absolute inset-0 flex items-end p-5">
          <div>
            <p className="text-[9px] font-bold tracking-[0.4em] text-primary/70 uppercase">Ministry of</p>
            <h1 className="text-3xl font-[Cinzel] font-bold text-primary">FOREIGN AFFAIRS</h1>
          </div>
          <div className="ml-auto flex gap-3">
            {[{ l: "Allies", v: "3" }, { l: "Treaties", v: "5 Active" }, { l: "Threats", v: "1 Critical" }].map(s => (
              <div key={s.l} className="bg-white/80 backdrop-blur rounded-lg px-3 py-2 text-center shadow-sm">
                <div className="text-[9px] text-muted-foreground font-semibold">{s.l}</div>
                <div className="text-sm font-mono font-bold text-foreground">{s.v}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="p-5 space-y-4">
        <h2 className="text-[10px] font-bold tracking-[0.3em] text-muted-foreground uppercase">World Relations</h2>
        <div className="grid grid-cols-4 gap-3">
          {NATIONS.map((n, i) => (
            <NationCard key={n.nation} n={n} i={i}
              onClick={() => setSelected(selected === n.nation ? null : n.nation)} />
          ))}
        </div>
        {selected && (() => {
          const n = NATIONS.find(x => x.nation === selected)!;
          return (
            <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
              className="bg-card border border-border rounded-xl p-5 shadow-md">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <span className="text-3xl">{n.flag}</span>
                  <div>
                    <h3 className="text-lg font-[Cinzel] font-bold text-foreground">{n.nation}</h3>
                    <div className="flex gap-2 mt-1"><Badge label={n.status} /><Badge label={n.threat} /></div>
                  </div>
                </div>
                <button onClick={() => setSelected(null)} className="text-muted-foreground hover:text-foreground text-xl leading-none">×</button>
              </div>
              <div className="grid grid-cols-3 gap-3">
                {[
                  { l: "Diplomatic Actions Available", btns: ["Propose Treaty", "Gift Aid", "Request Meeting"] },
                  { l: "Economic Options", btns: ["Offer Trade Deal", "Impose Tariff", "Sanction"] },
                  { l: "Military Options", btns: ["Joint Exercise", "Deploy Attaché", "Show of Force"] },
                ].map(col => (
                  <div key={col.l}>
                    <p className="text-[9px] font-bold tracking-wider text-muted-foreground uppercase mb-2">{col.l}</p>
                    <div className="space-y-1.5">
                      {col.btns.map(btn => (
                        <button key={btn} className="w-full text-left text-xs py-2 px-3 rounded-lg border border-border hover:border-primary hover:bg-primary/5 text-foreground transition-colors">
                          {btn}
                        </button>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            </motion.div>
          );
        })()}
      </div>
    </div>
  );
}

// ─── LOCKED PANEL ────────────────────────────────────────────────────────────
function LockedPanel({ label }: { label: string }) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-4 p-10 text-center">
      <div className="w-20 h-20 rounded-2xl bg-muted flex items-center justify-center">
        <Lock className="w-10 h-10 text-muted-foreground/40" />
      </div>
      <div>
        <h2 className="font-[Cinzel] font-bold text-xl text-foreground mb-1">{label}</h2>
        <p className="text-sm text-muted-foreground">This ministry requires Security Clearance Level 5.</p>
        <button className="mt-4 px-6 py-2 bg-primary/10 border border-primary/30 text-primary text-sm font-semibold rounded-lg hover:bg-primary/20 transition-colors">
          Request Access
        </button>
      </div>
    </div>
  );
}

// ─── TOP HUD ─────────────────────────────────────────────────────────────────
function TopHUD() {
  const resources = [
    { Icon: DollarSign, label: "GDP",      value: "$892B", trend: +1, color: "text-green-300" },
    { Icon: Shield,     label: "Stability",value: "73%",   trend: -1, color: "text-amber-300" },
    { Icon: Swords,     label: "Military", value: "8,400", trend: +1, color: "text-green-300" },
    { Icon: Scroll,     label: "Treasury", value: "$124B", trend: -1, color: "text-amber-300" },
    { Icon: Users,      label: "Approval", value: "61%",   trend: +1, color: "text-green-300" },
  ];
  return (
    <header className="bg-primary shrink-0 flex items-center h-13 px-4 gap-4 shadow-lg">
      <div className="flex items-center gap-2 shrink-0 pr-4 border-r border-white/20">
        <div className="w-7 h-7 rounded bg-white/10 flex items-center justify-center">
          <Landmark className="w-4 h-4 text-white" />
        </div>
        <div>
          <div className="text-sm font-[Cinzel] font-bold text-white leading-none">VELTRIA</div>
          <div className="text-[9px] text-white/60 leading-none mt-0.5">2031 · Quarter III</div>
        </div>
      </div>
      <div className="flex flex-1 gap-1">
        {resources.map(({ Icon, label, value, trend, color }) => (
          <div key={label} className="flex items-center gap-1.5 bg-white/10 hover:bg-white/15 rounded-lg px-3 py-1.5 transition-colors cursor-default">
            <Icon className="w-3.5 h-3.5 text-white/70" />
            <div>
              <div className="text-[8px] text-white/60 leading-none">{label}</div>
              <div className={`text-xs font-mono font-bold leading-none mt-0.5 ${color}`}>{value}
                <span className="ml-0.5 text-[8px]">{trend > 0 ? "▲" : "▼"}</span>
              </div>
            </div>
          </div>
        ))}
      </div>
      <div className="flex items-center gap-2 pl-4 border-l border-white/20 shrink-0">
        <motion.div animate={{ scale: [1, 1.3, 1] }} transition={{ repeat: Infinity, duration: 2 }}
          className="w-2 h-2 rounded-full bg-red-400" />
        <div className="text-xs text-white/80 font-semibold">3 Alerts</div>
        <div className="ml-2 bg-accent text-white text-[10px] font-bold px-3 py-1.5 rounded-lg hover:bg-accent/90 transition-colors cursor-pointer">
          End Turn ▶
        </div>
      </div>
    </header>
  );
}

// ─── BOTTOM NAV ──────────────────────────────────────────────────────────────
function BottomNav({ view, setView }: { view: string; setView: (v: string) => void }) {
  const tabs = [
    { id: "overview",      label: "Overview",     Icon: Landmark   },
    { id: "economy",       label: "Economy",      Icon: DollarSign },
    { id: "defense",       label: "Defense",      Icon: Shield     },
    { id: "foreign",       label: "Foreign",      Icon: Globe      },
    { id: "intelligence",  label: "Intel",        Icon: Eye        },
  ];
  const alerts: Record<string, number> = { overview: 3, defense: 1, foreign: 2 };
  return (
    <nav className="bg-primary/95 backdrop-blur border-t border-white/10 shrink-0 flex">
      {tabs.map(({ id, label, Icon }) => {
        const active = view === id;
        const alert = alerts[id];
        return (
          <button key={id} onClick={() => setView(id)}
            className={`flex-1 flex flex-col items-center justify-center py-2.5 gap-1 relative transition-all duration-200 ${
              active ? "text-white" : "text-white/50 hover:text-white/80"
            }`}>
            {active && (
              <motion.div layoutId="bottom-active" className="absolute inset-0 bg-white/10 rounded-none"
                transition={{ type: "spring", stiffness: 500, damping: 40 }} />
            )}
            {active && <div className="absolute top-0 left-1/4 right-1/4 h-0.5 bg-accent rounded-full" />}
            <div className="relative">
              <Icon className="w-5 h-5" />
              {alert && (
                <span className="absolute -top-1 -right-1.5 w-3.5 h-3.5 bg-red-500 rounded-full text-[8px] text-white font-bold flex items-center justify-center">{alert}</span>
              )}
            </div>
            <span className="text-[9px] font-semibold tracking-wide relative">{label}</span>
          </button>
        );
      })}
    </nav>
  );
}

// ─── APP ─────────────────────────────────────────────────────────────────────
export default function App() {
  const [view, setView] = useState("overview");
  return (
    <div className="h-screen flex flex-col bg-background overflow-hidden font-sans">
      <TopHUD />
      <motion.div key={view} initial={{ opacity: 0, y: 6 }} animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.2 }} className="flex-1 flex flex-col overflow-hidden">
        {view === "overview"     && <Dashboard setView={setView} />}
        {view === "economy"      && <EconomyPanel />}
        {view === "defense"      && <DefensePanel />}
        {view === "foreign"      && <ForeignPanel />}
        {view === "domestic"     && <LockedPanel label="Domestic Policy" />}
        {view === "intelligence" && <LockedPanel label="Intelligence" />}
      </motion.div>
      <BottomNav view={view} setView={setView} />
    </div>
  );
}
