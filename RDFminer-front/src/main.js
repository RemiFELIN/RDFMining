import { createApp } from 'vue'

import App from "./App.vue";
import { createRouter, createWebHistory } from 'vue-router';

import unoverlay from 'unoverlay-vue';
import VueCookies from 'vue3-cookies';

import WelcomeHome from './vues/Home.vue'
import VueVisualisation from './vues/vis/Visualisation.vue'
import MyProjects from './vues/projects/MyProjects.vue'
import RDFMinerPublications from './vues/Publications.vue'
// import SwaggerDoc from './rdfminer/SwaggerDoc.vue'
// import LogIn from './vues/LogIn.vue'

const cookiesConfig = {
    expireTimes: "1d",
    secure: true
};

const routes = [
    { path: '/', component: WelcomeHome },
    { path: '/projects', component: MyProjects },
    // here, the id corresponds to the results ID (linked to the project)
    { path: '/visualisation/:task/:resultsId', component: VueVisualisation, name: "VueVisualisation" },
    { path: '/publications', component: RDFMinerPublications },
    // { path: '/api', component: SwaggerDoc },
    // { path: '/login', component: LogIn }
]

const router = createRouter({
    // 4. Provide the history implementation to use. We are using the hash history for simplicity here.
    history: createWebHistory(),
    routes, // short for `routes: routes`
})

createApp(App).use(router).use(VueCookies, cookiesConfig).use(unoverlay).mount('#app')
