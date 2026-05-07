/**
 * dom-functions.test.js
 *
 * Pruebas para las funciones que leen/escriben el DOM.
 * Cada test llama a buildDOM() para partir de un estado limpio.
 */
import { describe, it, expect, beforeEach } from 'vitest';
import {
  showError, hideError, showEmpty,
  clearResults, setLoading, renderSummary,
} from '../../../main/resources/public/ui-logic.js';
import { buildDOM, makeClass, makeMethod, makeIssue } from '../fixtures.js';

// ════════════════════════════════════════════════════════
// showError / hideError
// ════════════════════════════════════════════════════════
describe('showError', () => {
  beforeEach(() => buildDOM());

  it('prefixes the message with the warning symbol ⚠', () => {
    showError('Ruta inválida.');
    expect(document.getElementById('errorBox').textContent).toBe('⚠ Ruta inválida.');
  });

  it('adds the "visible" class to errorBox', () => {
    showError('Error de red.');
    expect(document.getElementById('errorBox').classList.contains('visible')).toBe(true);
  });

  it('replaces previous error message', () => {
    showError('Primer error');
    showError('Segundo error');
    expect(document.getElementById('errorBox').textContent).toBe('⚠ Segundo error');
  });
});

describe('hideError', () => {
  beforeEach(() => buildDOM());

  it('removes "visible" class from errorBox', () => {
    const el = document.getElementById('errorBox');
    el.classList.add('visible');
    hideError();
    expect(el.classList.contains('visible')).toBe(false);
  });

  it('is idempotent when called twice', () => {
    hideError();
    hideError();
    expect(document.getElementById('errorBox').classList.contains('visible')).toBe(false);
  });
});

// ════════════════════════════════════════════════════════
// showEmpty
// ════════════════════════════════════════════════════════
describe('showEmpty', () => {
  beforeEach(() => buildDOM());

  it('adds "visible" class to #results', () => {
    showEmpty();
    expect(document.getElementById('results').classList.contains('visible')).toBe(true);
  });

  it('sets emptyState display to flex', () => {
    showEmpty();
    expect(document.getElementById('emptyState').style.display).toBe('flex');
  });
});

// ════════════════════════════════════════════════════════
// clearResults
// ════════════════════════════════════════════════════════
describe('clearResults', () => {
  beforeEach(() => {
    buildDOM();
    // Simular estado con datos
    document.getElementById('classCards').innerHTML = '<div class="class-card">Dummy</div>';
    document.getElementById('results').classList.add('visible');
    document.getElementById('summaryGrid').style.display = 'grid';
    document.getElementById('emptyState').style.display = 'flex';
    document.querySelectorAll('.stat-card').forEach(c => c.classList.add('visible'));
  });

  it('empties classCards container', () => {
    clearResults();
    expect(document.getElementById('classCards').innerHTML).toBe('');
  });

  it('removes "visible" class from #results', () => {
    clearResults();
    expect(document.getElementById('results').classList.contains('visible')).toBe(false);
  });

  it('hides summaryGrid', () => {
    clearResults();
    expect(document.getElementById('summaryGrid').style.display).toBe('none');
  });

  it('hides emptyState', () => {
    clearResults();
    expect(document.getElementById('emptyState').style.display).toBe('none');
  });

  it('removes "visible" class from every stat-card', () => {
    clearResults();
    const stillVisible = [...document.querySelectorAll('.stat-card')]
      .filter(c => c.classList.contains('visible'));
    expect(stillVisible).toHaveLength(0);
  });
});

// ════════════════════════════════════════════════════════
// setLoading
// ════════════════════════════════════════════════════════
describe('setLoading', () => {
  beforeEach(() => buildDOM());

  it('disables the analyze button when loading=true', () => {
    setLoading(true);
    expect(document.getElementById('analyzeBtn').disabled).toBe(true);
  });

  it('changes button text to "Analizando…" when loading=true', () => {
    setLoading(true);
    expect(document.getElementById('analyzeBtn').textContent).toBe('Analizando…');
  });

  it('adds "visible" class to progressWrap when loading=true', () => {
    setLoading(true);
    expect(document.getElementById('progressWrap').classList.contains('visible')).toBe(true);
  });

  it('re-enables the button when loading=false', () => {
    setLoading(true);
    setLoading(false);
    expect(document.getElementById('analyzeBtn').disabled).toBe(false);
  });

  it('restores button text to "Analizar →" when loading=false', () => {
    setLoading(true);
    setLoading(false);
    expect(document.getElementById('analyzeBtn').textContent).toBe('Analizar →');
  });

  it('removes "visible" from progressWrap when loading=false', () => {
    setLoading(true);
    setLoading(false);
    expect(document.getElementById('progressWrap').classList.contains('visible')).toBe(false);
  });
});

// ════════════════════════════════════════════════════════
// renderSummary
// ════════════════════════════════════════════════════════
describe('renderSummary', () => {
  beforeEach(() => buildDOM());

  it('renders class count correctly', () => {
    renderSummary([makeClass(), makeClass()]);
    expect(document.getElementById('sv-classes').textContent).toBe('2');
  });

  it('renders total methods correctly', () => {
    const cls = makeClass({ methods: [makeMethod(), makeMethod(), makeMethod()] });
    renderSummary([cls]);
    expect(document.getElementById('sv-methods').textContent).toBe('3');
  });

  it('renders average score (rounded)', () => {
    const results = [makeClass({ score: 80 }), makeClass({ score: 60 })];
    renderSummary(results);
    expect(document.getElementById('sv-score').textContent).toBe('70');
  });

  it('renders total issues count', () => {
    const methodWithIssues = makeMethod({
      evaluation: { score: 40, issues: [makeIssue(), makeIssue(), makeIssue()] },
    });
    const cls = makeClass({ methods: [methodWithIssues] });
    renderSummary([cls]);
    expect(document.getElementById('sv-issues').textContent).toBe('3');
  });

  it('renders god class count', () => {
    const results = [
      makeClass({ badMethodCount: 0 }),
      makeClass({ badMethodCount: 2 }),
    ];
    renderSummary(results);
    expect(document.getElementById('sv-god').textContent).toBe('1');
  });

  it('sets summaryGrid display to "grid"', () => {
    renderSummary([makeClass()]);
    expect(document.getElementById('summaryGrid').style.display).toBe('grid');
  });
});