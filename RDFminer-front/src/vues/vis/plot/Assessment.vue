<template>
    <CRow>
        <CCol sm="4">
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center">Entities elapsed time (in sec.)</CCardTitle>
                <CCardBody>
                    <CChart type="bar" :wrapper="false" :data="getChart()" :key="refresh" :options="options" :redraw="true">
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
                    <CButton color="info" variant="outline" :disabled="progression != 100" @click="getSHACLReport">Download
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
import { CCard, CCardBody, CCardTitle, CRow, CCol, CProgress, CProgressBar, CButton } from '@coreui/vue';
import { toRaw } from 'vue';
import DetailsPopup from '../../projects/popup/DetailsPopup.vue';
// import io from "socket.io-client";
import { get } from '@/tools/api';
import { options } from "../settings/assessment";
import { socket } from '@/tools/env';

export default {
    name: 'VisAssessment',
    components: {
        CChart, CCard, CCardBody, CCardTitle, CRow, CCol, CProgress, CProgressBar, CButton, DetailsPopup
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
            project: {},
            showDetailsPopup: false,
            // socket io
            socket: socket,
            // entities
            options: options,
            nEntities: 0,
            // current generation
            curEntities: 1,
            progression: 0,
            // n_gen labels
            entitiesLabels: [],
            // options plugin
            // CoreUI CCharts: Individuals with non-null fitness
            elapsedTimeData: [],
            computationTimeChart: {},
        };
    },
    mounted() {
        if (this.task == "Assessment") {
            this.getProject();
            // get number of entities
            this.nEntities = toRaw(this.results.nEntities);
            if (toRaw(this.results.entities.length) != 0) {
                //
                this.curEntities = toRaw(this.results.entities.length);
                this.progression = Math.round((this.curEntities / this.nEntities) * 100);
                //
                for (let i = 0; i < toRaw(this.results.entities.length); i++) {
                    this.entitiesLabels.push(toRaw(this.results.entities[i].phenotype));
                    this.elapsedTimeData[i] = toRaw(this.results.entities[i].elapsedTime);
                }
            }
            // console.log(toRaw(this.elapsedTimeData));
            // Individuals with non-null fitness chart
            this.computationTimeChart = {
                labels: toRaw(this.entitiesLabels),
                datasets: [
                    {
                        label: 'Elapsed time',
                        backgroundColor: 'rgba(222, 0, 0, 0.8)',
                        data: toRaw(this.elapsedTimeData)
                    }
                ],
            };
            // SOCKET IO
            this.socket.on("update-entities", (data) => {
                // console.log("socket.io updates generations ... with " + JSON.stringify(data));
                // update each data arrays
                this.computationTimeChart.labels.push(data.phenotype);
                this.computationTimeChart.datasets[0].data.push(data.elapsedTime);
                //
                this.curEntities += 1;
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
        getChart() {
            return toRaw(this.computationTimeChart);
        },
        async getResults() {
            const result = await get("api/results", { path: this.path, file: "results" });
            this.download(result, "results.json");
        },
        async getSHACLReport() {
            const result = await get("api/results", { path: this.path, file: "shacl" });
            this.download(result, "shacl_report.ttl");
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
        curEntities() {
            this.progression = Math.round((this.curEntities / this.nEntities) * 100);
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