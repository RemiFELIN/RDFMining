<template>
    <div class="container">
        <!-- List of projects -->
        <div class="widget" v-if="projects.length != 0">
            <h1>My projects</h1>
            <!-- <h2>{{ id }}</h2> -->
            <p v-for="project in projects" :key="project">{{ project.projectName }}</p>
        </div>
        <div class="widget" v-else>
            <h1>No project initiated</h1>
        </div>
        <!-- Init project -->
        <div class="widget" v-if="projects.length != 0">
            <h1>I would like to start a new project !</h1>
            <!-- <h2>{{ id }}</h2> -->
            <CreateProject :id="id" :show="isVisibleForm"></CreateProject>
            <button v-if="!isVisibleForm" @click="toggleForm">Let's go</button>
            <button v-if="isVisibleForm" @click="toggleForm" class="disconnect">Cancel</button>
        </div>
    </div>
</template>


<script>
// import { publications } from '../data/publications.json'
// import _ from 'lodash';
import axios from 'axios';
import CreateProject from './CreateProject.vue';
import io from "socket.io-client";

export default {
    name: 'MyProjects',
    components: {
        CreateProject,
    },
    props: {
        id: {
            type: String,
        }
    },
    data() {
        return {
            projects: [],
            isVisibleForm: false,
            socket: io("http://localhost:3000"),
            // papers: [],
            // keywords: [],
            // choosenFilter: "",
        }
    },
    mounted() {
        // build a request to the API
        axios.get("http://localhost:3000/api/projects/", { params: { id: this.id } }).then(
            (response) => {
                if (response.status === 200) {
                    // fill papers list
                    response.data.forEach((project) => {
                        this.projects.push(project);
                    })
                }
            }
        ).catch((error) => {
            console.log(error);
        });
    },
    methods: {
        toggleForm() {
            this.isVisibleForm = !this.isVisibleForm;
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>

</style>