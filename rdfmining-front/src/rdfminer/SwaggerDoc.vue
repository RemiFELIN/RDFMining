<template>
    <div v-html="html"></div>
    <!-- <open-api v-if="spec" :api="spec" md-theme="'default'"></open-api> -->
    <!-- <h1>{{ info.title }}</h1>
    <div class="app-desc">{{ info.description }}</div> -->
    <!-- <div class="app-desc">More information: <a href="https://helloreverb.com">https://helloreverb.com</a></div> -->
    <!-- <div class="app-desc">Contact Info: <a href="hello@helloreverb.com">hello@helloreverb.com</a></div> -->
    <!-- <div class="app-desc">Version: {{ info.version }}</div>
    <div class="app-desc">BasePath: {{ spec.basePath }}</div> -->
    <!-- <div class="license-info">All rights reserved</div> -->
    <!-- <div class="license-url">http://apache.org/licenses/LICENSE-2.0.html</div> -->
    <!-- <h2>Access</h2>

    <h2><a name="__Methods">Methods</a></h2> -->
    <!-- [ Jump to <a href="#__Models">Models</a> ] -->

    <!-- <h3>Table of Contents </h3>
    <div class="method-summary"></div>
    <h4>Default</h4>
    <ul>
        <li v-for="key in pathsKeys" :key="key"><code><span class="http-method"></span> /api/auth</code></li> -->
        <!-- <li><a href="#apiAuthGet"><code><span class="http-method">get</span> /api/auth</code></a></li>
        <li><a href="#apiProjectDeletePost"><code><span class="http-method">post</span> /api/project/delete</code></a></li>
        <li><a href="#apiProjectSetupPost"><code><span class="http-method">post</span> /api/project/setup</code></a></li>
        <li><a href="#apiProjectsGet"><code><span class="http-method">get</span> /api/projects</code></a></li>
        <li><a href="#apiPublicationsGet"><code><span class="http-method">get</span> /api/publications</code></a></li>
        <li><a href="#apiUserGet"><code><span class="http-method">get</span> /api/user</code></a></li>
        <li><a href="#apiUserPost"><code><span class="http-method">post</span> /api/user</code></a></li> -->
    <!-- </ul> -->
</template>
  
<script>
import axios from 'axios';
// import { toRaw } from 'vue';
// import OpenApi from 'vue-openapi'

export default {
    name: 'SwaggerDoc',
    data() {
        return {
            html: "",
            //
            info: {},
            spec: {},
            paths: {},
            pathsKeys: []
        };
    },
    beforeMount() {
        // build a request to the API
        axios.get("http://localhost:3000/api/spec/").then(
            (response) => {
                if (response.status === 200) {
                    this.html = response.data;
                    // this.info = response.data.info;
                    // this.pathsKeys = Object.keys(response.data.paths);
                    // this.paths = response.data.paths;
                }
            }
        ).catch((error) => {
            console.log(error);
        });
    }
}
</script>

<style scoped>
</style>