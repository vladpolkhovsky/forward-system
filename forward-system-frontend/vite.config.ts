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
                  
                    /* cyrillic */
                    @font-face {
                        font-family: 'Montserrat';
                        font-style: normal;
                        font-weight: 100 900;
                        font-display: swap;
                        src: url(/static/montserrat-bi.woff2) format('woff2');
                        unicode-range: U+0301, U+0400-045F, U+0490-0491, U+04B0-04B1, U+2116;
                    }
                    
                    /* latin */
                    @font-face {
                        font-family: 'Montserrat';
                        font-style: normal;
                        font-weight: 100 900;
                        font-display: swap;
                        src: url(/static/montserrat.woff2) format('woff2');
                        unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+0304, U+0308, U+0329, U+2000-206F, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
                    }
                `
            }
        }
    }
})