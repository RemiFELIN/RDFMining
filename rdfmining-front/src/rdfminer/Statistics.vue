<template>
    <GChart class="container" type="AreaChart" 
        :data="individuals" 
        :options="individuals_options" 
    />
    <!-- FITNESS VISUALISATION -->
    <GChart class="container" type="AreaChart" 
        :data="fitness" 
        :options="fitness_options" 
    />
    <!-- POPULATION EVOLUTION VISUALISATION -->
    <GChart class="container" type="AreaChart" 
        :data="popEvol" 
        :options="popEvol_options"
    />
    <GChart class="container" type="BubbleChart" 
        :data="entitiesData" 
        :options="bubble_options" 
    />
</template>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.container {
    display: flex;
    justify-content: space-around;
    width: 100%;
}

@media screen and (max-width: 768px) {
    .container {
        flex-direction: column;
        align-items: center;
    }
}
</style>

<script>
// https://developers.google.com/chart/interactive/docs/gallery/areachart?hl=fr#overview
import { GChart } from 'vue-google-charts'
import { statistics, entities } from '../data/results_1.json'

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
                // width: 1500,
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
                // width: 1500,
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
                // width: 1500,
                height: 600,
            },
            // ELAPSED TIME / REFERENCE CARDINALITY (EXCEPTIONS ?)
            entitiesData: [],
            bubble_options: {
                title: "Overview of entities found",
                titleTextStyle: { fontSize: 30 },
                backgroundColor: "#ffffff",
                hAxis: { 
                    title: "Reference Cardinality",
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true } 
                },
                vAxis: { 
                    title: "CPU Computation time (ms.)",
                    textStyle: { fontSize: 30 }, 
                    titleTextStyle: { fontSize: 20, italic: false, bold: true } 
                },
                bubble: { textStyle: { auraColor: 'none', fontSize: 1 } },
                sizeAxis: { minSize: 20,  maxSize: 100 },
                explorer: {},
                colorAxis: {colors: ['#15d600', '#ff0000']},
                // width: 1500,
                height: 1000,
            },
        };
    },
    mounted() {
        if(entities) {
            // entities data
            this.entitiesData.push([
                "phenotype",
                "referenceCardinality",
                "elapsedTime",
                "Violations Ratio",
                "numExceptions"
            ]);
            // iterate on entities found
            entities.forEach((entity) => {
                console.log(entity.numExceptions/entity.referenceCardinality)
                this.entitiesData.push([
                    entity.phenotype,
                    entity.referenceCardinality,
                    entity.elapsedTime,
                    entity.numExceptions/entity.referenceCardinality,
                    entity.numExceptions
                ]);
            });
        }
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
    