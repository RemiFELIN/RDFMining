import { createApp } from 'vue'

import App from "./App.vue";
import { createRouter, createWebHistory } from 'vue-router';

import unoverlay from 'unoverlay-vue';

import WelcomeHome from './vues/Home.vue'
// import VueVisualisation from './vues/Visualisation.vue'
import CreateProject from './vues/projects/CreateProject.vue'
import RDFMinerPublications from './vues/Publications.vue'
// import LogIn from './vues/LogIn.vue'

const routes = [
    { path: '/', component: WelcomeHome },
    { path: '/projects', component: CreateProject },
    // { path: '/visualisation', component: VueVisualisation },
    { path: '/publications', component: RDFMinerPublications },
    // { path: '/login', component: LogIn }
]

const router = createRouter({
    // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
    history: createWebHistory(),
    routes, // short for `routes: routes`
})

createApp(App).use(router).use(unoverlay).mount('#app')
