/**
 * card-builders.test.js
 *
 * Pruebas para buildClassCard(), buildMethodsTable() y buildAIBox().
 * Verifican el HTML generado con un parser DOM real (jsdom).
 */
import { describe, it, expect, beforeEach } from 'vitest';
import {
  buildClassCard, buildMethodsTable, buildAIBox,
  levelFromScore,
} from '../../../main/resources/public/ui-logic.js';
import {
  buildDOM, makeClass, makeMethod, makeIssue, cleanClass, complexClass,
} from '../fixtures.js';

// ════════════════════════════════════════════════════════
// buildAIBox
// ════════════════════════════════════════════════════════
describe('buildAIBox', () => {

  it('contains the label text', () => {
    const html = buildAIBox('Sugerencia IA', 'Refactoriza.');
    expect(html).toContain('Sugerencia IA');
  });

  it('contains the body text', () => {
    const html = buildAIBox('Label', 'El cuerpo aquí.');
    expect(html).toContain('El cuerpo aquí.');
  });

  it('includes the ai-box class', () => {
    const html = buildAIBox('X', 'Y');
    expect(html).toContain('class="ai-box"');
  });

  it('includes the ✦ decorative symbol', () => {
    const html = buildAIBox('X', 'Y');
    expect(html).toContain('✦');
  });
});

// ════════════════════════════════════════════════════════
// buildMethodsTable — sin métodos
// ════════════════════════════════════════════════════════
describe('buildMethodsTable — empty', () => {

  it('shows "Sin métodos encontrados" when methods array is empty', () => {
    const html = buildMethodsTable(makeClass({ methods: [] }));
    expect(html).toContain('Sin métodos encontrados');
  });

  it('shows "Sin métodos encontrados" when methods is undefined', () => {
    const cls = makeClass();
    delete cls.methods;
    const html = buildMethodsTable(cls);
    expect(html).toContain('Sin métodos encontrados');
  });
});

// ════════════════════════════════════════════════════════
// buildMethodsTable — con métodos limpios
// ════════════════════════════════════════════════════════
describe('buildMethodsTable — clean method', () => {
  let doc;

  beforeEach(() => {
    doc = buildDOM();
    const cls = makeClass({
      methods: [makeMethod({ metrics: { methodName: 'calcular', loc: 10, cyclomaticComplexity: 2 } })],
    });
    doc.getElementById('classCards').innerHTML = buildMethodsTable(cls);
  });

  it('renders the method name', () => {
    expect(doc.querySelector('.method-name').textContent).toBe('calcular');
  });

  it('renders the LOC chip', () => {
    const chips = [...doc.querySelectorAll('.metric-chip')].map(c => c.textContent);
    expect(chips.some(t => t.includes('LOC') && t.includes('10'))).toBe(true);
  });

  it('renders the CC chip', () => {
    const chips = [...doc.querySelectorAll('.metric-chip')].map(c => c.textContent);
    expect(chips.some(t => t.includes('CC') && t.includes('2'))).toBe(true);
  });

  it('shows "Sin problemas" for a clean method', () => {
    expect(doc.querySelector('.no-issues').textContent).toContain('Sin problemas');
  });

  it('does NOT render an AI box for a clean method', () => {
    expect(doc.querySelector('.ai-box')).toBeNull();
  });
});

// ════════════════════════════════════════════════════════
// buildMethodsTable — con issues
// ════════════════════════════════════════════════════════
describe('buildMethodsTable — method with issues', () => {
  let doc;
  const issue1 = makeIssue({ type: 'High Complexity',  description: 'CC alta (10)', suggestion: 'Usar Strategy.' });
  const issue2 = makeIssue({ type: 'Long Method',      description: 'Más de 50 LOC',  suggestion: null });

  beforeEach(() => {
    doc = buildDOM();
    const method = makeMethod({
      metrics:    { methodName: 'bigMethod', loc: 70, cyclomaticComplexity: 10 },
      evaluation: { score: 20, issues: [issue1, issue2] },
    });
    doc.getElementById('classCards').innerHTML = buildMethodsTable(makeClass({ methods: [method] }));
  });

  it('renders each issue type in the table', () => {
    const text = doc.getElementById('classCards').textContent;
    expect(text).toContain('High Complexity');
    expect(text).toContain('Long Method');
  });

  it('renders the issue description', () => {
    expect(doc.getElementById('classCards').textContent).toContain('CC alta (10)');
  });

  it('renders a dot-high class for High Complexity issue', () => {
    const dots = doc.querySelectorAll('.dot-high');
    expect(dots.length).toBeGreaterThanOrEqual(1);
  });

  it('renders a dot-medium class for Long Method issue', () => {
    const dots = doc.querySelectorAll('.dot-medium');
    expect(dots.length).toBeGreaterThanOrEqual(1);
  });

  it('renders the AI suggestion box for the first issue with suggestion', () => {
    expect(doc.querySelector('.ai-box')).not.toBeNull();
    expect(doc.querySelector('.ai-box').textContent).toContain('Usar Strategy.');
  });

  it('does NOT render "Sin problemas" when there are issues', () => {
    expect(doc.querySelector('.no-issues')).toBeNull();
  });
});

// ════════════════════════════════════════════════════════
// buildClassCard — clase limpia
// ════════════════════════════════════════════════════════
describe('buildClassCard — clean class', () => {
  let card;

  beforeEach(() => {
    buildDOM();
    card = buildClassCard(cleanClass());
  });

  it('creates a div with class "class-card"', () => {
    expect(card.classList.contains('class-card')).toBe(true);
  });

  it('sets dataset.level to BUENO for score=100', () => {
    expect(card.dataset.level).toBe('BUENO');
  });

  it('sets dataset.god to false for clean class', () => {
    expect(card.dataset.god).toBe('false');
  });

  it('displays the class name', () => {
    expect(card.querySelector('.class-name').textContent).toBe('MiClase');
  });

  it('shows badge-good badge', () => {
    expect(card.querySelector('.badge-good')).not.toBeNull();
  });

  it('does NOT show god badge for clean class', () => {
    expect(card.querySelector('.badge-god')).toBeNull();
  });

  it('shows the score value', () => {
    expect(card.querySelector('.score-text').textContent).toBe('100');
  });
});

// ════════════════════════════════════════════════════════
// buildClassCard — God Class
// ════════════════════════════════════════════════════════
describe('buildClassCard — God Class', () => {
  let card;

  beforeEach(() => {
    buildDOM();
    card = buildClassCard(complexClass());
  });

  it('sets dataset.god to true', () => {
    expect(card.dataset.god).toBe('true');
  });

  it('shows the badge-god badge', () => {
    expect(card.querySelector('.badge-god')).not.toBeNull();
  });

  it('badge text includes "God Class"', () => {
    expect(card.querySelector('.badge-god').textContent).toContain('God Class');
  });

  it('sets dataset.level based on score', () => {
    // complexClass has score=30 → MALO
    expect(card.dataset.level).toBe('MALO');
  });
});

// ════════════════════════════════════════════════════════
// buildClassCard — niveles de badge
// ════════════════════════════════════════════════════════
describe('buildClassCard — badge levels', () => {
  beforeEach(() => buildDOM());

  it('shows badge-good for BUENO class', () => {
    const card = buildClassCard(makeClass({ score: 90, badMethodCount: 0 }));
    expect(card.querySelector('.badge-good')).not.toBeNull();
  });

  it('shows badge-regular for REGULAR class', () => {
    const card = buildClassCard(makeClass({ score: 65, badMethodCount: 0 }));
    expect(card.querySelector('.badge-regular')).not.toBeNull();
  });

  it('shows badge-bad for MALO class', () => {
    const card = buildClassCard(makeClass({ score: 20, badMethodCount: 0 }));
    expect(card.querySelector('.badge-bad')).not.toBeNull();
  });
});