import { createApp } from 'vue'

import AuthWrapper from "./vues/auth/AuthWrapper.vue";
import { createRouter, createWebHistory } from 'vue-router';

import WelcomeHome from './vues/Home.vue'
import VueVisualisation from './vues/Visualisation.vue'
import SetupExperience from './vues/Experiences.vue'
import RDFMinerPublications from './vues/Publications.vue'

const routes = [
    { path: '/', component: WelcomeHome },
    { path: '/experience', component: SetupExperience },
    { path: '/visualisation', component: VueVisualisation },
    { path: '/publications', component: RDFMinerPublications }
]

const router = createRouter({
    // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
    history: createWebHistory(),
    routes, // short for `routes: routes`
})

createApp(AuthWrapper).use(router).mount('#app')
