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
    
    <!-- <div class="containter">
        <Vue3EasyDataTable :headers="headers" :items="projects" :rows-per-page="5" table-class-name="customize-table"
            theme-color="#1d90ff" alternating buttons-pagination>

            <template #expand="item">
                <div>
                    Parameters used:
                    <ul>
                        <li><b>Population size:</b> {{ item.params.populationSize }}</li>
                        <li><b>Total effort:</b> {{ item.params.kBase }}</li>
                        <li><b>Chromosome size:</b> {{ item.params.lenChromosome }}</li>
                        <li><b>Max wrapp:</b> {{ item.params.maxWrapp }}</li>
                        <li><b>Selection type:</b> {{ typeSelection[item.params.typeSelection].text }}</li>
                        <li><b>Crossover type:</b> {{ typeCrossover[item.params.typeCrossover].text }}</li>
                        <li><b>Mutation type:</b> {{ typeMutation[item.params.typeMutation].text }}</li>
                        <li><b>Crowding method:</b> {{ item.params.crowding ? "YES" : "NO" }}</li>
                        <li><b>Novelty Search:</b> {{ item.params.noveltySearch ? "YES" : "NO" }}</li>
                    </ul>
                </div>
            </template>
            <template #item-projectName="item">
                <b>{{ item.projectName }}</b>
            </template>
            <template #item-mod="item">
                <p>{{ item.mod }}</p>
            </template>
            <template #item-status="item">
                <b :style="{ color: status[item.status].color }">{{ status[item.status].text }}</b>
            </template>
            <template #item-operation="item">
                <div class="operation-wrapper">
                    <img v-if="item.status != 2" src="../../assets/cancel.png" class="button-action" @click="cancelExecution(item)" />
                    <img v-if="item.status != 0" src="../../assets/dashboard.png" class="button-action" @click="cancelExecution(item)" />
                    <img src="../../assets/garbage.png" class="button-action" @click="enableDeletePopup(item)" />
                </div>
            </template>
        </Vue3EasyDataTable>
        <Popup v-if="showDeletePopup" :message="'Are you sure you want to delete this project? (this is not reversible)'"
            @confirm="deleteProject(this.item)" @cancel="showDeletePopup = false">
        </Popup>
    </div> -->
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
        projects: {}
    },
    methods: {
        deletePopup(projectName) {
            this.$emit("delete", projectName);
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
        // this.socket.on("newProject", (data) => {
        //     this.projects.push(data);
        // });
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
