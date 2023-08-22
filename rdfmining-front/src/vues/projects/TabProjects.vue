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
                <CTableDataCell>{{ status[project.status].text }}</CTableDataCell>
                <CTableDataCell>
                    <CAvatar class="clickable" src="assets/cancel.png" v-if="project.status != 2" />
                    <CAvatar class="clickable" src="assets/dashboard.png" @click="redirectVisu(project.projectName)"
                        v-if="project.status != 0" />
                    <CAvatar class="clickable" src="assets/garbage.png" @click="deletePopup(project.projectName)" />
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
import { CTable, CTableHead, CTableBody, CTableFoot, CTableRow, CTableHeaderCell, CTableDataCell, CAvatar } from '@coreui/vue';

export default {
    name: 'TabProjects',
    components: {
        CTable, CTableHead, CTableBody, CTableFoot, CTableRow, CTableHeaderCell, CTableDataCell, CAvatar
        // Vue3EasyDataTable,
        // Popup
    },
    props: {
        id: {
            type: String,
        },
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
            console.log(this.id, " and ", p)
            // Is any results related to this project ?
            axios.get("http://localhost:9200/api/results",  { params: { userId: this.id, projectName: p } }).then(
                (response) => {
                    console.log(response.data);
                    if (response.status === 200) {
                        // redirect on visualisation route with the results ID linked to the project
                        this.$router.push({ name: "VueVisualisation", params: { resultsId: response.data } });
                    } 
                }
            ).catch((error) => {
                console.log(error);
            });
        }
    },
    data() {
        return {
            socket: io("http://localhost:9200"),
            status: {
                0: { text: "Pending...", color: "red" },
                1: { text: "In progress", color: "orange" },
                2: { text: "Complete", color: "green" }
            },
            showDeletePopup: false,
            selectedProject: "",
            headers: ["Project name", "Task", "Status", "Operations"],
            item: null,
        };
    },
    mounted() {
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
.clickable {
    cursor: pointer;
    padding: 1%;
}

.clickable:hover {
    opacity: 0.5;
}
</style>
