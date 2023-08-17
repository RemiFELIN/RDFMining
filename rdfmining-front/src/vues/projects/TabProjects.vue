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
                    <CAvatar class="clickable" src="assets/dashboard.png" v-if="project.status != 0" />
                    <CAvatar class="clickable" src="assets/garbage.png" @click="deletePopup(project.projectName)" />
                </CTableDataCell>
            </CTableRow>
        </CTableBody>
        <CTableFoot>
            <!-- <CTableDataCell colSpan="3" align="middle">Previous</CTableDataCell>
            <CTableDataCell align="right">Next</CTableDataCell> -->
        </CTableFoot>
    </CTable>
</template>


<script>
// https://hc200ok.github.io/vue3-easy-data-table-doc
// import { ref } from "vue";
// import Vue3EasyDataTable from 'vue3-easy-data-table';
import io from "socket.io-client";
// import { entities } from '../data/results_1.json';
// import 'vue3-easy-data-table/dist/style.css';
// import { rdfminer } from '../../data/form.json'
// import Popup from '@/components/Popup.vue';
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
        }
    },
    data() {
        return {
            // projects: [],
            socket: io("http://localhost:9200"),
            // searchValue: ref("Yolo")
            status: {
                0: { text: "Pending...", color: "red" },
                1: { text: "In progress", color: "orange" },
                2: { text: "Complete", color: "green" }
            },
            // typeSelection: rdfminer.typeSelection,
            // typeCrossover: rdfminer.typeCrossover,
            // typeMutation: rdfminer.typeMutation,
            showDeletePopup: false,
            selectedProject: "",
            headers: ["Project name", "Task", "Status", "Operations"],
            item: null,
        };
    },
    mounted() {
        // SOCKET IO
        this.socket.on("update-status", (data) => {
            console.log(data);
            this.projects.forEach((p) => {
                if(p.projectName == data.projectName) {
                    this.updateStatus(p, data.status);
                }
            });
        });
        // Header
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
