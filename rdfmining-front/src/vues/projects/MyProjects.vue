<template>
    <!-- <div class="container"> -->
    <CAccordion class="customizedAccordion">
        <CAccordionItem :item-key="1">
            <CAccordionHeader>My projects ({{ countProject }})</CAccordionHeader>
            <CAccordionBody>
                <TabProjects v-if="showTabProjects" :id="id" :projects="projects" @delete="deletePopup"></TabProjects>
                <!-- <b>YOYO</b> -->
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="2">
            <CAccordionHeader>I would like to start a new project !</CAccordionHeader>
            <CAccordionBody>
                <CreateProject :id="id" @new="refresh"></CreateProject>
            </CAccordionBody>
        </CAccordionItem>
    </CAccordion>
    <!-- </div> -->
    <!-- Delete Popup -->
    <DeletePopup :enable="showDeletePopup" :id="id" :projectName="selectedProject" @deleted="updateProjects"
        @close="deletePopup">
    </DeletePopup>
</template>


<script>
import CreateProject from './CreateProject.vue';
import TabProjects from './TabProjects.vue';
import axios from "axios"
import DeletePopup from './DeletePopup.vue';
import { CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody } from '@coreui/vue';

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
    props: {
        id: {
            type: String,
        }
    },
    data() {
        return {
            projects: [],
            isVisibleForm: false,
            countProject: 0,
            showTabProjects: true,
            showDeletePopup: false,
            // papers: [],
            // keywords: [],
            // choosenFilter: "",
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
            console.log("myProject: " + projectName);
            this.selectedProject = projectName;
            console.log(this.showDeletePopup + " for " + this.selectedProject);
        },
        toggleForm() {
            this.isVisibleForm = !this.isVisibleForm;
        },
        refresh() {
            this.countProject = 0;
            this.projects = [];
            axios.get("http://localhost:9200/api/projects/", { params: { id: this.id } }).then(
                (response) => {
                    if (response.status === 200) {
                        // fill papers list
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
        // build a request to the API
        axios.get("http://localhost:9200/api/projects/", { params: { id: this.id } }).then(
            (response) => {
                if (response.status === 200) {
                    // fill papers list
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
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.customizedAccordion {
    --cui-accordion-btn-color: rgb(14, 14, 163);
}

.bottom {
    /* position: absolute; */
    width: 50%;
    /* bottom: 5%; */
    left: 0;
    right: 0;
    margin: 0 auto;
}

.top {
    /* position: absolute; */
    /* height: auto; */
    display: block;
    width: auto;
    margin: 0;
}
</style>