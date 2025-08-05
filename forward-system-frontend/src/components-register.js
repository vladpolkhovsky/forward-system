import { createApp } from 'vue'

// Импорт Bootstrap CSS
import 'bootstrap/dist/css/bootstrap.min.css'

// Импорт Bootstrap Icons
import 'bootstrap-icons/font/bootstrap-icons.css'

// Импорт Bootstrap JS (с Popper)
import 'bootstrap/dist/js/bootstrap.bundle.min.js'

// Импортируем все компоненты автоматически
const components = import.meta.glob('./**/*.(vue|ts|js|d.ts|scss|css)', { eager: true })

// Функция для регистрации компонентов
window.registerVueComponents = () => {
    document.querySelectorAll('[data-vue-component]').forEach(el => {
        const componentName = el.dataset.vueComponent
        const component = components[`./components/${componentName}.vue`]?.default

        if (component) {
            const props = JSON.parse(el.dataset.props || '{}')
            createApp(component, props).mount(el)
        } else {
            console.error(`Component ${componentName} not found!`)
        }
    })
}

// Автоматическая инициализация при загрузке
if (document.readyState === 'complete') {
    window.registerVueComponents()
} else {
    document.addEventListener('DOMContentLoaded', window.registerVueComponents)
}