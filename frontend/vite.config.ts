import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    // 默认连后端 8080；若 8080 被占用，可改为 8081 并以后端 --server.port=8081 启动
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      },
      '/uploads': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})

