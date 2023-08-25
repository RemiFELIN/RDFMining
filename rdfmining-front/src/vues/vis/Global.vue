<template>
    <CRow :xs="{ cols: 1, gutter: 3 }" :md="{ cols: 2 }">
        <CCol xs>
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center">Generations computation time (in sec.)</CCardTitle>
                <CCardBody>
                    <CChart type="bar" :wrapper="true" :data="computation_time_chart" :options="options" :key="refresh"
                        :redraw="true">
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
                    <CButton style="margin: 5px;" color="success" variant="outline" :disabled="progression != 100" @click="getResults">Download
                        results (JSON)</CButton>
                    <CButton color="info" variant="outline" :disabled="progression != 100" @click="getSHACLReport">Download SHACL report (Turtle)</CButton>
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
    name: 'VueGlobal',
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
            // generations
            nGenerations: 0,
            // current generation
            curGeneration: 1,
            progression: 0,
            // n_gen labels
            gen_labels: [],
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
                            return "Generation " + context[0].label;
                        },
                        label: function (context) {
                            // return context.dataset.label + " value: " + context.formattedValue;
                            return context.formattedValue + " sec.";
                        },
                    }
                }
            },
            // chart options
            options: {},
            // CoreUI CCharts: Individuals with non-null fitness
            computation_time_data: [],
            computation_time_chart: {},
        };
    },
    mounted() {
        // get number of generations
        this.nGenerations = toRaw(this.results.statistics.nGenerations);
        // deduce x-labels
        this.gen_labels = Array.from({ length: this.nGenerations }, (_, idx) => idx + 1);
        //
        this.computation_time_data = Array(this.nGenerations).fill(0);
        // verify if any results (or all ?) are already defined
        if (toRaw(this.results.statistics.generations.length) != 0) {
            //
            this.curGeneration = toRaw(this.results.statistics.generations.length);
            this.progression = (this.curGeneration / this.nGenerations) * 100;
            //
            for (let i = 0; i < toRaw(this.results.statistics.generations.length); i++) {
                // console.log(toRaw(this.results.statistics.generations[i]));
                this.computation_time_data[i] = toRaw(this.results.statistics.generations[i].computationTime);
            }
        }
        // Individuals with non-null fitness chart
        this.computation_time_chart = {
            labels: this.gen_labels,
            datasets: [
                {
                    label: 'Computation time',
                    backgroundColor: 'rgba(222, 0, 0, 0.8)',
                    // borderColor: 'rgba(220, 220, 220, 1)',
                    // pointBackgroundColor: 'rgba(220, 220, 220, 1)',
                    // pointBorderColor: '#fff',
                    data: this.computation_time_data
                }
            ],
        };
        this.options = {
            // maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                },
                x: {
                    beginAtZero: true,
                    type: 'linear',
                    title: {
                        display: true,
                        text: 'Generation',
                        font: {
                            size: 18
                        }
                    },
                }
            },
            plugins: this.plugins
        }
        // SOCKET IO
        this.socket.on("update-generation", (data) => {
            // console.log("socket.io updates generations ... with " + JSON.stringify(data));
            // update each data arrays
            this.computation_time_chart.datasets[0].data[data.generation] = data.computationTime;
            //
            this.curGeneration += 1;
            //
            console.log(this.computation_time_chart.datasets[0].data);
            // refresh
            this.refresh = !this.refresh;
        });
    },
    methods: {
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
        curGeneration() {
            this.progression = (this.curGeneration / this.nGenerations) * 100;
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped></style>