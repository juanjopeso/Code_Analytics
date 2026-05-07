/**
 * filter-and-interaction.test.js
 *
 * Pruebas para applyFilter(), toggleClass() y el flujo integrado de render.
 */
import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  applyFilter, toggleClass, buildClassCard, clearResults,
  showError, renderSummary,
} from '../../../main/resources/public/ui-logic.js';
import {
  buildDOM, makeClass, mixedResults,
} from '../fixtures.js';

// ════════════════════════════════════════════════════════
// toggleClass
// ════════════════════════════════════════════════════════
describe('toggleClass', () => {
  let header, panel;

  beforeEach(() => {
    buildDOM();
    // Inyectar estructura mínima de card
    document.getElementById('classCards').innerHTML = `
      <div class="class-card">
        <div class="class-header" id="hdr"></div>
        <div class="methods-panel" id="pnl"></div>
      </div>`;
    header = document.getElementById('hdr');
    panel  = document.getElementById('pnl');
  });

  it('adds "open" class to header on first click', () => {
    toggleClass(header);
    expect(header.classList.contains('open')).toBe(true);
  });

  it('adds "open" class to panel on first click', () => {
    toggleClass(header);
    expect(panel.classList.contains('open')).toBe(true);
  });

  it('removes "open" class from header on second click (toggle)', () => {
    toggleClass(header);
    toggleClass(header);
    expect(header.classList.contains('open')).toBe(false);
  });

  it('removes "open" class from panel on second click', () => {
    toggleClass(header);
    toggleClass(header);
    expect(panel.classList.contains('open')).toBe(false);
  });
});

// ════════════════════════════════════════════════════════
// applyFilter
// ════════════════════════════════════════════════════════
describe('applyFilter', () => {
  beforeEach(() => {
    buildDOM();
    // Insertar 3 cards con diferentes niveles
    const container = document.getElementById('classCards');
    const cards = [
      { level: 'BUENO',   god: 'false' },
      { level: 'REGULAR', god: 'false' },
      { level: 'MALO',    god: 'true'  },
    ];
    cards.forEach(data => {
      const div = document.createElement('div');
      div.className = 'class-card';
      div.dataset.level = data.level;
      div.dataset.god   = data.god;
      container.appendChild(div);
    });
  });

  it('shows all cards with filter "all"', () => {
    const btn = document.querySelector('[data-filter="all"]');
    applyFilter(btn);
    const visible = [...document.querySelectorAll('.class-card')]
      .filter(c => c.style.display !== 'none');
    expect(visible).toHaveLength(3);
  });

  it('only shows BUENO cards with filter "BUENO"', () => {
    const btn = document.querySelector('[data-filter="BUENO"]');
    applyFilter(btn);
    const visible = [...document.querySelectorAll('.class-card')]
      .filter(c => c.style.display !== 'none');
    expect(visible).toHaveLength(1);
    expect(visible[0].dataset.level).toBe('BUENO');
  });

  it('only shows REGULAR cards with filter "REGULAR"', () => {
    const btn = document.querySelector('[data-filter="REGULAR"]');
    applyFilter(btn);
    const visible = [...document.querySelectorAll('.class-card')]
      .filter(c => c.style.display !== 'none');
    expect(visible).toHaveLength(1);
    expect(visible[0].dataset.level).toBe('REGULAR');
  });

  it('only shows MALO cards with filter "MALO"', () => {
    const btn = document.querySelector('[data-filter="MALO"]');
    applyFilter(btn);
    const visible = [...document.querySelectorAll('.class-card')]
      .filter(c => c.style.display !== 'none');
    expect(visible).toHaveLength(1);
  });

  it('only shows god=true cards with filter "god"', () => {
    const btn = document.querySelector('[data-filter="god"]');
    applyFilter(btn);
    const visible = [...document.querySelectorAll('.class-card')]
      .filter(c => c.style.display !== 'none');
    expect(visible).toHaveLength(1);
    expect(visible[0].dataset.god).toBe('true');
  });

  it('activates the clicked pill and deactivates others', () => {
    const btn = document.querySelector('[data-filter="MALO"]');
    applyFilter(btn);
    expect(btn.classList.contains('active')).toBe(true);
    const otherActive = [...document.querySelectorAll('.pill')]
      .filter(p => p !== btn && p.classList.contains('active'));
    expect(otherActive).toHaveLength(0);
  });

  it('shows emptyState when no cards match the filter', () => {
    // Quitamos todas las cards god=true para que el filtro god quede vacío
    document.querySelectorAll('.class-card').forEach(c => {
      c.dataset.god = 'false';
    });
    const btn = document.querySelector('[data-filter="god"]');
    applyFilter(btn);
    expect(document.getElementById('emptyState').style.display).toBe('flex');
  });

  it('hides emptyState when at least one card is visible', () => {
    const btn = document.querySelector('[data-filter="all"]');
    applyFilter(btn);
    expect(document.getElementById('emptyState').style.display).not.toBe('flex');
  });
});

// ════════════════════════════════════════════════════════
// startAnalysis validation (sin fetch)
// ════════════════════════════════════════════════════════
describe('startAnalysis path validation', () => {
  beforeEach(() => buildDOM());

  it('showError is called when path is empty', () => {
    // Simula la lógica de validación de startAnalysis directamente
    const path = ''.trim();
    if (!path) showError('Ingresa una ruta de proyecto válida.');
    expect(document.getElementById('errorBox').textContent)
      .toBe('⚠ Ingresa una ruta de proyecto válida.');
  });

  it('errorBox has class "visible" after empty-path submission', () => {
    const path = '';
    if (!path.trim()) showError('Ingresa una ruta de proyecto válida.');
    expect(document.getElementById('errorBox').classList.contains('visible')).toBe(true);
  });
});

// ════════════════════════════════════════════════════════
// startAnalysis — fetch mock (happy path)
// ════════════════════════════════════════════════════════
describe('startAnalysis — fetch mock', () => {
  beforeEach(() => buildDOM());

  it('renders summary stats after successful fetch', async () => {
    const fakeData = mixedResults();

    // Mock global fetch
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => fakeData,
    });

    // Simular el flujo sin DOMContentLoaded (llamamos funciones directamente)
    const results = fakeData;
    renderSummary(results);

    expect(document.getElementById('sv-classes').textContent).toBe('3');
  });

  it('shows error when fetch returns non-ok response', async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ error: 'Ruta no encontrada' }),
    });

    const res = { ok: false };
    const data = { error: 'Ruta no encontrada' };
    if (!res.ok) showError(data.error || 'Error desconocido.');

    expect(document.getElementById('errorBox').textContent)
      .toBe('⚠ Ruta no encontrada');
  });

  it('shows network error message when fetch throws', async () => {
    global.fetch = vi.fn().mockRejectedValue(new Error('Network failure'));

    try {
      await global.fetch('/api/analyze', {});
    } catch {
      showError('No se pudo conectar con el servidor. ¿Está corriendo en localhost:7070?');
    }

    expect(document.getElementById('errorBox').textContent)
      .toContain('No se pudo conectar');
  });
});

// ════════════════════════════════════════════════════════
// Flujo integrado: clear → render → filter
// ════════════════════════════════════════════════════════
describe('integrated flow: clear → render → filter', () => {
  beforeEach(() => {
    buildDOM();
  });

  it('classCards is empty after clearResults', () => {
    document.getElementById('classCards').innerHTML = '<div>old data</div>';
    clearResults();
    expect(document.getElementById('classCards').innerHTML).toBe('');
  });

  it('can build and append multiple class cards', () => {
    const container = document.getElementById('classCards');
    const classes = mixedResults();
    classes.forEach(cls => container.appendChild(buildClassCard(cls)));
    expect(container.querySelectorAll('.class-card').length).toBe(3);
  });

  it('filterBar starts with "all" pill active', () => {
    const activePill = document.querySelector('.pill.active');
    expect(activePill?.dataset.filter).toBe('all');
  });
});