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
import { socket } from '@/tools/env';

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
            // nGenerations: 0,
            // n_gen labels
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
            ind_non_null_chart: {},
            // CoreUI CCharts: Individuals fitness
            ind_fitness_chart: {},
            // CoreUI CCharts: Population evolution
            pop_evol_chart: {},
        };
    },
    mounted() {
        // Individuals with non-null fitness chart
        this.ind_non_null_chart = {
            labels: [],
            datasets: [
                {
                    label: 'Individuals with non-null fitness',
                    backgroundColor: 'rgba(222, 0, 0, 0.8)',
                    data: []
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
            labels: [],
            datasets: [
                {
                    label: 'Individuals fitness evolution',
                    backgroundColor: 'rgba(0, 255, 3, 0.8)',
                    borderColor: 'rgba(0, 178, 2, 0.8)',
                    pointBackgroundColor: 'rgba(0, 255, 3, 0.8)',
                    pointBorderColor: 'rgba(0, 148, 2, 0.8)',
                    data: []
                }
            ],
        };
        // Pop evol chart
        this.pop_evol_chart = {
            labels: [],
            datasets: [
                {
                    label: 'Population development rate',
                    backgroundColor: 'rgba(0, 119, 216, 0.8)',
                    borderColor: 'rgba(0, 119, 216, 0.8)',
                    pointBackgroundColor: 'rgba(0, 146, 255, 0.8)',
                    pointBorderColor: 'rgba(78, 176, 255, 0.8)',
                    data: []
                },
                {
                    label: 'Diversity coefficient',
                    backgroundColor: 'rgba(255, 173, 0, 0.8)',
                    borderColor: 'rgba(255, 173, 0, 0.8)',
                    pointBackgroundColor: 'rgba(255, 211, 118, 0.8)',
                    pointBorderColor: 'rgba(202, 137, 0, 0.8)',
                    data: []
                }
            ]
        };
        // verify if any results (or all ?) are already defined
        if (toRaw(this.results.statistics.generations.length) != 0) {
            for (let i = 0; i < toRaw(this.results.statistics.generations.length); i++) {
                // console.log(this.results.statistics.generations[i].numIndividualsWithNonNullFitness)
                // console.log(toRaw(this.results.statistics.generations[i]));
                this.ind_non_null_chart.datasets[0].data.push(toRaw(this.results.statistics.generations[i].numIndividualsWithNonNullFitness));
                this.ind_fitness_chart.datasets[0].data.push(toRaw(this.results.statistics.generations[i].averageFitness));
                this.pop_evol_chart.datasets[0].data.push(toRaw(this.results.statistics.generations[i].populationDevelopmentRate));
                this.pop_evol_chart.datasets[1].data.push(toRaw(this.results.statistics.generations[i].diversityCoefficient));
                // update labels
                this.ind_non_null_chart.labels.push(this.ind_non_null_chart.datasets[0].data.length);
                this.ind_fitness_chart.labels.push(this.ind_non_null_chart.datasets[0].data.length);
                this.pop_evol_chart.labels.push(this.pop_evol_chart.datasets[0].data.length);
            }
        }
        // SOCKET IO
        this.socket.on("update-generation", (data) => {
            // update each data arrays
            this.ind_non_null_chart.datasets[0].data.push(data.numIndividualsWithNonNullFitness);
            this.ind_fitness_chart.datasets[0].data.push(data.averageFitness);
            this.pop_evol_chart.datasets[0].data.push(data.populationDevelopmentRate);
            this.pop_evol_chart.datasets[1].data.push(data.diversityCoefficient);
            // update labels
            this.ind_non_null_chart.labels.push(this.ind_non_null_chart.datasets[0].data.length);
            this.ind_fitness_chart.labels.push(this.ind_non_null_chart.datasets[0].data.length);
            this.pop_evol_chart.labels.push(this.pop_evol_chart.datasets[0].data.length);
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