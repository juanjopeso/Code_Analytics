/**
 * fixtures.js  —  datos de prueba reutilizables
 *
 * Exporta factories que devuelven objetos frescos en cada llamada
 * para evitar mutaciones entre tests.
 */

export const makeMethod = (overrides = {}) => ({
  metrics: {
    methodName: 'testMethod',
    loc: 10,
    cyclomaticComplexity: 2,
    ...overrides.metrics,
  },
  evaluation: {
    score: 100,
    level: 'BUENO',
    issues: [],
    ...overrides.evaluation,
  },
});

export const makeClass = (overrides = {}) => ({
  className: 'MiClase',
  score: 100,
  badMethodCount: 0,
  methods: [makeMethod()],
  aiClassSuggestion: null,
  ...overrides,
});

export const makeIssue = (overrides = {}) => ({
  type: 'High Complexity',
  description: 'CC muy alta (10)',
  suggestion: 'Aplica Strategy Pattern.',
  ...overrides,
});

/** Clase limpia: 1 método, score=100 */
export const cleanClass = () => makeClass();

/** Clase con un método problemático (Long Method + High Complexity) */
export const complexClass = () => makeClass({
  className: 'GodCandidate',
  score: 30,
  badMethodCount: 3,
  methods: [
    makeMethod({
      metrics: { methodName: 'procesarTodo', loc: 80, cyclomaticComplexity: 12 },
      evaluation: {
        score: 20,
        level: 'MALO',
        issues: [
          makeIssue({ type: 'Long Method',      description: 'Más de 50 líneas (80)' }),
          makeIssue({ type: 'High Complexity',  description: 'CC muy alta (12)' }),
          makeIssue({ type: 'Risky Method',     description: 'Largo y complejo' }),
        ],
      },
    }),
    makeMethod({
      metrics: { methodName: 'calcular', loc: 40, cyclomaticComplexity: 6 },
      evaluation: {
        score: 60,
        level: 'REGULAR',
        issues: [makeIssue({ type: 'Moderate Complexity', description: 'CC moderada (6)' })],
      },
    }),
    makeMethod({
      metrics: { methodName: 'validar', loc: 35, cyclomaticComplexity: 5 },
      evaluation: {
        score: 70,
        level: 'REGULAR',
        issues: [makeIssue({ type: 'Risky Method', description: 'Largo y complejo' })],
      },
    }),
  ],
  aiClassSuggestion: 'Considera dividir esta clase siguiendo SRP.',
});

/** Array de resultados mixtos para tests de resumen y filtros */
export const mixedResults = () => [
  makeClass({ className: 'Buena',   score: 95, badMethodCount: 0 }),
  makeClass({ className: 'Regular', score: 65, badMethodCount: 1 }),
  complexClass(),
];

// ── DOM helper ─────────────────────────────────────────

/**
 * Crea un document jsdom mínimo con todos los IDs que usa ui-logic.js.
 * Cada test que necesite DOM llama a esta función y trabaja con el doc retornado.
 */
export function buildDOM() {
  document.body.innerHTML = `
    <input  id="pathInput"    value="" />
    <button id="analyzeBtn">Analizar →</button>
    <div    id="progressWrap"></div>
    <span   id="progressLabel"></span>
    <div    id="progressFill" style="width:0%"></div>
    <div    id="errorBox"></div>
    <div    id="summaryGrid">
      <div class="stat-card c-blue">  <span id="sv-classes">0</span></div>
      <div class="stat-card c-purple"><span id="sv-methods">0</span></div>
      <div class="stat-card c-green"> <span id="sv-score">—</span></div>
      <div class="stat-card c-amber"> <span id="sv-issues">0</span></div>
      <div class="stat-card c-red">   <span id="sv-god">0</span></div>
    </div>
    <section id="results">
      <div id="classCards"></div>
      <div id="emptyState" style="display:none"></div>
    </section>
    <div id="filterBar">
      <button class="pill active" data-filter="all">Todos</button>
      <button class="pill" data-filter="MALO">Malo</button>
      <button class="pill" data-filter="REGULAR">Regular</button>
      <button class="pill" data-filter="BUENO">Bueno</button>
      <button class="pill" data-filter="god">God Class</button>
    </div>
  `;
  return document;
}