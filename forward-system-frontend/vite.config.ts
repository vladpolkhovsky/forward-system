import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'
import cssInjectedByJsPlugin from 'vite-plugin-css-injected-by-js'

import path from 'path'

export default defineConfig({
    plugins: [
        vue(),
        cssInjectedByJsPlugin()
    ],
    base: '/static/vue/',
    build: {
        outDir: '../src/main/resources/static/vue',
        emptyOutDir: true,
        manifest: true,
        rollupOptions: {
            input: {
                components: './src/components-register.js',
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
            '/static': 'http://localhost:8081/static'
        }
    },
    resolve: {
        alias: {
            'bootstrap': path.resolve(__dirname, './node_modules/bootstrap'),
            'bootstrap-icons': path.resolve(__dirname, './node_modules/bootstrap-icons'),
            '@': path.resolve(__dirname, './src')
        }
    },
    css: {
        preprocessorOptions: {
            scss: {
                additionalData: `
                  $bootstrap-icons-font-dir: "node_modules/bootstrap-icons/font/fonts";
                  @import "bootstrap";
                  @import "bootstrap-icons/font/bootstrap-icons";
                `
            }
        }
    }
})