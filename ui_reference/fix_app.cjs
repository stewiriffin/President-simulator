const fs = require('fs');

let content = fs.readFileSync('src/app/App.tsx', 'utf-8');

// Replace specific emoji strings in NATIONS
content = content.replace(/"🟦"/g, '<Globe className="w-full h-full text-blue-200" />');
content = content.replace(/"🟩"/g, '<Globe className="w-full h-full text-emerald-200" />');
content = content.replace(/"🟪"/g, '<Globe className="w-full h-full text-violet-200" />');
content = content.replace(/"🟨"/g, '<Globe className="w-full h-full text-yellow-200" />');
content = content.replace(/"⬜"/g, '<Globe className="w-full h-full text-slate-200" />');
content = content.replace(/"🟫"/g, '<Globe className="w-full h-full text-stone-200" />');
content = content.replace(/"🟧"/g, '<Globe className="w-full h-full text-orange-200" />');
content = content.replace(/"🔴"/g, '<Globe className="w-full h-full text-red-200" />');
content = content.replace(/"⚔ Respond"/g, '"Respond"');
content = content.replace(/⚔ 3 Active Events/g, '3 Active Events');
content = content.replace(/👑 Rank #14 Globally/g, 'Rank #14 Globally');
content = content.replace(/💰 /g, '');
content = content.replace(/✓ /g, '');
content = content.replace(/⬆ /g, '');
content = content.replace(/🔒 /g, '');

content = content.replace(/icon: "🚨"/g, 'icon: <AlertTriangle className="w-5 h-5 text-red-500" />');
content = content.replace(/icon: "⚠️"/g, 'icon: <AlertTriangle className="w-5 h-5 text-amber-500" />');
content = content.replace(/icon: "🚀"/g, 'icon: <TrendingUp className="w-5 h-5 text-emerald-500" />');

content = content.replace(/ctaMini: "⚔ Deploy"/g, 'ctaMini: "Deploy"');
content = content.replace(/ctaMini: "💬 Talk"/g, 'ctaMini: "Talk"');
content = content.replace(/ctaMini: "⬆ Invest"/g, 'ctaMini: "Invest"');

fs.writeFileSync('src/app/App.tsx.tmp', content);
