<template>
    <CRow>
        <CCol sm="4">
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center">Generations computation time (in ms.)</CCardTitle>
                <CCardBody>
                    <CChart type="bar" :wrapper="true" :data="computationTimeChart" :options="options" :key="refresh"
                        :redraw="true">
                    </CChart>
                </CCardBody>
            </CCard>
        </CCol>
        <CCol sm="8">
            <CCard class="card-progression">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardBody>
                    <CCardTitle class="text-center"><b>Progression</b></CCardTitle>
                    <CProgress class="mb-3" height="50">
                        <CProgressBar :value="progression" color="success" :variant="progression == 100 ? '' : 'striped'"
                            animated><b style="font-size: large;">{{ progression }}%</b></CProgressBar>
                    </CProgress>
                    <CCardTitle class="text-center"><b>Actions</b></CCardTitle>
                    <CButton color="primary" variant="outline" @click="showDetails">
                        Check project settings</CButton>
                    <CButton style="margin: 5px;" color="success" variant="outline" :disabled="progression != 100"
                        @click="getResults">Download
                        results (JSON)</CButton>
                    <CButton color="info" variant="outline" :disabled="progression != 100" @click="getSHACLReport">
                        Download
                        SHACL report (Turtle)</CButton>
                </CCardBody>
            </CCard>
        </CCol>
    </CRow>
    <!-- Details Popup -->
    <DetailsPopup :enable="showDetailsPopup" :data="project"></DetailsPopup>
</template>

<script>
// https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#overview
// import { entities } from '../data/results_1.json'
import { CChart } from '@coreui/vue-chartjs';
import { CCard, CCardBody, CCardTitle, CCol, CProgress, CProgressBar, CButton, CRow } from '@coreui/vue';
import { toRaw } from 'vue';
import { options } from '../settings/GE';
import { get } from '@/tools/api';
import DetailsPopup from '../../projects/popup/DetailsPopup.vue';
// import axios from 'axios';
// import io from "socket.io-client";
import { socket } from '@/tools/env';

export default {
    name: 'VisGrammaticalEvolution',
    components: {
        CChart, CCard, CCardBody, CCardTitle, CCol, CProgress, CProgressBar, CButton,
        CRow, DetailsPopup
    },
    props: {
        results: {
            type: Object
        },
        path: {
            type: String
        },
        task: {
            type: String
        }
    },
    data() {
        return {
            // force refresh of component
            refresh: true,
            showDetailsPopup: false,
            // socket io
            socket: socket,
            // generations
            nGenerations: 0,
            project: {},
            // current generation
            curGeneration: 0,
            progression: 0,
            // n_gen labels
            labels: [],
            // chart options
            options: options,
            // CoreUI CCharts: Individuals with non-null fitness
            computationTimeData: [],
            computationTimeChart: {},
        };
    },
    mounted() {
        if (this.task == "Mining") {
            this.getProject();
            // get number of generations
            this.nGenerations = toRaw(this.results.statistics.nGenerations);
            // deduce x-labels
            this.labels = Array.from({ length: this.nGenerations }, (_, idx) => idx + 1);
            // console.log(this.labels);
            //
            this.computationTimeData = Array(this.nGenerations).fill(0);
            // verify if any results (or all ?) are already defined
            if (toRaw(this.results.statistics.generations.length) != 0) {
                //
                this.curGeneration = toRaw(this.results.statistics.generations.length);
                this.progression = (this.curGeneration / this.nGenerations) * 100;
                //
                for (let i = 0; i < toRaw(this.results.statistics.generations.length); i++) {
                    // console.log(toRaw(this.results.statistics.generations[i]));
                    this.computationTimeData[i] = toRaw(this.results.statistics.generations[i].computationTime);
                }
            }
            // Individuals with non-null fitness chart
            this.computationTimeChart = {
                labels: this.labels,
                datasets: [
                    {
                        label: 'Computation time',
                        backgroundColor: 'rgba(36, 168, 178, 0.8)',
                        data: this.computationTimeData
                    }
                ],
            };
            // SOCKET IO
            this.socket.on("update-generation", (data) => {
                // console.log("socket.io updates generations ... with " + JSON.stringify(data));
                // update each data arrays
                this.computationTimeChart.datasets[0].data[data.generation - 1] = data.computationTime;
                //
                this.curGeneration += 1;
                //
                // console.log(this.computationTimeChart.datasets[0].data);
                // refresh
                this.refresh = !this.refresh;
            });
        }
    },
    methods: {
        async getProject() {
            // get project
            const project = await get("api/project", { projectName: this.results.projectName });
            // console.log(project);
            this.project = project[0];
        },
        showDetails() {
            this.showDetailsPopup = !this.showDetailsPopup;
        },
        async getResults() {
            // get logs
            const results = await get("api/results", { path: this.path, file: "results" });
            this.download(results, "results.json");
            // axios.get("api/results", {
            //     params: { path: this.path, file: "results" },
            //     headers: { "x-access-token": this.cookies.get("token") }
            // }).then(
            //     (response) => {
            //         this.download(response.data, "results.json");
            //     }
            // ).catch((error) => {
            //     console.log(error);
            // });
        },
        async getSHACLReport() {
            // get logs
            const results = await get("api/results", { path: this.path, file: "shacl" });
            this.download(results, "shacl_report.ttl");
            // axios.get("api/results", {
            //     params: { path: this.path, file: "shacl" },
            //     headers: { "x-access-token": this.cookies.get("token") }
            // }).then(
            //     (response) => {
            //         this.download(response.data, "shacl_report.ttl");
            //     }
            // ).catch((error) => {
            //     console.log(error);
            // });
        },
        download(data, name) {
            const blob = new Blob([JSON.stringify(data, null, "\t")], { type: "application/octet-stream" });
            // download link
            const url = URL.createObjectURL(blob);
            // create <a>
            const link = document.createElement("a");
            link.href = url;
            link.download = name;
            // trigger it
            document.body.appendChild(link);
            link.click();
            // Clean elements
            URL.revokeObjectURL(url);
            document.body.removeChild(link);
        }
    },
    watch: {
        curGeneration() {
            this.progression = (this.curGeneration / this.nGenerations) * 100;
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.card {
    height: 100%;
    width: 100%;
}
</style>