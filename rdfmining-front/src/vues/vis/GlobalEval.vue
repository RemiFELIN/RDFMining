<template>
    <CRow :xs="{ cols: 1, gutter: 3 }" :md="{ cols: 2 }">
        <CCol xs>
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center">Entities elapsed time (in sec.)</CCardTitle>
                <CCardBody>
                    <CChart type="bar" :wrapper="false" :data="getChart()" :key="refresh" :options="options" :redraw="true">
                    </CChart>
                </CCardBody>
            </CCard>
        </CCol>
        <CCol xs>
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardBody>
                    <CCardTitle class="text-center"><b>Progression</b></CCardTitle>
                    <CProgress class="mb-3">
                        <CProgressBar :value="progression">{{ progression }}%</CProgressBar>
                    </CProgress>
                    <CCardTitle class="text-center"><b>Actions</b></CCardTitle>
                    <CButton style="margin: 5px;" color="success" variant="outline" :disabled="progression != 100"
                        @click="getResults">Download
                        results (JSON)</CButton>
                    <CButton color="info" variant="outline" :disabled="progression != 100" @click="getSHACLReport">Download
                        SHACL report (Turtle)</CButton>
                </CCardBody>
            </CCard>
        </CCol>
    </CRow>
</template>

<script>
// https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#overview
// import { entities } from '../data/results_1.json'
import { CChart } from '@coreui/vue-chartjs';
import { CCard, CCardBody, CCardTitle, CRow, CCol, CProgress, CProgressBar, CButton } from '@coreui/vue';
import { toRaw } from 'vue';
import { useCookies } from "vue3-cookies";
import axios from 'axios';
import io from "socket.io-client";

export default {
    name: 'VueGlobalEval',
    components: {
        CChart, CCard, CCardBody, CCardTitle, CRow, CCol, CProgress, CProgressBar, CButton
    },
    props: {
        results: {
            type: Object
        },
        path: {
            type: String
        }
    },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            // force refresh of component
            refresh: true,
            // socket io
            socket: io("http://localhost:9200"),
            // entities
            options: {},
            nEntities: 0,
            // current generation
            curEntities: 1,
            progression: 0,
            // n_gen labels
            entities_labels: [],
            // options plugin
            plugins: {
                legend: {
                    display: true,
                    labels: {
                        font: {
                            size: 16,
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        title: function (context) {
                            return "Phenotype: " + context[0].label;
                        },
                        label: function (context) {
                            // return context.dataset.label + " value: " + context.formattedValue;
                            return context.formattedValue + " sec.";
                        },
                    }
                }
            },
            // CoreUI CCharts: Individuals with non-null fitness
            elapsed_time_data: [],
            computation_time_chart: {},
        };
    },
    mounted() {
        // get number of entities
        this.nEntities = toRaw(this.results.nEntities);
        // deduce x-labels
        // this.entities_labels = Array.from({ length: this.nEntities }, (_, idx) => idx + 1);
        //
        // this.elapsed_time_data = Array(this.nEntities).fill(0);
        // verify if any results (or all ?) are already defined

        if (toRaw(this.results.entities.length) != 0) {
            //
            this.curEntities = toRaw(this.results.entities.length);
            this.progression = (this.curEntities / this.nEntities) * 100;
            //
            for (let i = 0; i < toRaw(this.results.entities.length); i++) {
                this.entities_labels.push(toRaw(this.results.entities[i].phenotype));
                this.elapsed_time_data[i] = toRaw(this.results.entities[i].elapsedTime);
            }
        }
        console.log(toRaw(this.elapsed_time_data));
        // Individuals with non-null fitness chart
        this.computation_time_chart = {
            labels: toRaw(this.entities_labels),
            datasets: [
                {
                    label: 'Elapsed time',
                    backgroundColor: 'rgba(222, 0, 0, 0.8)',
                    // borderColor: 'rgba(220, 220, 220, 1)',
                    // pointBackgroundColor: 'rgba(220, 220, 220, 1)',
                    // pointBorderColor: '#fff',
                    data: toRaw(this.elapsed_time_data)
                }
            ],
        };
        this.options = {
            // maintainAspectRatio: false,
            scales: {
                // y: {
                //     beginAtZero: true
                // },
                x: {
                    // beginAtZero: true,
                    // Empirically set
                    display: this.nEntities > 20 ? false : true,
                    // type: 'linear',
                    // title: {
                    //     display: true,
                    //     text: 'Entities',
                    //     font: {
                    //         size: 18
                    //     }
                    // },
                }
            },
            plugins: this.plugins
        }
        // SOCKET IO
        this.socket.on("update-entities", (data) => {
            // console.log("socket.io updates generations ... with " + JSON.stringify(data));
            // update each data arrays
            this.computation_time_chart.labels.push(data.phenotype);
            this.computation_time_chart.datasets[0].data.push(data.elapsedTime);
            //
            this.curEntities += 1;
            //
            console.log(this.computation_time_chart.datasets[0].data);
            // refresh
            this.refresh = !this.refresh;
        });
    },
    methods: {
        getChart() {
            return toRaw(this.computation_time_chart);
        },
        getResults() {
            // get logs
            axios.get("http://localhost:9200/api/results", {
                params: { path: this.path, file: "results" },
                headers: { "x-access-token": this.cookies.get("token") }
            }).then(
                (response) => {
                    this.download(response.data, "results.json");
                }
            ).catch((error) => {
                console.log(error);
            });
        },
        getSHACLReport() {
            // get logs
            axios.get("http://localhost:9200/api/results", {
                params: { path: this.path, file: "shacl" },
                headers: { "x-access-token": this.cookies.get("token") }
            }).then(
                (response) => {
                    this.download(response.data, "shacl_report.ttl");
                }
            ).catch((error) => {
                console.log(error);
            });
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
            this.progression = (this.curEntities / this.nEntities) * 100;
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>