<template>
    <CTable striped hover>
        <CTableHead color="light">
            <CTableRow>
                <CTableHeaderCell v-for="header in headers" :key="header" scope="col">{{ header }}</CTableHeaderCell>
            </CTableRow>
        </CTableHead>
        <CTableBody>
            <CTableRow v-for="project in projects" :key="project">
                <CTableHeaderCell scope="row">{{ project.projectName }}</CTableHeaderCell>
                <CTableDataCell>{{ project.task }}</CTableDataCell>
                <CTableDataCell><a :href="project.targetSparqlEndpoint">{{ project.targetSparqlEndpoint }}</a>
                </CTableDataCell>
                <CTableDataCell>
                    <!-- <CAvatar class="clickable" src="assets/dashboard.png" @click="redirectVisu(project.projectName)"
                        v-if="project.status != 0" /> -->
                    <CButton v-if="project.status != 0" @click="redirectVisu(project.projectName)" color="info"
                        variant="outline">
                        Dashboard
                    </CButton>
                    <CButton disabled v-else>
                        <CSpinner variant="grow" size="sm" aria-hidden="true" />
                        Waiting for the server
                    </CButton>
                </CTableDataCell>
                <CTableDataCell>
                    <!-- <CButton color="success" variant="outline" :disabled="project.status != 0" style="margin:5px;">
                        <CImage src="assets/start.png" width="20" height="20" />
                        Start
                    </CButton> -->
                    <CButton color="danger" variant="outline" :disabled="project.status != 1" style="margin:5px;">
                        <CImage src="assets/cancel.png" width="20" height="20" />
                        Stop
                    </CButton>
                    <CButton color="danger" style="margin:5px;" @click="deletePopup(project.projectName)">
                        <CImage src="assets/garbage.png" width="20" height="20" />
                        Delete
                    </CButton>
                    <!-- <CAvatar class="clickable" src="assets/cancel.png" />
                    <CAvatar class="clickable" src="assets/dashboard.png" @click="redirectVisu(project.projectName)"
                        v-if="project.status != 0" />
                    <CAvatar class="clickable" src="assets/garbage.png" @click="deletePopup(project.projectName)" /> -->
                </CTableDataCell>
                <CTableDataCell :color="getColor(project.status)" style="font-weight: bold;">
                    <CSpinner v-if="project.status != 2" size="sm" style="margin-right:10px;"/>{{ status[project.status].text }}
                </CTableDataCell>
            </CTableRow>
        </CTableBody>
        <CTableFoot>
        </CTableFoot>
    </CTable>
</template>


<script>
import io from "socket.io-client";
import axios from "axios";
import { CTable, CTableHead, CTableBody, CTableFoot, CTableRow, CTableHeaderCell, CTableDataCell, CSpinner, CButton, CImage } from '@coreui/vue';
import { useCookies } from 'vue3-cookies'

export default {
    name: 'TabProjects',
    components: {
        CTable, CTableHead, CTableBody, CTableFoot, CTableRow,
        CTableHeaderCell, CTableDataCell, CSpinner, CButton,
        CImage
    },
    props: {
        projects: Array
    },
    methods: {
        deletePopup(projectName) {
            this.$emit("delete", projectName);
        },
        updateStatus(project, status) {
            project.status = status;
        },
        redirectVisu(p) {
            axios.get("http://localhost:9200/api/results", {
                params: { projectName: p },
                headers: { "x-access-token": this.cookies.get("token") }
            }).then(
                (response) => {
                    if (response.status === 200) {
                        console.log(response.data);
                        // redirect on visualisation route with the results ID linked to the project
                        this.$router.push({ name: "VueVisualisation", params: { resultsId: response.data } });
                    }
                }
            ).catch((error) => {
                console.log(error);
            });
        },
        getColor(status) {
            switch (status) {
                default:
                case 0:
                    return "danger";
                case 1:
                    return "warning";
                case 2:
                    return "success";
            }
        },
        /**
         * return the type of entities from the mining/evaluating task
         * @param {String} type 
         */
        getType(type) {
            if (type.includes("-rs") || type.includes("-sf")) {
                return "SHACL Shapes";
            } else if (type.includes("-ra") || type.includes("-af")) {
                return "OWL Axioms";
            } else {
                return "Unknown";
            }
        }
    },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            socket: io("http://localhost:9200"),
            status: {
                0: { text: "Pending...", color: "red" },
                1: { text: "In progress", color: "orange" },
                2: { text: "Complete", color: "green" }
            },
            showDeletePopup: false,
            selectedProject: "",
            headers: ["Project name", "Task", "SPARQL endpoint", "Results", "Operations", "Status"],
            item: null,
        };
    },
    mounted() {
        this.cookies = useCookies(["token", "id"]).cookies;
        // console.log("Token: " + this.cookies.get("token"));
        // SOCKET IO
        // update project status
        this.socket.on("update-status", (data) => {
            console.log(data);
            this.projects.forEach((p) => {
                if (p.projectName == data.projectName) {
                    this.updateStatus(p, data.status);
                }
            });
        });
        // update resultId
        this.socket.on("results-created", () => {
            console.log("results created !");
        });
    }
}
</script>

<style scoped>
p {
  display: inline-block;
}
.clickable {
    cursor: pointer;
    padding: 1%;
}

.clickable:hover {
    opacity: 0.2;
}</style>
