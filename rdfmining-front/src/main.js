import { createApp } from 'vue'
import App from './App.vue'
import { createRouter, createWebHistory } from 'vue-router';

import WelcomeHome from './vues/Home.vue'
import VueVisualisation from './vues/Visualisation.vue'
import SetupExperience from './vues/Experiences.vue'

const routes = [
    { path: '/', component: WelcomeHome },
    { path: '/experience', component: SetupExperience },
    { path: '/visualisation', component: VueVisualisation }
]

const router = createRouter({
    // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
    history: createWebHistory(),
    routes, // short for `routes: routes`
})

createApp(App).use(router).mount('#app')
