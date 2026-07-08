const fs = require('fs');
let c = fs.readFileSync('src/app/App.tsx', 'utf-8');

// --- Atoms & Global ---
c = c.replace(/px-4 py-2\.5 shadow-md/g, 'px-3 py-2 shadow-md'); // Btn atom
c = c.replace(/gap-3/g, 'gap-2'); // Tighten main grids
c = c.replace(/mb-3/g, 'mb-2'); // Tighter headers
c = c.replace(/space-y-6/g, 'space-y-4'); // Sections
c = c.replace(/space-y-5/g, 'space-y-4');
c = c.replace(/space-y-4/g, 'space-y-3');
c = c.replace(/px-4 pt-4 pb-6/g, 'px-3 pt-3 pb-5'); // Screen content padding
c = c.replace(/px-4 pt-5 pb-6/g, 'px-3 pt-4 pb-5');

// --- Screen Header ---
c = c.replace(/className="relative h-44 overflow-hidden shrink-0"/g, 'className="relative h-32 overflow-hidden shrink-0"');
c = c.replace(/className="absolute inset-0 flex flex-col justify-end p-5"/g, 'className="absolute inset-0 flex flex-col justify-end p-4"');
c = c.replace(/className="text-4xl font-\[Cinzel\] font-black text-white drop-shadow-lg mb-3"/g, 'className="text-3xl font-[Cinzel] font-black text-white drop-shadow-lg mb-2"');

// --- Dashboard Hero ---
c = c.replace(/className="relative h-52 overflow-hidden shrink-0"/g, 'className="relative h-40 overflow-hidden shrink-0"');
c = c.replace(/className="text-6xl font-\[Cinzel\] font-black text-white drop-shadow-xl leading-none"/g, 'className="text-5xl font-[Cinzel] font-black text-white drop-shadow-xl leading-none"');

// --- Dashboard Vitals ---
c = c.replace(/className=\{\`bg-white rounded-2xl p-4 shadow-md border-2 \$\{warn \? "border-amber-200" : "border-transparent"\}\`\}/g, 'className={`bg-white rounded-2xl p-3 shadow-md border-2 ${warn ? "border-amber-200" : "border-transparent"}`}');
c = c.replace(/className="w-9 h-9 rounded-xl flex items-center justify-center"/g, 'className="w-7 h-7 rounded-xl flex items-center justify-center"');
c = c.replace(/className="text-2xl font-mono font-black text-stone-800 mb-0\.5"/g, 'className="text-xl font-mono font-black text-stone-800 mb-0.5"');

// --- Dashboard Situations ---
c = c.replace(/className="relative w-28 shrink-0 overflow-hidden"/g, 'className="relative w-20 shrink-0 overflow-hidden"');
c = c.replace(/className="flex-1 p-3\.5"/g, 'className="flex-1 p-2.5"');
c = c.replace(/className="text-base leading-none"/g, 'className="text-sm leading-none"');

// --- Dashboard Ministries ---
c = c.replace(/className="relative h-24 overflow-hidden"/g, 'className="relative h-20 overflow-hidden"');
c = c.replace(/className="px-3 py-2\.5 flex items-center justify-between"/g, 'className="px-2.5 py-2 flex items-center justify-between"');

// --- Dashboard Departments ---
c = c.replace(/className="bg-white rounded-xl p-3 shadow-sm/g, 'className="bg-white rounded-xl p-2 shadow-sm');
c = c.replace(/className="w-8 h-8 rounded-lg bg-stone-100 flex items-center justify-center"/g, 'className="w-6 h-6 rounded-lg bg-stone-100 flex items-center justify-center"');

// --- Economy Sectors ---
c = c.replace(/className="relative h-32 overflow-hidden"/g, 'className="relative h-24 overflow-hidden"');
c = c.replace(/className="p-3 space-y-2"/g, 'className="p-2.5 space-y-1.5"');
c = c.replace(/className="px-3 pb-3"/g, 'className="px-2.5 pb-2.5"');
c = c.replace(/className=\{\`w-full py-2\.5 rounded-xl/g, 'className={`w-full py-1.5 rounded-xl');

// --- Defense Units ---
c = c.replace(/className="relative h-28 overflow-hidden"/g, 'className="relative h-20 overflow-hidden"');
c = c.replace(/className="p-3 flex-1 space-y-2"/g, 'className="p-2.5 flex-1 space-y-1.5"');
c = c.replace(/className="py-2 text-\[10px\] font-bold/g, 'className="py-1.5 text-[10px] font-bold');
c = c.replace(/className="grid grid-cols-3 gap-2"/g, 'className="grid grid-cols-3 gap-1.5"'); // Tabs
c = c.replace(/className=\{\`rounded-2xl p-3\.5 text-left/g, 'className={`rounded-xl p-2.5 text-left');

// --- Defense Recruits ---
// already replaced h-24 -> h-20 above
c = c.replace(/className="p-2\.5 space-y-2"/g, 'className="p-2 space-y-1.5"');
c = c.replace(/className="w-8 h-8 bg-stone-100/g, 'className="w-7 h-7 bg-stone-100');
c = c.replace(/className="w-8 h-8 bg-\[#1E3A6E\]/g, 'className="w-7 h-7 bg-[#1E3A6E]');

// --- Foreign Nations ---
c = c.replace(/h-24 bg-gradient-to-br/g, 'h-20 bg-gradient-to-br');
c = c.replace(/className="text-5xl drop-shadow-lg"/g, 'className="text-4xl drop-shadow-lg"');
c = c.replace(/py-2 rounded-xl/g, 'py-1.5 rounded-xl'); // buttons

// --- Foreign Detail ---
c = c.replace(/h-16 bg-gradient-to-r/g, 'h-12 bg-gradient-to-r');
c = c.replace(/className="p-4"/g, 'className="p-3"');
c = c.replace(/py-2\.5 px-2/g, 'py-1.5 px-2');

// --- HUD ---
c = c.replace(/px-4 h-16/g, 'px-3 h-12');
c = c.replace(/w-10 h-10 rounded-xl/g, 'w-8 h-8 rounded-lg');
c = c.replace(/w-5 h-5 text-white drop-shadow-md/g, 'w-4 h-4 text-white drop-shadow-md');
c = c.replace(/text-lg font-\[Cinzel\]/g, 'text-base font-[Cinzel]');
c = c.replace(/px-5 py-2\.5/g, 'px-3 py-1.5');
c = c.replace(/px-4 py-2 flex/g, 'px-3 py-1.5 flex');

fs.writeFileSync('src/app/App.tsx', c);
