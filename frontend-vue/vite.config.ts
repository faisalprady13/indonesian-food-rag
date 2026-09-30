import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import path from 'node:path'
import { loadEnv } from 'vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, path.resolve(process.cwd(), '..'), '')
  const apiTarget = env.VITE_API_URL || 'http://localhost:8080'

  return {
    plugins: [vue(), tailwindcss()],
    server: {
      proxy: {
        '/api': {
          target: apiTarget,
          changeOrigin: true,
        },
        '/oauth2': {
          target: apiTarget,
          changeOrigin: true,
        },
      },
    },
  }
})
