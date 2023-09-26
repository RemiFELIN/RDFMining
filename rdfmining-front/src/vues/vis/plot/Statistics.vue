<template>
    <CRow>
        <CCol sm="4">
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center">Individuals with <b>non-null fitness</b></CCardTitle>
                <CCardBody>
                    <CChart type="bar" :wrapper="true" :data="ind_non_null_chart" :options="options" :key="refresh"
                        :redraw="true">
                    </CChart>
                </CCardBody>
            </CCard>
        </CCol>
        <CCol sm="4">
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center"><b>Fitness</b> individuals</CCardTitle>
                <CCardBody>
                    <CChart type="line" :wrapper="true" :data="ind_fitness_chart" :options="options" :key="refresh"
                        :redraw="true">
                    </CChart>
                </CCardBody>
            </CCard>
        </CCol>
        <CCol sm="4">
            <CCard class="card">
                <!-- <CCardImage width="10" height="20" orientation="top" src="../assets/fitness.png"></CCardImage> -->
                <CCardTitle class="text-center"><b>Population</b> evolution</CCardTitle>
                <CCardBody>
                    <CChart type="line" :wrapper="true" :data="pop_evol_chart" :options="options_rate" :key="refresh"
                        :redraw="true"></CChart>
                </CCardBody>
            </CCard>
        </CCol>
    </CRow>
</template>

<script>
// https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#overview
// import { GChart } from 'vue-google-charts'
// import { entities } from '../data/results_1.json'
import { CChart } from '@coreui/vue-chartjs'
import { CCard, CCardBody, CCardTitle, CRow, CCol } from '@coreui/vue';
import { toRaw } from 'vue';
// import io from "socket.io-client";
import { socket } from '@/tools/socket';

export default {
    name: 'VueStatistics',
    components: {
        CChart, CCard, CCardBody, CCardTitle, CRow, CCol
    },
    props: {
        results: {
            type: Object
        }
    },
    data() {
        return {
            // force refresh of component
            refresh: true,
            // socket io
            socket: socket,
            // generations
            nGenerations: 0,
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
                            return context.formattedValue;
                        },
                    }
                }
            },
            plugins_rate: {
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
                            return (context.formattedValue * 100) + "%";
                        },
                    }
                }
            },
            // chart options
            options: {},
            options_rate: {},
            // CoreUI CCharts: Individuals with non-null fitness
            ind_non_null_data: [],
            ind_non_null_chart: {},
            // CoreUI CCharts: Individuals fitness
            ind_fitness_data: [],
            ind_fitness_chart: {},
            // CoreUI CCharts: Population evolution
            pop_dvp_rate_data: [],
            div_coeff_data: [],
            pop_evol_chart: {},
            // CoreUI CCharts: Bubble chart
            // bubble_chart: {},
            // ELAPSED TIME / REFERENCE CARDINALITY (EXCEPTIONS ?)
            // entities_avalaible: false,
            // entities_data: [],
            // bubble_options: {
            //     // title: "Overview of entities found",
            //     titleTextStyle: { fontSize: 30 },
            //     backgroundColor: "#ffffff",
            //     hAxis: {
            //         title: "# exceptions",
            //         textStyle: { fontSize: 30 },
            //         titleTextStyle: { fontSize: 20, italic: false, bold: true }
            //     },
            //     vAxis: {
            //         title: "CPU computation time (ms.)",
            //         textStyle: { fontSize: 30 },
            //         titleTextStyle: { fontSize: 20, italic: false, bold: true }
            //     },
            //     colorAxis: {
            //         minValue: 0,  
            //         colors: ['#00FF00', '#FF0000']
            //     },
            //     bubble: { textStyle: { auraColor: 'none', fontSize: 1 } },
            //     sizeAxis: { minSize: 20, maxSize: 100 },
            //     explorer: {},
            //     // colorAxis: { colors: ['#15d600', '#ff0000'] },
            //     width: 1500,
            //     height: 750,
            // },
        };
    },
    mounted() {
        // get number of generations
        this.nGenerations = toRaw(this.results.statistics.nGenerations);
        // deduce x-labels
        this.gen_labels = Array.from({ length: this.nGenerations }, (_, idx) => idx + 1);
        //
        this.ind_non_null_data = Array(this.nGenerations).fill(0);
        this.ind_fitness_data = Array(this.nGenerations).fill(0);
        this.pop_dvp_rate_data = Array(this.nGenerations).fill(0);
        this.div_coeff_data = Array(this.nGenerations).fill(0);
        // verify if any results (or all ?) are already defined
        if (toRaw(this.results.statistics.generations.length) != 0) {
            for (let i = 0; i < toRaw(this.results.statistics.generations.length); i++) {
                // console.log(toRaw(this.results.statistics.generations[i]));
                this.ind_non_null_data[i] = toRaw(this.results.statistics.generations[i].numIndividualsWithNonNullFitness);
                this.ind_fitness_data[i] = toRaw(this.results.statistics.generations[i].averageFitness);
                this.pop_dvp_rate_data[i] = toRaw(this.results.statistics.generations[i].populationDevelopmentRate);
                this.div_coeff_data[i] = toRaw(this.results.statistics.generations[i].diversityCoefficient);
            }
        }
        // Individuals with non-null fitness chart
        this.ind_non_null_chart = {
            labels: this.gen_labels,
            datasets: [
                {
                    label: 'Individuals with non-null fitness',
                    backgroundColor: 'rgba(222, 0, 0, 0.8)',
                    // borderColor: 'rgba(220, 220, 220, 1)',
                    // pointBackgroundColor: 'rgba(220, 220, 220, 1)',
                    // pointBorderColor: '#fff',
                    data: this.ind_non_null_data
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
                    beginAtZero: false,
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
        };
        this.options_rate = {
            // maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true
                },
                x: {
                    beginAtZero: false,
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
            plugins: this.plugins_rate
        };
        // Individuals fitness chart
        this.ind_fitness_chart = {
            labels: this.gen_labels,
            datasets: [
                {
                    label: 'Individuals fitness evolution',
                    backgroundColor: 'rgba(0, 255, 3, 0.8)',
                    borderColor: 'rgba(0, 178, 2, 0.8)',
                    pointBackgroundColor: 'rgba(0, 255, 3, 0.8)',
                    pointBorderColor: 'rgba(0, 148, 2, 0.8)',
                    data: this.ind_fitness_data
                }
            ]
        };
        // Pop evol chart
        this.pop_evol_chart = {
            labels: this.gen_labels,
            datasets: [
                {
                    label: 'Population development rate',
                    backgroundColor: 'rgba(0, 119, 216, 0.8)',
                    borderColor: 'rgba(0, 119, 216, 0.8)',
                    pointBackgroundColor: 'rgba(0, 146, 255, 0.8)',
                    pointBorderColor: 'rgba(78, 176, 255, 0.8)',
                    data: this.pop_dvp_rate_data
                },
                {
                    label: 'Diversity coefficient',
                    backgroundColor: 'rgba(255, 173, 0, 0.8)',
                    borderColor: 'rgba(255, 173, 0, 0.8)',
                    pointBackgroundColor: 'rgba(255, 211, 118, 0.8)',
                    pointBorderColor: 'rgba(202, 137, 0, 0.8)',
                    data: this.div_coeff_data
                }
            ]
        };
        //
        // if (toRaw(this.results.entities.length) != 0) {
        //     // entities data
        //     this.entities_data.push([
        //         "phenotype",
        //         "numExceptions",
        //         "elapsedTime",
        //         "Violations Ratio",
        //         "referenceCardinality"
        //     ]);
        //     // iterate on entities found
        //     for (let i = 0; i < toRaw(this.results.entities.length); i++) {
        //         // console.log(entity.numExceptions / entity.referenceCardinality)
        //         this.entities_data.push([
        //             toRaw(this.results.entities[i].phenotype),
        //             toRaw(this.results.entities[i].numExceptions),
        //             toRaw(this.results.entities[i].elapsedTime),
        //             toRaw(this.results.entities[i].numExceptions) / toRaw(this.results.entities[i].referenceCardinality),
        //             toRaw(this.results.entities[i].referenceCardinality)
        //         ]);
        //     }
        //     this.entities_avalaible = true;
        // }
        // SOCKET IO
        this.socket.on("update-generation", (data) => {
            // console.log("socket.io updates generations ... with " + JSON.stringify(data));
            // update each data arrays
            this.ind_non_null_chart.datasets[0].data[data.generation - 1] = data.numIndividualsWithNonNullFitness;
            this.ind_fitness_chart.datasets[0].data[data.generation - 1] = data.averageFitness;
            this.pop_evol_chart.datasets[0].data[data.generation - 1] = data.populationDevelopmentRate;
            this.pop_evol_chart.datasets[1].data[data.generation - 1] = data.diversityCoefficient;
            //
            console.log(this.ind_fitness_chart.datasets[0].data);
            // refresh
            this.refresh = !this.refresh;
        });
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