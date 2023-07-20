

<template>
    <div>
        <!-- Specific search by keyword -->
        <span style="font-size: 2em;">search value: </span><input style="font-size: 2em;" type="text" v-model="searchValue">
        <Vue3EasyDataTable 
            :headers="headers" 
            :items="items" 
            :search-field="phenotype"
            :search-value="searchValue"
            :sort-by="fitness"
            :sort-type="desc"
            table-class-name="customize-table"
            theme-color="#1d90ff" 
            alternating
            buttons-pagination>
            <!-- show-index -->

            <!-- Shapes -->
            <template #item-fitness = "item">
                <b style="font-size: 2em;">{{ item.fitness }}</b>
            </template>
            <!-- CUSTOMIZED Number of violations / confirmations -->
            <template #item-numExceptions = "item">
                <b class="num-violations">{{ item.numExceptions }}</b>
            </template>
            <template #item-numConfirmations = "item">
                <b class="num-confirmations">{{ item.numConfirmations }}</b>
            </template>
            <!-- CUSTOMIZED Fitness Score -->
            <template #item-likelihood = "item">
                <b :style="getColor(item.likelihood)">{{ item.likelihood }}</b>
            </template>
            <!-- Reference Cardinality -->
            <template #item-referenceCardinality = "item">
                <p style="font-size: 2em;">{{ item.referenceCardinality }}</p>
            </template>
            <!-- Fitness -->
            <template #item-phenotype = "item">
                <p class="shapes">{{ item.phenotype }}</p>
            </template>
            <!-- LOADING BEHAVIOR -->
            <template #loading>
                <iframe src="https://giphy.com/embed/AigDNTK9tnGQdgyZoS" width="480" height="480" frameBorder="0" class="giphy-embed" allowFullScreen></iframe><p><a href="https://giphy.com/gifs/AigDNTK9tnGQdgyZoS"></a></p>
            </template>
            <!-- EMPTY SET -->
            <template #empty-message>
                <b style="font-size: 2em;">None of the shapes match your search ...</b>
            </template>
        </Vue3EasyDataTable>
    </div>
</template>


<script>
// https://hc200ok.github.io/vue3-easy-data-table-doc
import { ref } from "vue";
import Vue3EasyDataTable from 'vue3-easy-data-table';
import { entities } from '../data/results_1.json';
import 'vue3-easy-data-table/dist/style.css';

export default {
    name: 'VisuShapes',
    components: {
        Vue3EasyDataTable,
    },
    methods: {
        getColor(a) {
            console.log((0.3 + 0.7 * a))
            return "font-size: 1.2em; color: rgb(0,0,0," + (0.3 + 0.7 * a)+ ");"
        }
    },
    data() {
        return {
            headers: [],
            items: [],
            searchValue: ref("Yolo")
        };
    },
    mounted() {
        if(entities) {
            // Header
            this.headers = [
                { text: "Generation", value: "generation", sortable: true },
                { text: "SHACL Shape", value: "phenotype", width: 100 },
                { text: "Reference Cardinality", value: "referenceCardinality", sortable: true},
                { text: "#Exceptions", value: "numExceptions", sortable: true },
                { text: "#Confirmations", value: "numConfirmations", sortable: true },
                { text: "Likelihood", value: "likelihood", sortable: true },
                { text: "Fitness Score", value: "fitness", sortable: true },
                // TODO: confirmations
                // TODO: exceptions
            ];
            // Items
            entities.forEach((entity) => {
                this.items.push(entity);
            });
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.shapes {
    font-family: "Lucida Console", Courier, monospace;
    font-size: 1em;
}
.num-violations {
    font-size: 1.8em; 
    color: rgb(195, 0, 0);
}
.num-confirmations {
    font-size: 1.8em; 
    color: rgba(0, 195, 7, 0.503);
}
.customize-table {
    --easy-table-border: 5px solid #cecece;
    --easy-table-row-border: 1px solid #445269;

    --easy-table-header-font-size: 2em;
    --easy-table-header-height: 50px;
    --easy-table-header-font-color: #c1cad4;
    --easy-table-header-background-color: #003c9e;

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
    --easy-table-footer-background-color: #029711;
    --easy-table-footer-font-color: #c0c7d2;
    --easy-table-footer-font-size: 1.5em;
    --easy-table-footer-padding: 0px 10px;
    --easy-table-footer-height: 10%;

    --easy-table-rows-per-page-selector-width: 70px;
    --easy-table-rows-per-page-selector-option-padding: 10px;
    --easy-table-rows-per-page-selector-z-index: 1;


    --easy-table-scrollbar-track-color: #2d3a4f;
    --easy-table-scrollbar-color: #2d3a4f;
    --easy-table-scrollbar-thumb-color: #4c5d7a;;
    --easy-table-scrollbar-corner-color: #2d3a4f;

    /* --easy-table-loading-mask-background-color: #2d3a4f; */
}
</style>
