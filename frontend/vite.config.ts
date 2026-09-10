import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

const api = ['/auth', '/budgets', '/labels', '/transactions']

// Built output lands where Spring serves static files, so `mvnw spring-boot:run`
// alone is enough once the frontend has been built.
export default defineConfig({
  plugins: [react()],
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
  server: {
    proxy: Object.fromEntries(
      api.map((path) => [path, { target: 'http://localhost:8080', changeOrigin: true }]),
    ),
  },
})
