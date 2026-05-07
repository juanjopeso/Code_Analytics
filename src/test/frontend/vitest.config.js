import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    // Simula el DOM del navegador con jsdom
    environment: 'jsdom',

    // Reportes: texto legible en consola + junit XML para CI
    reporters: ['verbose', 'junit'],
    outputFile: './test-results/junit.xml',

    // Cobertura de las funciones JS extraídas
    coverage: {
      provider: 'v8',
      reporter: ['text', 'html'],
      include: ['src/**/*.js'],
    },
  },
});