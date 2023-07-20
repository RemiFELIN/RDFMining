<template>
    <h1 style="text-align: center;">Summary Dashboard for the {{ experienceId }} project</h1>
    <section id="main-content" class="container">
        <div>
            <h2>Statistics</h2>
            <VueStatistics></VueStatistics>
        </div>
        <div>
            <h2>SHACL Shapes</h2>
            <VisuShapes></VisuShapes>
        </div>
    </section>
</template>

<script>
import VueStatistics from '../rdfminer/Statistics.vue'
import VisuShapes from '../rdfminer/Shapes.vue'

export default {
    name: 'VueVisualisation',
    props: {
        experienceId: String
    },
    components: {
        VueStatistics,
        VisuShapes
    }
}
</script>

<style scoped>
section#main-content {
    margin: 20px;
}
</style>
  