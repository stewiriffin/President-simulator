import { useState } from "react";
import { motion } from "motion/react";
import {
  Star, Shield, Globe, DollarSign, Swords, Anchor, Wind,
  Users, Eye, Landmark, Lock, ChevronRight, Zap,
  TrendingUp, TrendingDown, AlertTriangle, Clock,
  Plus, Minus, Trophy, Flame, Settings, BarChart2, BookOpen
} from "lucide-react";

// ─── IMAGES ──────────────────────────────────────────────────────────────────
const IMG = {
  agriculture:   "https://images.unsplash.com/photo-1508175688576-0c076b47b5b5?w=600&h=300&fit=crop&auto=format",
  industry:      "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=600&h=300&fit=crop&auto=format",
  manufacturing: "https://images.unsplash.com/photo-1700727448686-b314cb5f9948?w=600&h=300&fit=crop&auto=format",
  services:      "https://images.unsplash.com/photo-1627327053419-fe894c4650ed?w=600&h=300&fit=crop&auto=format",
  technology:    "https://images.unsplash.com/photo-1651340608985-d25cc73156e8?w=600&h=300&fit=crop&auto=format",
  energy:        "https://images.unsplash.com/photo-1588011930968-eadac80e6a5a?w=600&h=300&fit=crop&auto=format",
  defense_ind:   "https://images.unsplash.com/photo-1693515156811-25156b5345a3?w=600&h=300&fit=crop&auto=format",
  infantry:      "https://images.unsplash.com/photo-1630534658718-395efda906cb?w=600&h=300&fit=crop&auto=format",
  armored:       "https://images.unsplash.com/photo-1693515157462-e217eec5e786?w=600&h=300&fit=crop&auto=format",
  artillery:     "https://images.unsplash.com/photo-1517065963912-27f75001ebe2?w=600&h=300&fit=crop&auto=format",
  special_ops:   "https://images.unsplash.com/flagged/photo-1560177776-295b9cd779de?w=600&h=300&fit=crop&auto=format",
  destroyer:     "https://images.unsplash.com/photo-1719553946838-1190abdeee92?w=600&h=300&fit=crop&auto=format",
  frigate:       "https://images.unsplash.com/photo-1708342421457-9c59f4843fe1?w=600&h=300&fit=crop&auto=format",
  submarine:     "https://images.unsplash.com/photo-1775384222998-c3b458424353?w=600&h=300&fit=crop&auto=format",
  fighter:       "https://images.unsplash.com/photo-1689182314475-ff55f109b430?w=600&h=300&fit=crop&auto=format",
  bomber:        "https://images.unsplash.com/photo-1536714303373-a2114b28b6b7?w=600&h=300&fit=crop&auto=format",
  drone:         "https://images.unsplash.com/photo-1514598800938-f7125ea1aa1c?w=600&h=300&fit=crop&auto=format",
  map:           "https://images.unsplash.com/photo-1543191879-742cb35a3a4e?w=1400&h=500&fit=crop&auto=format",
  econ_bg:       "https://images.unsplash.com/photo-1605702012553-e954fbde66eb?w=1400&h=300&fit=crop&auto=format",
  def_bg:        "https://images.unsplash.com/photo-1678818048682-44b5cc5375a1?w=1400&h=300&fit=crop&auto=format",
  for_bg:        "https://images.unsplash.com/photo-1770308144171-77831cf9130a?w=1400&h=300&fit=crop&auto=format",
} as const;

// ─── GAME DATA ───────────────────────────────────────────────────────────────
const SECTORS = [
  { id: "services",      img: IMG.services,      name: "Services",       gdp: 37.6, growth: +3.1, level: 4, xp: 72, revenue: 336 },
  { id: "industry",      img: IMG.industry,      name: "Heavy Industry", gdp: 21.4, growth: +1.8, level: 3, xp: 45, revenue: 191 },
  { id: "manufacturing", img: IMG.manufacturing, name: "Manufacturing",  gdp: 12.8, growth: +0.9, level: 3, xp: 28, revenue: 114 },
  { id: "technology",    img: IMG.technology,    name: "Technology",     gdp: 11.4, growth: +6.7, level: 2, xp: 88, revenue: 102 },
  { id: "agriculture",   img: IMG.agriculture,   name: "Agriculture",    gdp: 8.2,  growth: -0.3, level: 3, xp: 12, revenue: 73  },
  { id: "energy",        img: IMG.energy,        name: "Energy",         gdp: 5.3,  growth: -1.2, level: 2, xp: 33, revenue: 47  },
  { id: "defense_ind",   img: IMG.defense_ind,   name: "Defense Ind.",   gdp: 3.3,  growth: +2.1, level: 4, xp: 61, revenue: 29  },
];

const UNITS = [
  { branch: "ARMY", img: IMG.infantry,   name: "Infantry Division",  count: 12, str: 94,  stars: 4, status: "READY",    maint: 2.4 },
  { branch: "ARMY", img: IMG.armored,    name: "Armored Brigade",    count: 4,  str: 88,  stars: 3, status: "READY",    maint: 5.1 },
  { branch: "ARMY", img: IMG.artillery,  name: "Artillery Regiment", count: 6,  str: 91,  stars: 3, status: "TRAINING", maint: 1.8 },
  { branch: "ARMY", img: IMG.special_ops,name: "Special Operations", count: 2,  str: 100, stars: 5, status: "READY",    maint: 3.7 },
  { branch: "NAVY", img: IMG.destroyer,  name: "Destroyer",          count: 8,  str: 82,  stars: 3, status: "PATROL",   maint: 8.4 },
  { branch: "NAVY", img: IMG.frigate,    name: "Frigate",            count: 14, str: 79,  stars: 2, status: "PATROL",   maint: 4.2 },
  { branch: "NAVY", img: IMG.submarine,  name: "Submarine",          count: 4,  str: 95,  stars: 4, status: "READY",    maint: 11.2 },
  { branch: "NAVY", img: IMG.frigate,    name: "Carrier Group",      count: 1,  str: 87,  stars: 5, status: "REFIT",    maint: 28.6 },
  { branch: "AIR",  img: IMG.fighter,    name: "Fighter Squadron",   count: 6,  str: 91,  stars: 4, status: "READY",    maint: 9.3 },
  { branch: "AIR",  img: IMG.bomber,     name: "Bomber Wing",        count: 2,  str: 76,  stars: 3, status: "TRAINING", maint: 14.7 },
  { branch: "AIR",  img: IMG.drone,      name: "Drone Fleet",        count: 3,  str: 98,  stars: 5, status: "ACTIVE",   maint: 2.1 },
];

const RECRUITS = [
  { id: "inf", img: IMG.infantry,    name: "Infantry Div.", branch: "ARMY", cost: 4.2,  months: 6,  power: 320 },
  { id: "arm", img: IMG.armored,     name: "Armored Brig.", branch: "ARMY", cost: 18.7, months: 12, power: 840 },
  { id: "sof", img: IMG.special_ops, name: "Special Ops",   branch: "ARMY", cost: 11.4, months: 18, power: 560 },
  { id: "des", img: IMG.destroyer,   name: "Destroyer",     branch: "NAVY", cost: 34.8, months: 24, power: 1200 },
  { id: "sub", img: IMG.submarine,   name: "Submarine",     branch: "NAVY", cost: 47.3, months: 30, power: 1600 },
  { id: "fri", img: IMG.frigate,     name: "Frigate",       branch: "NAVY", cost: 18.2, months: 18, power: 680 },
  { id: "fiq", img: IMG.fighter,     name: "Fighter Sqdn.", branch: "AIR",  cost: 22.6, months: 15, power: 920 },
  { id: "drn", img: IMG.drone,       name: "Drone Fleet",   branch: "AIR",  cost: 8.9,  months: 9,  power: 440 },
];

const NATIONS = [
  { name: "Korathia",    flag: <Globe className="w-full h-full text-blue-200" />, status: "ALLY",    rel: 87, bg: "from-blue-400 to-blue-600",     threat: "LOW",      action: "Send Aid",   actionColor: "bg-blue-600"    },
  { name: "Ozantia",    flag: <Globe className="w-full h-full text-emerald-200" />, status: "PARTNER", rel: 72, bg: "from-emerald-400 to-green-600",  threat: "LOW",      action: "Trade Deal", actionColor: "bg-emerald-600" },
  { name: "Nortegra",   flag: <Globe className="w-full h-full text-violet-200" />, status: "PARTNER", rel: 68, bg: "from-violet-400 to-purple-600",  threat: "LOW",      action: "Strengthen", actionColor: "bg-violet-600"  },
  { name: "Sulvane",    flag: <Globe className="w-full h-full text-yellow-200" />, status: "NEUTRAL", rel: 61, bg: "from-yellow-400 to-amber-500",   threat: "LOW",      action: "Engage",     actionColor: "bg-amber-500"   },
  { name: "Meldova",    flag: <Globe className="w-full h-full text-slate-200" />, status: "NEUTRAL", rel: 54, bg: "from-slate-400 to-slate-600",    threat: "MEDIUM",   action: "Engage",     actionColor: "bg-slate-600"   },
  { name: "Telmiran",   flag: <Globe className="w-full h-full text-stone-200" />, status: "NEUTRAL", rel: 44, bg: "from-stone-400 to-stone-600",    threat: "MEDIUM",   action: "Engage",     actionColor: "bg-stone-600"   },
  { name: "Drexan",     flag: <Globe className="w-full h-full text-orange-200" />, status: "RIVAL",   rel: 23, bg: "from-orange-400 to-red-500",     threat: "HIGH",     action: "Warn",       actionColor: "bg-orange-600"  },
  { name: "Veskovia",   flag: <Globe className="w-full h-full text-red-200" />, status: "HOSTILE", rel: 8,  bg: "from-red-500 to-red-800",        threat: "CRITICAL", action: "Respond",  actionColor: "bg-red-700"     },
];

const EVENTS = [
  { id: "v", sev: "CRISIS" as const,      title: "Veskovia Advances!", desc: "Armored units breach the northern corridor. Immediate military action required.", img: IMG.infantry,    cta: "Deploy Forces",    ctaMini: "Deploy", nav: "defense",  icon: <AlertTriangle className="w-5 h-5 text-red-500" /> },
  { id: "d", sev: "WARNING" as const,     title: "Drexan Naval Buildup", desc: "34% surge in enemy naval activity near Sector 7 detected by intelligence.", img: IMG.destroyer,   cta: "Open Dialogue",    ctaMini: "Talk",  nav: "foreign",  icon: <AlertTriangle className="w-5 h-5 text-amber-500" /> },
  { id: "t", sev: "OPPORTUNITY" as const, title: "Tech Boom Incoming!", desc: "Your Technology sector is surging +6.7%. Invest now to unlock Level 3 bonus.", img: IMG.technology,  cta: "Invest Now",       ctaMini: "Invest",nav: "economy",  icon: <TrendingUp className="w-5 h-5 text-emerald-500" /> },
];

// ─── HELPERS ─────────────────────────────────────────────────────────────────
type Branch = "ARMY" | "NAVY" | "AIR";

function strColor(v: number) { return v >= 88 ? "#16a34a" : v >= 70 ? "#d97706" : "#dc2626"; }
function relColor(v: number) { return v >= 65 ? "#16a34a" : v >= 35 ? "#d97706" : "#dc2626"; }
function statusStyle(s: string) {
  if (["READY","ACTIVE","ALLY"].includes(s))   return "bg-green-100 text-green-800";
  if (["PATROL","PARTNER"].includes(s))         return "bg-blue-100 text-blue-800";
  if (["TRAINING","NEUTRAL","REFIT"].includes(s))return "bg-amber-100 text-amber-800";
  if (["RIVAL","HIGH"].includes(s))             return "bg-orange-100 text-orange-800";
  if (["HOSTILE","CRITICAL"].includes(s))       return "bg-red-100 text-red-800";
  return "bg-stone-100 text-stone-600";
}
function bInfo(b: Branch) {
  if (b === "ARMY") return { color: "#16a34a", bg: "from-green-600 to-green-800",  Icon: Swords, label: "Ground Forces" };
  if (b === "NAVY") return { color: "#2563eb", bg: "from-blue-600 to-blue-800",    Icon: Anchor, label: "Naval Fleet" };
  return               { color: "#7c3aed", bg: "from-violet-600 to-violet-800",  Icon: Wind,   label: "Air Command" };
}

// ─── ATOMS ───────────────────────────────────────────────────────────────────
function Btn({ children, color = "bg-[#C4882A]", className = "", onClick }: {
  children: React.ReactNode; color?: string; className?: string; onClick?: () => void;
}) {
  return (
    <motion.button whileTap={{ scale: 0.93 }} onClick={onClick}
      className={`${color} text-white font-bold text-sm rounded-xl px-3 py-2 shadow-md active:shadow-sm transition-shadow ${className}`}>
      {children}
    </motion.button>
  );
}

function Stars({ n, max = 5 }: { n: number; max?: number }) {
  return (
    <div className="flex gap-0.5">
      {Array.from({ length: max }).map((_, i) => (
        <Star key={i} className={`w-3.5 h-3.5 ${i < n ? "fill-[#C4882A] text-[#C4882A]" : "text-stone-300"}`} />
      ))}
    </div>
  );
}

function GameBar({ pct, color, delay = 0, h = "h-3" }: { pct: number; color: string; delay?: number; h?: string }) {
  return (
    <div className={`w-full bg-stone-100 rounded-full overflow-hidden ${h}`} style={{ boxShadow: "inset 0 1px 3px rgba(0,0,0,0.15)" }}>
      <motion.div className="h-full rounded-full" style={{ background: color }}
        initial={{ width: 0 }} animate={{ width: `${Math.min(100, pct)}%` }}
        transition={{ duration: 1, ease: "easeOut", delay }} />
    </div>
  );
}

function XPBar({ xp, delay = 0 }: { xp: number; delay?: number }) {
  return (
    <div className="w-full bg-amber-100 rounded-full overflow-hidden h-2">
      <motion.div className="h-full rounded-full bg-gradient-to-r from-[#C4882A] to-amber-400"
        initial={{ width: 0 }} animate={{ width: `${xp}%` }}
        transition={{ duration: 1.1, ease: "easeOut", delay }} />
    </div>
  );
}

function LvBadge({ n }: { n: number }) {
  return (
    <div className="bg-[#1E3A6E] text-white text-[10px] font-black px-2 py-0.5 rounded-md tracking-wide">
      LV.{n}
    </div>
  );
}

function StatusChip({ label }: { label: string }) {
  return <span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${statusStyle(label)}`}>{label}</span>;
}

// ─── SCREEN HEADER ───────────────────────────────────────────────────────────
function ScreenHeader({ img, title, subtitle, stats }: {
  img: string; title: string; subtitle: string;
  stats: { label: string; value: string }[];
}) {
  return (
    <div className="relative h-32 overflow-hidden shrink-0">
      <img src={img} alt={title} className="w-full h-full object-cover" />
      <div className="absolute inset-0 bg-gradient-to-b from-[#1E3A6E]/70 via-[#1E3A6E]/40 to-[#1E3A6E]/80" />
      <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}
        className="absolute inset-0 flex flex-col justify-end p-4">
        <p className="text-white/60 text-[10px] font-bold tracking-[0.4em] uppercase mb-0.5">{subtitle}</p>
        <h1 className="text-4xl font-[Cinzel] font-black text-white drop-shadow-lg mb-2">{title}</h1>
        <div className="flex gap-2">
          {stats.map(s => (
            <div key={s.label} className="bg-white/15 backdrop-blur-sm rounded-xl px-3 py-1.5 border border-white/20">
              <div className="text-[9px] text-white/70 font-semibold">{s.label}</div>
              <div className="text-sm font-mono font-black text-white">{s.value}</div>
            </div>
          ))}
        </div>
      </motion.div>
    </div>
  );
}

// ─── DASHBOARD ───────────────────────────────────────────────────────────────
function Dashboard({ setView, date }: { setView: (v: string) => void; date: Date }) {
  const vitals = [
    { Icon: DollarSign, label: "Treasury",   value: "$124.7B", pct: 62, color: "#C4882A", warn: true,  sub: "↓ Declining"  },
    { Icon: Shield,     label: "Stability",  value: "73%",     pct: 73, color: "#d97706", warn: true,  sub: "↓ At risk"    },
    { Icon: Swords,     label: "Mil. Power", value: "8,400",   pct: 84, color: "#1E3A6E", warn: false, sub: "↑ Increasing" },
    { Icon: Users,      label: "Approval",   value: "61%",     pct: 61, color: "#16a34a", warn: false, sub: "↑ Steady"     },
  ];

  const sevCfg = {
    CRISIS:      { accent: "#dc2626", badge: "bg-red-600",    btn: "bg-red-600",    ring: "ring-red-300" },
    WARNING:     { accent: "#d97706", badge: "bg-amber-500",  btn: "bg-amber-500",  ring: "ring-amber-200" },
    OPPORTUNITY: { accent: "#16a34a", badge: "bg-emerald-600",btn: "bg-emerald-600",ring: "ring-emerald-200" },
  };

  const ministries = [
    { id: "economy", label: "Economy",  img: IMG.econ_bg, Icon: DollarSign, sub: "GDP $892B",     badge: null     },
    { id: "defense", label: "Defense",  img: IMG.def_bg,  Icon: Shield,     sub: "Power 8,400",   badge: "1 Alert" },
    { id: "foreign", label: "Foreign",  img: IMG.for_bg,  Icon: Globe,      sub: "3 Allies",      badge: "Crisis"  },
    { id: "domestic",label: "Domestic", img: IMG.map,     Icon: Landmark,   sub: "Lvl.5 required",badge: "Locked"  },
  ];

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      {/* Hero map */}
      <div className="relative h-40 overflow-hidden shrink-0">
        <img src={IMG.map} alt="Veltria" className="w-full h-full object-cover" />
        <div className="absolute inset-0 bg-gradient-to-b from-[#1E3A6E]/50 via-transparent to-[#f0e8d4]/95" />
        <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} transition={{ duration: 0.6 }}
          className="absolute inset-0 flex flex-col items-center justify-center text-center pb-4">
          <p className="text-[10px] font-bold tracking-[0.5em] text-white/80 uppercase mb-1">The Republic of</p>
          <h1 className="text-5xl font-[Cinzel] font-black text-white drop-shadow-xl leading-none">VELTRIA</h1>
          <p className="text-sm text-white/80 font-semibold mt-2">{formatQuarter(date).replace('·', '—')}</p>
          <div className="flex gap-2 mt-3">
            <span className="bg-[#C4882A] text-white text-[10px] font-black px-3 py-1 rounded-full">3 Active Events</span>
            <span className="bg-white/20 backdrop-blur text-white text-[10px] font-bold px-3 py-1 rounded-full border border-white/30">Rank #14 Globally</span>
          </div>
        </motion.div>
      </div>

      <div className="px-3 pt-3 pb-5 space-y-3">
        {/* Empire vitals */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase">Empire Status</h2>
            <span className="text-[10px] text-stone-400 font-medium">Turn {formatQuarter(date)}</span>
          </div>
          <div className="grid grid-cols-2 gap-2">
            {vitals.map(({ Icon, label, value, pct, color, warn, sub }, i) => (
              <motion.div key={label} initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.07 }}
                className={`bg-white rounded-2xl p-3 shadow-md border-2 ${warn ? "border-amber-200" : "border-transparent"}`}>
                <div className="flex items-center justify-between mb-2">
                  <div className="w-7 h-7 rounded-xl flex items-center justify-center" style={{ background: color + "20" }}>
                    <Icon className="w-5 h-5" style={{ color }} />
                  </div>
                  {warn && (
                    <motion.div animate={{ opacity: [1, 0.4, 1] }} transition={{ repeat: Infinity, duration: 1.5 }}
                      className="w-2 h-2 rounded-full bg-amber-400" />
                  )}
                </div>
                <div className="text-xl font-mono font-black text-stone-800 mb-0.5">{value}</div>
                <div className="text-[10px] font-bold text-stone-400 mb-2">{label} · {sub}</div>
                <GameBar pct={pct} color={color} delay={i * 0.07} />
              </motion.div>
            ))}
          </div>
        </div>

        {/* Active situations */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase">Active Situations</h2>
            <span className="text-[10px] text-stone-400 font-medium">3 require action</span>
          </div>
          <div className="space-y-3">
            {EVENTS.map((ev, i) => {
              const cfg = sevCfg[ev.sev];
              return (
                <motion.div key={ev.id} initial={{ opacity: 0, x: -16 }} animate={{ opacity: 1, x: 0 }}
                  transition={{ delay: 0.2 + i * 0.1 }}
                  className={`bg-white rounded-2xl overflow-hidden shadow-md border-2 ${ev.sev === "CRISIS" ? "border-red-200" : "border-transparent"} ring-4 ring-transparent ${ev.sev === "CRISIS" ? "ring-red-50" : ""}`}>
                  <div className="flex">
                    <div className="relative w-20 shrink-0 overflow-hidden">
                      <img src={ev.img} alt={ev.title} className="w-full h-full object-cover" />
                      <div className="absolute inset-0 bg-gradient-to-r from-transparent to-white/10" />
                    </div>
                    <div className="flex-1 p-2.5">
                      <div className="flex items-start gap-2 mb-1">
                        <span className="text-sm leading-none">{ev.icon}</span>
                        <div>
                          <span className={`text-[9px] font-black px-1.5 py-0.5 rounded text-white ${cfg.badge}`}>{ev.sev}</span>
                          <h3 className="text-sm font-bold text-stone-800 mt-0.5 leading-tight">{ev.title}</h3>
                        </div>
                      </div>
                      <p className="text-[11px] text-stone-500 leading-relaxed mb-2.5 line-clamp-2">{ev.desc}</p>
                      <Btn color={cfg.btn} className="text-[11px] px-3 py-1.5" onClick={() => setView(ev.nav)}>
                        {ev.ctaMini} →
                      </Btn>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>

        {/* Ministry tiles */}
        <div>
          <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase mb-2">Ministries</h2>
          <div className="grid grid-cols-2 gap-2">
            {ministries.map(({ id, label, img, Icon, sub, badge }, i) => (
              <motion.div key={id} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.35 + i * 0.08 }}
                onClick={() => setView(id)}
                className="bg-white rounded-2xl overflow-hidden shadow-md hover:shadow-lg active:scale-98 transition-all cursor-pointer group">
                <div className="relative h-20 overflow-hidden">
                  <img src={img} alt={label} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div className="absolute inset-0 bg-gradient-to-t from-[#1E3A6E]/80 to-transparent" />
                  {badge && (
                    <div className={`absolute top-2 right-2 text-[9px] font-black px-2 py-0.5 rounded-full text-white ${
                      badge === "Locked" ? "bg-stone-600" : badge === "Crisis" ? "bg-red-600" : "bg-amber-500"
                    }`}>{badge}</div>
                  )}
                  <Icon className="absolute bottom-2.5 left-3 w-5 h-5 text-white drop-shadow" />
                </div>
                <div className="px-2.5 py-2 flex items-center justify-between">
                  <div>
                    <div className="text-sm font-bold text-stone-800">{label}</div>
                    <div className="text-[10px] text-stone-400 font-medium">{sub}</div>
                  </div>
                  <ChevronRight className="w-4 h-4 text-stone-300" />
                </div>
              </motion.div>
            ))}
          </div>

          <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase mt-6 mb-2">Departments</h2>
          <div className="grid grid-cols-2 gap-2">
            {[
              { id: "science", label: "Science", Icon: Zap },
              { id: "laws", label: "Laws", Icon: BookOpen },
              { id: "un", label: "United Nations", Icon: Globe },
              { id: "analytics", label: "Analytics", Icon: BarChart2 },
              { id: "demographics", label: "Demographics", Icon: Users },
              { id: "settings", label: "Settings", Icon: Settings }
            ].map(({ id, label, Icon }, i) => (
              <motion.div key={id} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: 0.5 + i * 0.05 }}
                onClick={() => setView(id)}
                className="bg-white rounded-xl p-2 shadow-sm border border-stone-100 flex items-center gap-2 cursor-pointer hover:border-[#C4882A]/50 transition-colors">
                <div className="w-6 h-6 rounded-lg bg-stone-100 flex items-center justify-center">
                  <Icon className="w-4 h-4 text-[#1E3A6E]" />
                </div>
                <div className="text-xs font-bold text-stone-700 flex-1">{label}</div>
              </motion.div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── ECONOMY ─────────────────────────────────────────────────────────────────
function EconomyScreen() {
  const [boosted, setBoosted] = useState<Set<string>>(new Set());
  const toggle = (id: string) => setBoosted(s => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <ScreenHeader img={IMG.econ_bg} title="Economy" subtitle="Ministry of"
        stats={[{ label: "GDP", value: "$892.4B" }, { label: "Growth", value: "+2.3%" }, { label: "Revenue", value: "$893B/yr" }]} />

      {/* GDP summary bar */}
      <div className="mx-4 -mt-4 bg-white rounded-2xl shadow-lg p-4 border border-stone-100 z-10 relative">
        <div className="flex items-center justify-between mb-2">
          <span className="text-xs font-black text-stone-500 tracking-wider uppercase">Total GDP Breakdown</span>
          <div className="flex items-center gap-1">
            <Trophy className="w-3.5 h-3.5 text-[#C4882A]" />
            <span className="text-[11px] font-bold text-[#C4882A]">7 Active Sectors</span>
          </div>
        </div>
        <div className="flex h-4 rounded-full overflow-hidden gap-px">
          {SECTORS.map(s => (
            <motion.div key={s.id} initial={{ width: 0 }} animate={{ width: `${s.gdp}%` }}
              transition={{ duration: 1, ease: "easeOut" }} title={s.name}
              className={`h-full ${boosted.has(s.id) ? "opacity-100 ring-2 ring-[#C4882A] ring-inset" : "opacity-70"} hover:opacity-100 transition-opacity`}
              style={{ background: ["#3b82f6","#16a34a","#8b5cf6","#06b6d4","#d97706","#f97316","#dc2626"][SECTORS.findIndex(x=>x.id===s.id)] }} />
          ))}
        </div>
        <div className="flex flex-wrap gap-x-3 gap-y-1 mt-2">
          {SECTORS.map((s, i) => (
            <div key={s.id} className="flex items-center gap-1">
              <div className="w-2 h-2 rounded-full" style={{ background: ["#3b82f6","#16a34a","#8b5cf6","#06b6d4","#d97706","#f97316","#dc2626"][i] }} />
              <span className="text-[9px] text-stone-500 font-medium">{s.name} {s.gdp}%</span>
            </div>
          ))}
        </div>
      </div>

      {/* Sector cards */}
      <div className="px-3 pt-4 pb-5">
        <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase mb-2">Sector Management</h2>
        <div className="grid grid-cols-2 gap-2">
          {SECTORS.map((s, i) => {
            const active = boosted.has(s.id);
            return (
              <motion.div key={s.id} initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }}
                transition={{ delay: i * 0.07 }}
                className={`bg-white rounded-2xl overflow-hidden shadow-md border-2 transition-all duration-200 ${active ? "border-[#C4882A] shadow-[#C4882A]/20 shadow-xl" : "border-transparent"}`}>
                <div className="relative h-24 overflow-hidden">
                  <img src={s.img} alt={s.name} className="w-full h-full object-cover" />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-black/20 to-transparent" />
                  <div className="absolute top-2 left-2"><LvBadge n={s.level} /></div>
                  <div className="absolute top-2 right-2">
                    <span className={`text-[10px] font-black px-2 py-0.5 rounded-full text-white ${s.growth >= 0 ? "bg-green-600" : "bg-red-600"}`}>
                      {s.growth >= 0 ? "▲" : "▼"}{Math.abs(s.growth).toFixed(1)}%
                    </span>
                  </div>
                  <div className="absolute bottom-2 left-3 right-3">
                    <h3 className="text-sm font-black text-white drop-shadow leading-tight">{s.name}</h3>
                    <Stars n={s.level} />
                  </div>
                </div>
                <div className="p-2.5 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <span className="text-[10px] font-bold text-stone-400">GDP Share</span>
                    <span className="text-lg font-mono font-black text-stone-800">{s.gdp.toFixed(1)}%</span>
                  </div>
                  <div>
                    <div className="flex justify-between text-[9px] font-bold text-stone-400 mb-1">
                      <span>XP to Level {s.level + 1}</span><span>{s.xp}%</span>
                    </div>
                    <XPBar xp={s.xp} delay={i * 0.07} />
                  </div>
                  <div className="text-[10px] font-semibold text-stone-500">
                    ${s.revenue}B/yr revenue
                  </div>
                </div>
                <div className="px-2.5 pb-2.5">
                  <motion.button whileTap={{ scale: 0.94 }} onClick={() => toggle(s.id)}
                    className={`w-full py-1.5 rounded-xl font-black text-sm transition-all duration-200 shadow-sm ${
                      active ? "bg-green-500 text-white shadow-green-200" : "bg-[#C4882A] text-white hover:bg-[#b07820]"
                    }`}>
                    {active ? "Investing" : "Invest"}
                  </motion.button>
                </div>
              </motion.div>
            );
          })}
        </div>
      </div>
    </div>
  );
}

// ─── DEFENSE ─────────────────────────────────────────────────────────────────
function DefenseScreen() {
  const [branch, setBranch] = useState<Branch>("ARMY");
  const [qty, setQty] = useState<Record<string, number>>({});
  const upd = (id: string, d: number) => setQty(q => ({ ...q, [id]: Math.max(0, (q[id] ?? 0) + d) }));
  const totalCost = RECRUITS.filter(r => r.branch === branch).reduce((s, u) => s + (qty[u.id] ?? 0) * u.cost, 0);
  const branches: Branch[] = ["ARMY", "NAVY", "AIR"];

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <ScreenHeader img={IMG.def_bg} title="Defense" subtitle="Ministry of"
        stats={[{ label: "Power", value: "8,400" }, { label: "Personnel", value: "847K" }, { label: "Readiness", value: "87%" }]} />

      <div className="px-3 pt-3 pb-5 space-y-3">
        {/* Branch tabs */}
        <div className="grid grid-cols-3 gap-1.5">
          {branches.map(b => {
            const info = bInfo(b);
            const active = branch === b;
            const unitCount = UNITS.filter(u => u.branch === b).reduce((s, u) => s + u.count, 0);
            return (
              <motion.button key={b} whileTap={{ scale: 0.95 }} onClick={() => setBranch(b)}
                className={`rounded-xl p-2.5 text-left transition-all duration-200 shadow-sm ${
                  active ? "text-white shadow-lg" : "bg-white text-stone-700"
                }`}
                style={active ? { background: `linear-gradient(135deg, ${info.color}dd, ${info.color})` } : {}}>
                <info.Icon className={`w-5 h-5 mb-2 ${active ? "text-white" : "text-stone-400"}`} />
                <div className={`text-sm font-black ${active ? "text-white" : "text-stone-800"}`}>{b}</div>
                <div className={`text-[10px] font-semibold ${active ? "text-white/70" : "text-stone-400"}`}>{unitCount} units</div>
                {active && <div className="h-0.5 bg-white/40 rounded-full mt-2" />}
              </motion.button>
            );
          })}
        </div>

        {/* Units */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase">{branch} Units</h2>
            <span className="text-[10px] text-stone-400 font-medium">{UNITS.filter(u => u.branch === branch).length} divisions</span>
          </div>
          <motion.div key={branch} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ duration: 0.25 }}
            className="grid grid-cols-2 gap-2">
            {UNITS.filter(u => u.branch === branch).map((u, i) => (
              <motion.div key={u.name} initial={{ opacity: 0, scale: 0.92 }} animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: i * 0.06 }}
                className="bg-white rounded-2xl overflow-hidden shadow-md border border-stone-100 flex flex-col group">
                <div className="relative h-20 overflow-hidden">
                  <img src={u.img} alt={u.name} className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500" />
                  <div className="absolute inset-0 bg-gradient-to-t from-black/75 to-transparent" />
                  <div className="absolute top-2 left-2"><StatusChip label={u.status} /></div>
                  <div className="absolute top-2 right-2">
                    <div className="bg-black/60 backdrop-blur-sm rounded-lg px-2 py-0.5">
                      <span className="text-white text-xs font-black">×{u.count}</span>
                    </div>
                  </div>
                  <div className="absolute bottom-2 left-2.5 right-2.5">
                    <p className="text-xs font-black text-white leading-tight">{u.name}</p>
                    <Stars n={u.stars} />
                  </div>
                </div>
                <div className="p-2.5 flex-1 space-y-1.5">
                  <div>
                    <div className="flex justify-between text-[9px] font-bold mb-1">
                      <span className="text-stone-400">Strength</span>
                      <span style={{ color: strColor(u.str) }} className="font-black">{u.str}%</span>
                    </div>
                    <GameBar pct={u.str} color={strColor(u.str)} h="h-2.5" />
                  </div>
                  <div className="text-[9px] text-stone-400 font-medium">${u.maint.toFixed(1)}B/yr</div>
                </div>
                <div className="grid grid-cols-2 border-t border-stone-100">
                  <motion.button whileTap={{ scale: 0.94 }} className="py-1.5 text-[10px] font-bold text-[#1E3A6E] hover:bg-blue-50 transition-colors rounded-bl-2xl">
                    Redeploy
                  </motion.button>
                  <motion.button whileTap={{ scale: 0.94 }} className="py-1.5 text-[10px] font-bold text-[#C4882A] hover:bg-amber-50 border-l border-stone-100 transition-colors rounded-br-2xl">
                    Upgrade
                  </motion.button>
                </div>
              </motion.div>
            ))}
          </motion.div>
        </div>

        {/* Recruitment */}
        <div>
          <div className="flex items-center justify-between mb-2">
            <h2 className="text-xs font-black tracking-[0.25em] text-[#1E3A6E] uppercase">Recruit {branch}</h2>
            {totalCost > 0 && (
              <motion.button whileTap={{ scale: 0.93 }}
                className="bg-[#C4882A] text-white text-xs font-black px-4 py-1.5 rounded-full shadow-md">
                Commission · ${totalCost.toFixed(1)}B ▶
              </motion.button>
            )}
          </div>
          <div className="grid grid-cols-2 gap-2">
            {RECRUITS.filter(r => r.branch === branch).map((u, i) => {
              const q = qty[u.id] ?? 0;
              return (
                <motion.div key={u.id} initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: i * 0.07 }}
                  className={`bg-white rounded-2xl overflow-hidden shadow-md border-2 transition-all ${q > 0 ? "border-[#C4882A] shadow-[#C4882A]/20 shadow-xl" : "border-transparent"}`}>
                  <div className="relative h-20 overflow-hidden">
                    <img src={u.img} alt={u.name} className="w-full h-full object-cover" />
                    <div className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent" />
                    {q > 0 && (
                      <div className="absolute top-2 right-2 bg-[#C4882A] text-white text-[10px] font-black px-2 py-0.5 rounded-full">
                        ×{q}
                      </div>
                    )}
                    <div className="absolute bottom-2 left-2.5">
                      <p className="text-xs font-black text-white leading-none">{u.name}</p>
                      <p className="text-xl font-mono font-black text-[#C4882A]">${u.cost.toFixed(1)}B</p>
                    </div>
                  </div>
                  <div className="p-2 space-y-1.5">
                    <div className="flex gap-2 text-[9px] font-bold text-stone-400">
                      <span className="flex items-center gap-0.5"><Clock className="w-3 h-3" />{u.months}mo</span>
                      <span className="flex items-center gap-0.5"><Zap className="w-3 h-3 text-amber-500" />+{u.power} power</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <motion.button whileTap={{ scale: 0.9 }} onClick={() => upd(u.id, -1)}
                        className="w-7 h-7 bg-stone-100 rounded-xl font-bold text-stone-600 flex items-center justify-center hover:bg-stone-200 transition-colors">
                        <Minus className="w-3.5 h-3.5" />
                      </motion.button>
                      <span className="flex-1 text-center text-lg font-mono font-black text-stone-800">{q}</span>
                      <motion.button whileTap={{ scale: 0.9 }} onClick={() => upd(u.id, +1)}
                        className="w-7 h-7 bg-[#1E3A6E] rounded-xl text-white flex items-center justify-center hover:bg-[#1E3A6E]/80 transition-colors">
                        <Plus className="w-3.5 h-3.5" />
                      </motion.button>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}

// ─── FOREIGN ─────────────────────────────────────────────────────────────────
function ForeignScreen() {
  const [selected, setSelected] = useState<string | null>(null);
  const sel = NATIONS.find(n => n.name === selected);

  return (
    <div className="flex-1 overflow-y-auto scrollbar-hide">
      <ScreenHeader img={IMG.for_bg} title="Foreign Affairs" subtitle="Ministry of"
        stats={[{ label: "Allies", value: "3" }, { label: "Treaties", value: "5" }, { label: "Crises", value: "1" }]} />

      <div className="px-3 pt-3 pb-5 space-y-3">
        {/* Relation legend */}
        <div className="bg-white rounded-2xl p-3.5 shadow-sm border border-stone-100 flex items-center gap-4">
          <div className="text-[10px] font-bold text-stone-400 uppercase tracking-wider">Relations:</div>
          {[["ALLY","bg-green-500"],["PARTNER","bg-blue-500"],["NEUTRAL","bg-stone-400"],["RIVAL","bg-orange-500"],["HOSTILE","bg-red-600"]].map(([l,c]) => (
            <div key={l} className="flex items-center gap-1">
              <div className={`w-2 h-2 rounded-full ${c}`} />
              <span className="text-[9px] font-bold text-stone-500">{l}</span>
            </div>
          ))}
        </div>

        <div className="grid grid-cols-2 gap-2">
          {NATIONS.map((n, i) => {
            const isHostile = n.status === "HOSTILE";
            const isRival = n.status === "RIVAL";
            return (
              <motion.div key={n.name} initial={{ opacity: 0, scale: 0.92 }} animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: i * 0.06 }}
                onClick={() => setSelected(selected === n.name ? null : n.name)}
                className={`bg-white rounded-2xl overflow-hidden shadow-md border-2 cursor-pointer transition-all duration-200 hover:shadow-lg hover:-translate-y-0.5 ${
                  selected === n.name ? "border-[#C4882A] shadow-[#C4882A]/20 shadow-xl" :
                  isHostile ? "border-red-200" : "border-transparent"
                }`}>
                <div className={`h-20 bg-gradient-to-br ${n.bg} flex items-center justify-center relative overflow-hidden`}>
                  <motion.span className="text-4xl drop-shadow-lg"
                    animate={isHostile ? { scale: [1, 1.05, 1] } : {}}
                    transition={{ repeat: Infinity, duration: 2 }}>
                    {n.flag}
                  </motion.span>
                  {isHostile && (
                    <motion.div animate={{ opacity: [0.5, 1, 0.5] }} transition={{ repeat: Infinity, duration: 1.2 }}
                      className="absolute inset-0 border-4 border-red-400/60 rounded-t-2xl pointer-events-none" />
                  )}
                  <div className="absolute top-2 right-2">
                    <StatusChip label={n.status} />
                  </div>
                </div>
                <div className="p-2.5 space-y-1.5">
                  <div className="flex items-center justify-between">
                    <h3 className="text-sm font-black text-stone-800">{n.name}</h3>
                    <StatusChip label={n.threat} />
                  </div>
                  <div>
                    <div className="flex justify-between text-[9px] font-bold mb-1">
                      <span className="text-stone-400">Relations</span>
                      <span style={{ color: relColor(n.rel) }} className="font-black">{n.rel}/100</span>
                    </div>
                    <GameBar pct={n.rel} color={relColor(n.rel)} delay={i * 0.06} h="h-2.5" />
                  </div>
                  <motion.button whileTap={{ scale: 0.93 }}
                    className={`w-full py-1.5 rounded-xl text-[11px] font-black text-white transition-colors ${n.actionColor}`}>
                    {n.action}
                  </motion.button>
                </div>
              </motion.div>
            );
          })}
        </div>

        {/* Detail panel on select */}
        {sel && (
          <motion.div initial={{ opacity: 0, y: 16 }} animate={{ opacity: 1, y: 0 }}
            className="bg-white rounded-2xl overflow-hidden shadow-xl border-2 border-[#C4882A]">
            <div className={`h-12 bg-gradient-to-r ${sel.bg} flex items-center px-4 gap-2`}>
              <span className="text-4xl">{sel.flag}</span>
              <div>
                <h3 className="text-white font-[Cinzel] font-black text-lg">{sel.name}</h3>
                <div className="flex gap-2 mt-0.5"><StatusChip label={sel.status} /><StatusChip label={sel.threat} /></div>
              </div>
              <button onClick={() => setSelected(null)} className="ml-auto text-white/70 hover:text-white text-2xl leading-none">×</button>
            </div>
            <div className="p-3">
              <p className="text-xs font-black tracking-widest text-stone-400 uppercase mb-2">Diplomatic Actions</p>
              <div className="grid grid-cols-3 gap-1.5">
                {["Propose Treaty","Send Trade Offer","Gift Aid","Share Intelligence","Issue Warning","Request Summit"].map(a => (
                  <motion.button key={a} whileTap={{ scale: 0.93 }}
                    className="bg-stone-50 border border-stone-200 text-stone-700 text-[10px] font-bold py-1.5 px-2 rounded-xl hover:border-[#1E3A6E] hover:text-[#1E3A6E] transition-colors text-center">
                    {a}
                  </motion.button>
                ))}
              </div>
            </div>
          </motion.div>
        )}
      </div>
    </div>
  );
}

// ─── LOCKED SCREEN ───────────────────────────────────────────────────────────
function LockedScreen({ label }: { label: string }) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center gap-5 p-10 text-center">
      <div className="w-24 h-24 rounded-3xl bg-stone-100 flex items-center justify-center shadow-inner">
        <Lock className="w-10 h-10 text-stone-300" />
      </div>
      <div>
        <h2 className="font-[Cinzel] font-black text-2xl text-[#1E3A6E] mb-2">{label}</h2>
        <p className="text-sm text-stone-400 max-w-48 mx-auto leading-relaxed">
          This ministry requires Security Clearance Level 5 and is currently locked.
        </p>
      </div>
      <motion.button whileTap={{ scale: 0.93 }}
        className="bg-[#1E3A6E] text-white font-bold px-8 py-3 rounded-2xl shadow-md hover:bg-[#1E3A6E]/90 transition-colors">
        Request Access
      </motion.button>
      <div className="flex items-center gap-2 text-[11px] text-stone-400 font-medium">
        <Star className="w-3.5 h-3.5 text-[#C4882A]" />
        Reach Level 12 to unlock
      </div>
    </div>
  );
}

// ─── TOP HUD ─────────────────────────────────────────────────────────────────
function HUD({ date, advanceTurn }: { date: Date; advanceTurn: () => void }) {
  const resources = [
    { Icon: DollarSign, v: "$124B", warn: true, label: "Treasury" },
    { Icon: Shield,     v: "73%",   warn: true, label: "Stability" },
    { Icon: Swords,     v: "8.4K",  warn: false, label: "Mil. Power" },
  ];

  // Election logic
  const nextElectionYear = Math.ceil((date.getFullYear() + 1) / 4) * 4;
  const turnsToElection = ((nextElectionYear - date.getFullYear()) * 4) + (4 - Math.floor(date.getMonth() / 3));

  return (
    <header className="bg-[#1E3A6E] pt-[env(safe-area-inset-top,0px)] shrink-0 flex flex-col shadow-2xl z-50 rounded-b-3xl">
      {/* Top row: Brand & Turn Action */}
      <div className="flex items-center justify-between px-3 h-12">
        <div className="flex items-center gap-2">
          <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#C4882A] to-amber-600 flex items-center justify-center shadow-lg border border-white/20">
            <Landmark className="w-4 h-4 text-white drop-shadow-md" />
          </div>
          <div className="leading-none">
            <div className="text-base font-[Cinzel] font-black text-white tracking-widest drop-shadow-sm">VELTRIA</div>
            <div className="flex items-center gap-2 mt-0.5">
              <div className="text-[10px] text-white/60 font-bold uppercase tracking-widest">{formatQuarter(date)}</div>
              {turnsToElection <= 4 && (
                <div className="bg-amber-500/20 border border-amber-500/40 px-1.5 rounded text-[8px] font-black text-amber-300">
                  ELECTION IN {turnsToElection}
                </div>
              )}
            </div>
          </div>
        </div>

        <motion.button
          animate={{ boxShadow: ["0 0 0 0 rgba(196,136,42,0.6)", "0 0 0 10px rgba(196,136,42,0)", "0 0 0 0 rgba(196,136,42,0)"] }}
          transition={{ repeat: Infinity, duration: 2.5 }}
          whileTap={{ scale: 0.93 }}
          onClick={advanceTurn}
          className="bg-gradient-to-r from-[#C4882A] to-amber-500 text-white text-xs font-black px-3 py-1.5 rounded-2xl shadow-xl shrink-0 flex items-center gap-2 border border-white/20 hover:to-amber-400 transition-all">
          <Flame className="w-4 h-4 text-amber-100" />
          End Turn
        </motion.button>
      </div>

      {/* Bottom row: Metrics Bar */}
      <div className="bg-[#15294e] px-3 py-1.5 flex items-center justify-between mx-2 mb-2 rounded-xl border border-white/5 shadow-inner">
        {resources.map(({ Icon, v, warn }, i) => (
          <div key={i} className="flex items-center gap-1.5">
            <div className={`p-1 rounded-md ${warn ? "bg-amber-500/20" : "bg-white/10"}`}>
              <Icon className={`w-3.5 h-3.5 ${warn ? "text-amber-400" : "text-white/70"}`} />
            </div>
            <span className={`text-xs font-mono font-black ${warn ? "text-amber-400" : "text-white"}`}>{v}</span>
          </div>
        ))}
        <div className="w-px h-6 bg-white/10 mx-1" />
        <div className="flex items-center gap-1.5 bg-red-500/20 rounded-lg px-2 py-1 border border-red-500/30">
          <motion.div animate={{ scale: [1, 1.4, 1] }} transition={{ repeat: Infinity, duration: 1.8 }}
            className="w-1.5 h-1.5 rounded-full bg-red-400 shadow-[0_0_8px_rgba(248,113,113,0.8)]" />
          <span className="text-[10px] font-black text-red-300">3 ALERTS</span>
        </div>
      </div>
    </header>
  );
}

// ─── BOTTOM NAV ──────────────────────────────────────────────────────────────
function BottomNav({ view, setView }: { view: string; setView: (v: string) => void }) {
  const tabs = [
    { id: "overview",  label: "Overview",  Icon: Landmark,    alert: 3 },
    { id: "economy",   label: "Economy",   Icon: DollarSign,  alert: 0 },
    { id: "defense",   label: "Defense",   Icon: Shield,      alert: 1 },
    { id: "foreign",   label: "Foreign",   Icon: Globe,       alert: 2 },
    { id: "intel",     label: "Intel",     Icon: Eye,         alert: 0 },
  ];
  return (
    <nav className="bg-[#1E3A6E] border-t border-white/10 shrink-0 flex pb-[env(safe-area-inset-bottom,0px)] z-50">
      {tabs.map(({ id, label, Icon, alert }) => {
        const active = view === id;
        return (
          <motion.button key={id} whileTap={{ scale: 0.88 }} onClick={() => setView(id)}
            className={`flex-1 flex flex-col items-center justify-center pt-2.5 pb-2 gap-1 relative transition-all duration-200 ${
              active ? "text-white" : "text-white/40 hover:text-white/70"
            }`}>
            {active && (
              <>
                <motion.div layoutId="nav-bg" className="absolute inset-x-1 inset-y-0.5 rounded-xl bg-white/12"
                  transition={{ type: "spring", stiffness: 500, damping: 40 }} />
                <motion.div layoutId="nav-dot" className="absolute top-0 left-1/2 -translate-x-1/2 w-8 h-0.5 bg-[#C4882A] rounded-full"
                  transition={{ type: "spring", stiffness: 500, damping: 40 }} />
              </>
            )}
            <div className="relative z-10">
              <Icon className="w-5 h-5" />
              {alert > 0 && (
                <motion.div animate={{ scale: [1, 1.3, 1] }} transition={{ repeat: Infinity, duration: 2, delay: 0.3 }}
                  className="absolute -top-1 -right-1.5 w-3.5 h-3.5 bg-red-500 rounded-full border-2 border-[#1E3A6E] flex items-center justify-center">
                  <span className="text-[7px] font-black text-white leading-none">{alert}</span>
                </motion.div>
              )}
            </div>
            <span className="text-[9px] font-bold relative z-10 leading-none">{label}</span>
          </motion.button>
        );
      })}
    </nav>
  );
}

// ─── DATE HELPERS ─────────────────────────────────────────────────────────────
const formatQuarter = (date: Date) => {
  const q = Math.floor(date.getMonth() / 3) + 1;
  return `${date.getFullYear()} · Q${q}`;
};

// ─── APP ─────────────────────────────────────────────────────────────────────
export default function App() {
  const [view, setView] = useState("overview");
  const [date, setDate] = useState(new Date(2031, 6, 1)); // Starts Q3 2031

  const advanceTurn = () => {
    setDate(prev => {
      const next = new Date(prev);
      next.setMonth(prev.getMonth() + 3); // Advance one quarter per turn
      return next;
    });
  };

  return (
    <div className="h-screen flex flex-col overflow-hidden font-sans" style={{
      background: "#f0e8d4",
      backgroundImage: "radial-gradient(circle at 1px 1px, rgba(30,58,110,0.06) 1px, transparent 0)",
      backgroundSize: "22px 22px",
    }}>
      <HUD date={date} advanceTurn={advanceTurn} />
      <motion.div key={view} initial={{ opacity: 0, y: 8 }} animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.22, ease: "easeOut" }} className="flex-1 flex flex-col overflow-hidden">
        {view === "overview" && <Dashboard setView={setView} date={date} />}
        {view === "economy"  && <EconomyScreen />}
        {view === "defense"  && <DefenseScreen />}
        {view === "foreign"  && <ForeignScreen />}
        {view === "intel"    && <LockedScreen label="Intelligence" />}
        {view === "domestic" && <LockedScreen label="Domestic Policy" />}
        {["science", "laws", "un", "analytics", "demographics", "settings"].includes(view) && (
          <LockedScreen label={view.charAt(0).toUpperCase() + view.slice(1)} />
        )}
      </motion.div>
      <BottomNav view={view} setView={setView} />
    </div>
  );
}
