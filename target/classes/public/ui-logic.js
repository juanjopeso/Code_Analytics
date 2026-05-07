/**
 * ui-logic.js
 *
 * Todas las funciones puras y de DOM extraídas de index.html.
 * Este módulo NO contiene fetch(), addEventListener() ni nada
 * que arranque automáticamente — sólo exporta funciones que
 * los tests pueden importar y ejercitar de forma aislada.
 *
 * En producción, index.html importa este archivo con:
 *   <script type="module" src="ui-logic.js"></script>
 */

// ── ESTADO ──────────────────────────────────────────────
export let allResults = [];
export let currentFilter = 'all';

export function setCurrentFilter(f) { currentFilter = f; }

// ── FUNCIONES PURAS (sin side-effects de DOM) ───────────

/**
 * Convierte un score numérico en etiqueta de nivel.
 * @param {number} s
 * @returns {'BUENO'|'REGULAR'|'MALO'}
 */
export function levelFromScore(s) {
  return s >= 80 ? 'BUENO' : s >= 50 ? 'REGULAR' : 'MALO';
}

/**
 * Devuelve la clase CSS para el dot de severidad de un issue.
 * @param {string|null} type
 * @returns {'dot-high'|'dot-medium'|'dot-low'}
 */
export function dotClass(type) {
  if (!type) return 'dot-low';
  const t = type.toLowerCase();
  if (t.includes('high') || t.includes('god') || t.includes('risky')) return 'dot-high';
  if (t.includes('moderate') || t.includes('long')) return 'dot-medium';
  return 'dot-low';
}

/**
 * Calcula las estadísticas de resumen a partir del array de resultados.
 * @param {ClassAnalysisResult[]} results
 * @returns {{ classes, methods, avgScore, issues, godClasses }}
 */
export function computeSummary(results) {
  if (!results || results.length === 0) {
    return { classes: 0, methods: 0, avgScore: 0, issues: 0, godClasses: 0 };
  }

  const classes    = results.length;
  const methods    = results.reduce((s, c) => s + (c.methods?.length || 0), 0);
  const issues     = results.reduce((s, c) =>
    s + (c.methods?.reduce((ms, m) => ms + (m.evaluation?.issues?.length || 0), 0) || 0), 0);
  const godClasses = results.filter(c => c.badMethodCount >= 2).length;
  const avgScore   = Math.round(results.reduce((s, c) => s + (c.score || 0), 0) / classes);

  return { classes, methods, avgScore, issues, godClasses };
}

/**
 * Decide si un card debe mostrarse dado el filtro activo.
 * @param {{ level: string, god: string }} cardDataset  – dataset del elemento DOM
 * @param {string} filter
 * @returns {boolean}
 */
export function cardMatchesFilter(cardDataset, filter) {
  if (filter === 'all') return true;
  if (filter === 'god')  return cardDataset.god === 'true';
  return cardDataset.level === filter;
}

// ── FUNCIONES DE DOM ────────────────────────────────────
// Cada función recibe el documento como parámetro para
// que los tests puedan pasarle el DOM de jsdom.

export function showError(msg, doc = document) {
  const el = doc.getElementById('errorBox');
  el.textContent = '⚠ ' + msg;
  el.classList.add('visible');
}

export function hideError(doc = document) {
  doc.getElementById('errorBox').classList.remove('visible');
}

export function showEmpty(doc = document) {
  doc.getElementById('results').classList.add('visible');
  doc.getElementById('emptyState').style.display = 'flex';
}

export function clearResults(doc = document) {
  doc.getElementById('classCards').innerHTML = '';
  doc.getElementById('results').classList.remove('visible');
  doc.getElementById('summaryGrid').style.display = 'none';
  doc.getElementById('emptyState').style.display = 'none';
  doc.querySelectorAll('.stat-card').forEach(c => c.classList.remove('visible'));
}

export function setLoading(on, doc = document) {
  const btn = doc.getElementById('analyzeBtn');
  btn.disabled = on;
  btn.textContent = on ? 'Analizando…' : 'Analizar →';
  doc.getElementById('progressWrap').classList.toggle('visible', on);
}

export function renderSummary(results, doc = document) {
  const { classes, methods, avgScore, issues, godClasses } = computeSummary(results);
  doc.getElementById('sv-classes').textContent = classes;
  doc.getElementById('sv-methods').textContent = methods;
  doc.getElementById('sv-score').textContent   = avgScore;
  doc.getElementById('sv-issues').textContent  = issues;
  doc.getElementById('sv-god').textContent     = godClasses;
  doc.getElementById('summaryGrid').style.display = 'grid';
}

export function buildAIBox(label, text) {
  return `<div class="ai-box"><div class="ai-box-label">✦ ${label}</div>${text}</div>`;
}

export function buildMethodsTable(cls) {
  if (!cls.methods || cls.methods.length === 0) {
    return '<p class="no-methods">Sin métodos encontrados.</p>';
  }

  const rows = cls.methods.map(m => {
    const metrics   = m.metrics   || {};
    const evaluation = m.evaluation || {};
    const issues    = evaluation.issues || [];
    const score     = evaluation.score ?? 100;
    const hasIssues = issues.length > 0;

    const issuesSummary = hasIssues
      ? `<ul class="issue-list">${issues.map(i =>
          `<li class="issue-item">
             <span class="issue-dot ${dotClass(i.type)}"></span>
             <span><strong>${i.type}</strong> — ${i.description || ''}</span>
           </li>`).join('')}</ul>`
      : `<span class="no-issues">✓ Sin problemas</span>`;

    const aiBox = hasIssues && issues[0]?.suggestion
      ? buildAIBox('Sugerencia IA', issues[0].suggestion)
      : '';

    return `<tr>
      <td><span class="method-name">${metrics.methodName || '?'}</span></td>
      <td>
        <span class="metric-chip">LOC ${metrics.loc ?? '?'}</span>
        <span class="metric-chip">CC ${metrics.cyclomaticComplexity ?? '?'}</span>
      </td>
      <td>${issuesSummary}${aiBox}</td>
      <td><span class="score-cell">${score}</span></td>
    </tr>`;
  }).join('');

  return `<table class="method-table">
    <thead><tr><th>Método</th><th>Métricas</th><th>Problemas</th><th>Score</th></tr></thead>
    <tbody>${rows}</tbody>
  </table>`;
}

export function buildClassCard(cls, doc = document) {
  const level  = cls.level || levelFromScore(cls.score);
  const score  = cls.score || 0;
  const isGod  = cls.badMethodCount >= 2;

  let badgeClass = level === 'BUENO' ? 'badge-good'
                 : level === 'REGULAR' ? 'badge-regular' : 'badge-bad';
  let badgeText  = level;
  if (isGod) { badgeClass = 'badge-god'; badgeText = '🔥 God Class'; }

  const card = doc.createElement('div');
  card.className = 'class-card';
  card.dataset.level = level;
  card.dataset.god   = isGod ? 'true' : 'false';

  card.innerHTML = `
    <div class="class-header">
      <div class="score-text">${score}</div>
      <div class="class-name">${cls.className}</div>
      <div class="class-meta">${cls.methods?.length || 0} método(s)</div>
      <span class="badge ${badgeClass}">${badgeText}</span>
      <span class="chevron">⌄</span>
    </div>
    <div class="methods-panel">
      ${buildMethodsTable(cls)}
    </div>`;

  return card;
}

export function toggleClass(header) {
  header.classList.toggle('open');
  const panel = header.nextElementSibling;
  panel.classList.toggle('open');
}

export function applyFilter(btn, doc = document) {
  doc.querySelectorAll('.pill').forEach(p => p.classList.remove('active'));
  btn.classList.add('active');
  currentFilter = btn.dataset.filter;

  const cards = doc.querySelectorAll('.class-card');
  let visible = 0;
  cards.forEach(card => {
    const show = cardMatchesFilter(card.dataset, currentFilter);
    card.style.display = show ? 'block' : 'none';
    if (show) visible++;
  });

  doc.getElementById('emptyState').style.display = visible === 0 ? 'flex' : 'none';
}