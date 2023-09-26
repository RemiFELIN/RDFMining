<template>
    <!-- <div class="container"> -->
    <CAccordion class="customizedAccordion" :active-item-key="1" always-open>
        <CAccordionItem :item-key="1">
            <CAccordionHeader>My projects ({{ countProject }})</CAccordionHeader>
            <CAccordionBody>
                <TabProjects v-if="showTabProjects" :projects="projects" @delete="deletePopup" @details="detailsPopup"></TabProjects>
                <!-- <b>YOYO</b> -->
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="2">
            <CAccordionHeader>I would like to start a new project !</CAccordionHeader>
            <CAccordionBody>
                <CreateProject @new="successPopup" :projects="projects"></CreateProject>
            </CAccordionBody>
        </CAccordionItem>
    </CAccordion>
    <!-- </div> -->
    <!-- Delete Popup -->
    <DeletePopup :enable="showDeletePopup" :projectName="selectedProject" @deleted="deleteProject"
        @close="deletePopup">
    </DeletePopup>
    <!-- Details Popup -->
    <DetailsPopup :enable="showDetailsPopup" :data="projectJSON"></DetailsPopup>
    <!-- Success creation popup -->
    <SuccessPopup :enable="showSuccessPopup" :projectName="selectedProject"></SuccessPopup>
</template>


<script>
import CreateProject from './CreateProject.vue';
import TabProjects from './TabProjects.vue';
import DeletePopup from './popup/DeletePopup.vue';
import DetailsPopup from './popup/DetailsPopup.vue';
import SuccessPopup from './popup/SuccessPopup.vue';
import { CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody} from '@coreui/vue';
import { get } from '@/tools/api';

export default {
    name: 'MyProjects',
    components: {
        DeletePopup, DetailsPopup, SuccessPopup, CreateProject, TabProjects,
        CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody
    },
    // props: {
    //     token: {
    //         type: String,
    //     },
    // },
    data() {
        return {
            projects: [],
            countProject: 0,
            showTabProjects: true,
            showDeletePopup: false,
            showDetailsPopup: false,
            showSuccessPopup: false,
            projectJSON: {}
        }
    },
    methods: {
        deleteProject() {
            // delete the selected project
            this.projects = this.projects.filter((project) => {
                // keep all the others
                return project.projectName != this.selectedProject;
            });
            this.selectedProject = "";
            this.countProject--;
        },
        deletePopup(projectName) {
            this.selectedProject = projectName;
            this.showDeletePopup = !this.showDeletePopup;
        },
        detailsPopup(project) {
            // console.log(project)
            this.projectJSON = project;
            this.showDetailsPopup = !this.showDetailsPopup;
        },
        successPopup(projectName) {
            this.getProjects();
            this.selectedProject = projectName;
            this.showSuccessPopup = !this.showSuccessPopup;
        },
        async getProjects() {
            this.projects = await get("api/projects/", {});
            this.countProject = this.projects.length;
        }
    },
    mounted() {
        this.getProjects();
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.customizedAccordion {
    --cui-accordion-btn-color: rgb(14, 14, 163);
}
</style>