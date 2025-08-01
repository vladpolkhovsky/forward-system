import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

import path from 'path'

export default defineConfig({
    plugins: [vue()],

    base: '/vue-dist/',
    build: {
        outDir: '../src/main/resources/static/vue',
        emptyOutDir: true,
        manifest: true,
        rollupOptions: {
            input: {
                components: './src/components-register.js'
            },
            output: {
                entryFileNames: `[name].js`,
                chunkFileNames: `[name].js`,
                assetFileNames: `[name].[ext]`
            }
        }
    },
    server: {
        port: 5173,
        strictPort: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            },
            '/static': 'http://localhost:8080'
        }
    },
    resolve: {
        alias: {
            '~bootstrap': path.resolve(__dirname, 'bootstrap'),
            '@': path.resolve(__dirname, './src')
        }
    }
})