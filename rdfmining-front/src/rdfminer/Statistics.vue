<template>
<div class="container">
    <!-- #INDIVIDUALS WITH NON-NULL FITNESS -->
    <GChart type="AreaChart" 
        :data="individuals" 
        :options="individuals_options" 
        :resizeDebounce="500"
    />
    <!-- FITNESS VISUALISATION -->
    <GChart type="AreaChart" 
        :data="fitness" 
        :options="fitness_options" 
        :resizeDebounce="500"
    />
    <!-- POPULATION EVOLUTION VISUALISATION -->
    <GChart type="AreaChart" 
        :data="popEvol" 
        :options="popEvol_options" 
        :resizeDebounce="500"
    />
</div>
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.container {
    display: flex;
    justify-content: space-around;
}
</style>

<script>
// https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#overview
import { GChart } from 'vue-google-charts'
import { statistics } from '../data/results_1.json'

export default {
    name: 'VueStatistics',
    components: {
        GChart,
    },
    data() {
        return {
            // INDIVIDUALS NON NULL FITNESS
            individuals: [],
            individuals_options: {
                title: "#Individuals with non-null Fitness Score",
                titleTextStyle: { fontSize: 30 },
                backgroundColor: "#ffffff",
                hAxis: { 
                    title: "Generation", 
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true }
                },
                vAxis: { 
                    title: "# Individuals", 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true },
                    textStyle: { fontSize: 30 },
                },
                series: {
                    0: { lineWidth: 5 }
                },
                colors: [ "#0059FF" ],
                legend: {position: "top", textStyle: { color: "dark", fontSize: 25 } },
                width: 1000,
                height: 600,
            },
            // FITNESS
            fitness: [],
            fitness_options: {
                title: "Average Fitness Score Evolution Over Generations",
                titleTextStyle: { fontSize: 30 },
                backgroundColor: "#ffffff",
                hAxis: { 
                    title: "Generation", 
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true }
                },
                vAxis: { 
                    title: "Fitness Score", 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true },
                    textStyle: { fontSize: 30 },
                },
                series: {
                    0: { lineWidth: 5 }
                },
                colors: [ "#E56800" ],
                legend: {position: "top", textStyle: { color: "dark", fontSize: 25 } },
                width: 1000,
                height: 600,
            },
            // POPULATION EVOLUTION
            popEvol: [],
            popEvol_options: {
                title: "Population Evolution Over Generations",
                titleTextStyle: { fontSize: 30 },
                backgroundColor: "#ffffff",
                hAxis: { 
                    title: "Generation",
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true } 
                },
                vAxis: { 
                    title: "Ratio",
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true } 
                },
                series: {
                    0: { lineWidth: 5 },
                    1: { lineWidth: 5 }
                },
                colors: [ "#00FF83", "31BF00" ],
                legend: {position: "top", textStyle: { color: "dark", fontSize: 25 } },
                width: 1000,
                height: 600,
            }
        };
    },
    mounted() {
        if(statistics) {
            // individuals values 
            this.individuals.push([
                "Generations", 
                "#Individuals with non-null fitness"
            ]);
            // fitness values
            // structure : 
            // ["Generations", "Fitness"]
            this.fitness.push([
                "Generations", 
                "Avg. Fitness"
            ]);
            // [1, 1.2]
            // ...
            // [100, 120]
            //
            // population evolution
            // structure : 
            // ["Generations", "Fitness"]
            this.popEvol.push([
                "Generations", 
                "Diversity Coefficient",
                "Pop. Dvp. Rate",
            ]);
            // iterate on generations
            statistics.generations.forEach((generation) => {
                this.individuals.push([
                    generation.generation,
                    generation.numIndividualsWithNonNullFitness
                ]);
                this.fitness.push([
                    generation.generation,
                    generation.averageFitness
                ]);
                this.popEvol.push([
                    generation.generation,
                    generation.diversityCoefficient,
                    generation.populationDevelopmentRate
                ]);
            });
        }
    }
}
</script>
    