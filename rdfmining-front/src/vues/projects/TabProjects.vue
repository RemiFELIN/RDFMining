<template>
    <div class="containter">
        <!-- Specific search by keyword -->
        <!-- <span style="font-size: 2em;">search value: </span><input style="font-size: 2em;" type="text" v-model="searchValue"> -->
        <Vue3EasyDataTable :headers="headers" :items="projects" :rows-per-page="5" table-class-name="customize-table"
            theme-color="#1d90ff" alternating buttons-pagination>

            <!-- Expand to get informations about the selected project -->
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
            <!-- CUSTOMIZED Number of violations / confirmations -->
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
    </div>
</template>


<script>
// https://hc200ok.github.io/vue3-easy-data-table-doc
// import { ref } from "vue";
import Vue3EasyDataTable from 'vue3-easy-data-table';
import axios from 'axios';
import io from "socket.io-client";
// import { entities } from '../data/results_1.json';
import 'vue3-easy-data-table/dist/style.css';
import { rdfminer } from '../../data/form.json'
import Popup from '@/components/Popup.vue';

export default {
    name: 'TabProjects',
    components: {
        Vue3EasyDataTable,
        Popup
    },
    props: {
        id: {
            type: String,
        },
        projects: {}
    },
    methods: {
        enableDeletePopup(item) {
            this.showDeletePopup = true;
            this.item = item;
        },
        deleteProject(item) {
            console.log("delete");
            console.log(item);
            axios.post("http://localhost:3000/api/project/delete", {
                userId: this.id,
                projectName: item.projectName
            }).then(
                (response) => {
                    console.log("Delete project " + item.projectName + ": " + response);
                }
            ).catch((error) => {
                console.log(error);
                alert("Incorrect username/password");
            });
            // remove the current project 
            // this.projects.pop(item);
            this.showDeletePopup = false;
        },
        cancelExecution(i) {
            console.log("cancel " + i);
        }
    },
    data() {
        return {
            headers: [],
            // projects: [],
            socket: io("http://localhost:3000"),
            // searchValue: ref("Yolo")
            status: {
                0: { text: "Pending...", color: "red" },
                1: { text: "In progress", color: "orange" },
                2: { text: "Complete", color: "green" }
            },
            typeSelection: rdfminer.typeSelection,
            typeCrossover: rdfminer.typeCrossover,
            typeMutation: rdfminer.typeMutation,
            showDeletePopup: false,
            item: null,
        };
    },
    mounted() {
        // SOCKET IO
        // this.socket.on("newProject", (data) => {
        //     this.projects.push(data);
        // });
        // Header
        this.headers = [
            { text: "Project Name", value: "projectName", sortable: true },
            { text: "Mod", value: "mod", sortable: true },
            { text: "Status", value: "status", sortable: true },
            { text: "Operations", value: "operation" },
        ];
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.operation-wrapper {
    /* width: 40px; */
    align-items: center;
    /* cursor: pointer; */
}

.button-action {
    border: none;
    cursor: pointer;
    align-items: center;
    /* border: 2px solid black; */
    width: 40px;
    padding-right: 20px;
    /* border-radius: 10px; */
    /* appearance: none; */
    background-color: inherit;
}

.button-action:hover {
    opacity: 0.5;
}

.shapes {
    font-family: "Lucida Console", Courier, monospace;
    font-size: 1em;
}

.pending {
    /* font-size: 1.8em; */
    color: rgba(255, 0, 0, 0.503);
}

.in-progress {
    color: rgb(187, 255, 0);
}

.complete {
    color: #298900;
}

.customize-table {
    height: 650px;
    overflow-y: auto;
    /* z-index: 1; */
    --easy-table-border: 5px solid #cecece;
    --easy-table-row-border: 1px solid #445269;

    --easy-table-header-font-size: 2em;
    --easy-table-header-height: 50px;
    --easy-table-header-font-color: #003c9e;
    /* --easy-table-header-background-color: #003c9e; */

    --easy-table-header-item-padding: 10px 15px;

    --easy-table-body-even-row-font-color: #000000;
    --easy-table-body-even-row-background-color: #cdcdcd8d;

    --easy-table-body-row-font-color: #000000;
    --easy-table-body-row-background-color: #ffffff;
    --easy-table-body-row-height: 50px;
    --easy-table-body-row-font-size: 1.5em;

    --easy-table-body-row-hover-font-color: #2d3a4f;
    --easy-table-body-row-hover-background-color: #eee;

    --easy-table-body-item-padding: 10px 15px;

    /** Footer */
    --easy-table-footer-background-color: #ebebeb;
    --easy-table-footer-font-color: #003c9e;
    --easy-table-footer-font-size: 1.5em;
    --easy-table-footer-padding: 0px 10px;
    --easy-table-footer-height: 100px;

    /* --easy-table-rows-per-page-selector-width: 70px; */
    /* --easy-table-rows-per-page-selector-option-padding: 10px; */
    /* --easy-table-rows-per-page-selector-z-index: 1; */


    --easy-table-scrollbar-track-color: #dcdcdc;
    /* --easy-table-scrollbar-color: #09ff00; */
    --easy-table-scrollbar-thumb-color: #0077ff;
    ;
    /* --easy-table-scrollbar-corner-color: #ffee00; */

    /* --easy-table-loading-mask-background-color: #2d3a4f; */
}
</style>
