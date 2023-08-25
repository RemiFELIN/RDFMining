<template>
    <!-- <div class="container"> -->
    <CAccordion class="customizedAccordion" :active-item-key="1" always-open>
        <CAccordionItem :item-key="1">
            <CAccordionHeader>My projects ({{ countProject }})</CAccordionHeader>
            <CAccordionBody>
                <TabProjects v-if="showTabProjects" :projects="projects" @delete="deletePopup"></TabProjects>
                <!-- <b>YOYO</b> -->
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="2">
            <CAccordionHeader>I would like to start a new project !</CAccordionHeader>
            <CAccordionBody>
                <CreateProject @new="refresh"></CreateProject>
            </CAccordionBody>
        </CAccordionItem>
    </CAccordion>
    <!-- </div> -->
    <!-- Delete Popup -->
    <DeletePopup :enable="showDeletePopup" :projectName="selectedProject" @deleted="updateProjects"
        @close="deletePopup">
    </DeletePopup>
</template>


<script>
import CreateProject from './CreateProject.vue';
import TabProjects from './TabProjects.vue';
import axios from "axios"
import DeletePopup from './DeletePopup.vue';
import { CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody } from '@coreui/vue';
import { useCookies } from 'vue3-cookies'

export default {
    name: 'MyProjects',
    components: {
        DeletePopup,
        CreateProject,
        TabProjects,
        CAccordion,
        CAccordionItem,
        CAccordionHeader,
        CAccordionBody
    },
    // props: {
    //     token: {
    //         type: String,
    //     },
    // },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            projects: [],
            isVisibleForm: false,
            countProject: 0,
            showTabProjects: true,
            showDeletePopup: false,
        }
    },
    methods: {
        updateProjects(projectName) {
            this.countProject--;
            this.projects.forEach((project) => {
                if(project.projectName == projectName) {
                    this.projects.pop(project);
                    this.selectedProject = "";
                    return;
                }
            });
        },
        deletePopup(projectName) {
            this.showDeletePopup = !this.showDeletePopup;
            this.selectedProject = projectName;
        },
        toggleForm() {
            this.isVisibleForm = !this.isVisibleForm;
        },
        refresh() {
            this.countProject = 0;
            this.projects = [];
            axios.get("http://localhost:9200/api/projects/", { 
                headers: { "x-access-token": this.cookies.get("token") },
            }).then(
                (response) => {
                    if (response.status === 200) {
                        response.data.forEach((project) => {
                            this.projects.push(project);
                            this.countProject++;
                        });
                    }
                }
            ).catch((error) => {
                console.log(error);
            });
        }
    },
    mounted() {
        this.refresh();
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.customizedAccordion {
    --cui-accordion-btn-color: rgb(14, 14, 163);
}
</style>