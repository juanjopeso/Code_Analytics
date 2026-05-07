/**
 * pure-functions.test.js
 *
 * Pruebas para las funciones puras de ui-logic.js.
 * NO necesitan DOM — son las más rápidas y estables.
 */
import { describe, it, expect } from 'vitest';
import {
  levelFromScore,
  dotClass,
  computeSummary,
  cardMatchesFilter,
} from '../../../main/resources/public/ui-logic.js';
import { makeClass, makeMethod, makeIssue } from '../fixtures.js';

// ════════════════════════════════════════════════════════
// levelFromScore
// ════════════════════════════════════════════════════════
describe('levelFromScore', () => {

  it('returns BUENO for score exactly 80', () => {
    expect(levelFromScore(80)).toBe('BUENO');
  });

  it('returns BUENO for score above 80', () => {
    expect(levelFromScore(100)).toBe('BUENO');
    expect(levelFromScore(95)).toBe('BUENO');
  });

  it('returns REGULAR for score exactly 50', () => {
    expect(levelFromScore(50)).toBe('REGULAR');
  });

  it('returns REGULAR for scores between 50 and 79', () => {
    expect(levelFromScore(79)).toBe('REGULAR');
    expect(levelFromScore(65)).toBe('REGULAR');
  });

  it('returns MALO for score exactly 49', () => {
    expect(levelFromScore(49)).toBe('MALO');
  });

  it('returns MALO for score 0', () => {
    expect(levelFromScore(0)).toBe('MALO');
  });

  it('returns MALO for negative score', () => {
    // Defensa ante datos corruptos del backend
    expect(levelFromScore(-5)).toBe('MALO');
  });
});

// ════════════════════════════════════════════════════════
// dotClass
// ════════════════════════════════════════════════════════
describe('dotClass', () => {

  it('returns dot-high for "High Complexity"', () => {
    expect(dotClass('High Complexity')).toBe('dot-high');
  });

  it('returns dot-high for "Risky Method"', () => {
    expect(dotClass('Risky Method')).toBe('dot-high');
  });

  it('returns dot-high for "GOD_CLASS" (uppercase)', () => {
    expect(dotClass('GOD_CLASS')).toBe('dot-high');
  });

  it('returns dot-medium for "Moderate Complexity"', () => {
    expect(dotClass('Moderate Complexity')).toBe('dot-medium');
  });

  it('returns dot-medium for "Long Method"', () => {
    expect(dotClass('Long Method')).toBe('dot-medium');
  });

  it('returns dot-low for unknown type', () => {
    expect(dotClass('Unknown Issue')).toBe('dot-low');
  });

  it('returns dot-low for null', () => {
    expect(dotClass(null)).toBe('dot-low');
  });

  it('returns dot-low for empty string', () => {
    expect(dotClass('')).toBe('dot-low');
  });

  it('is case-insensitive', () => {
    expect(dotClass('HIGH COMPLEXITY')).toBe('dot-high');
    expect(dotClass('long method')).toBe('dot-medium');
  });
});

// ════════════════════════════════════════════════════════
// computeSummary
// ════════════════════════════════════════════════════════
describe('computeSummary', () => {

  it('returns all zeros for empty array', () => {
    expect(computeSummary([])).toEqual({
      classes: 0, methods: 0, avgScore: 0, issues: 0, godClasses: 0,
    });
  });

  it('returns all zeros for null input', () => {
    expect(computeSummary(null)).toEqual({
      classes: 0, methods: 0, avgScore: 0, issues: 0, godClasses: 0,
    });
  });

  it('counts classes correctly', () => {
    const results = [makeClass(), makeClass(), makeClass()];
    expect(computeSummary(results).classes).toBe(3);
  });

  it('sums methods across all classes', () => {
    const cls1 = makeClass({ methods: [makeMethod(), makeMethod()] });
    const cls2 = makeClass({ methods: [makeMethod()] });
    expect(computeSummary([cls1, cls2]).methods).toBe(3);
  });

  it('computes average score rounded', () => {
    const results = [
      makeClass({ score: 100 }),
      makeClass({ score: 60 }),
      makeClass({ score: 50 }),
    ];
    // (100 + 60 + 50) / 3 = 70
    expect(computeSummary(results).avgScore).toBe(70);
  });

  it('rounds avgScore correctly (no decimals)', () => {
    const results = [makeClass({ score: 100 }), makeClass({ score: 99 })];
    // (100 + 99) / 2 = 99.5 → rounds to 100
    expect(Number.isInteger(computeSummary(results).avgScore)).toBe(true);
  });

  it('counts total issues across all methods', () => {
    const methodWithIssues = makeMethod({
      evaluation: { score: 40, issues: [makeIssue(), makeIssue()] },
    });
    const cls = makeClass({ methods: [methodWithIssues, makeMethod()] });
    expect(computeSummary([cls]).issues).toBe(2);
  });

  it('counts god classes (badMethodCount >= 2)', () => {
    const results = [
      makeClass({ badMethodCount: 0 }),
      makeClass({ badMethodCount: 2 }), // god
      makeClass({ badMethodCount: 3 }), // god
    ];
    expect(computeSummary(results).godClasses).toBe(2);
  });

  it('does NOT count class with badMethodCount = 1 as god class', () => {
    const results = [makeClass({ badMethodCount: 1 })];
    expect(computeSummary(results).godClasses).toBe(0);
  });

  it('handles class with no methods gracefully', () => {
    const cls = makeClass({ methods: [] });
    const summary = computeSummary([cls]);
    expect(summary.methods).toBe(0);
    expect(summary.issues).toBe(0);
  });
});

// ════════════════════════════════════════════════════════
// cardMatchesFilter
// ════════════════════════════════════════════════════════
describe('cardMatchesFilter', () => {

  it('"all" filter always shows the card', () => {
    expect(cardMatchesFilter({ level: 'MALO',    god: 'false' }, 'all')).toBe(true);
    expect(cardMatchesFilter({ level: 'BUENO',   god: 'true'  }, 'all')).toBe(true);
  });

  it('"god" filter only shows cards with god=true', () => {
    expect(cardMatchesFilter({ level: 'MALO', god: 'true'  }, 'god')).toBe(true);
    expect(cardMatchesFilter({ level: 'MALO', god: 'false' }, 'god')).toBe(false);
  });

  it('"BUENO" filter matches only level BUENO', () => {
    expect(cardMatchesFilter({ level: 'BUENO',   god: 'false' }, 'BUENO')).toBe(true);
    expect(cardMatchesFilter({ level: 'REGULAR', god: 'false' }, 'BUENO')).toBe(false);
    expect(cardMatchesFilter({ level: 'MALO',    god: 'false' }, 'BUENO')).toBe(false);
  });

  it('"REGULAR" filter matches only level REGULAR', () => {
    expect(cardMatchesFilter({ level: 'REGULAR', god: 'false' }, 'REGULAR')).toBe(true);
    expect(cardMatchesFilter({ level: 'BUENO',   god: 'false' }, 'REGULAR')).toBe(false);
  });

  it('"MALO" filter matches only level MALO', () => {
    expect(cardMatchesFilter({ level: 'MALO',  god: 'false' }, 'MALO')).toBe(true);
    expect(cardMatchesFilter({ level: 'BUENO', god: 'false' }, 'MALO')).toBe(false);
  });
});