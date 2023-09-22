<template>
    <div class="scroll">
        <CTable striped hover>
            <CTableHead color="light">
                <CTableRow>
                    <CTableHeaderCell v-for="header in headers" :key="header" scope="col">{{ header }}</CTableHeaderCell>
                </CTableRow>
            </CTableHead>
            <CTableBody>
                <CTableRow v-for="entity in entities" :key="entity" :color="getColor(entity.acceptance)">
                    <CTableHeaderCell scope="row" v-html="detectAndHighlightLink(entity.phenotype)"></CTableHeaderCell>
                    <CTableDataCell>{{ entity.referenceCardinality }}</CTableDataCell>
                    <CTableDataCell>{{ entity.numExceptions }}</CTableDataCell>
                    <CTableDataCell>{{ entity.numConfirmations }}</CTableDataCell>
                    <CTableDataCell>{{ entity.likelihood }}</CTableDataCell>
                    <CTableDataCell>{{ entity.fitness }}</CTableDataCell>
                    <CTableDataCell>{{ entity.acceptance }}</CTableDataCell>
                    <CTableDataCell>
                        <CImage v-if="entity.elite" src="/assets/crown.png" width="20" height="20" />
                    </CTableDataCell>
                </CTableRow>
            </CTableBody>
            <CTableFoot>
            </CTableFoot>
        </CTable>
    </div>
</template>


<script>
// https://hc200ok.github.io/vue3-easy-data-table-doc
// import { ref } from "vue";
import { CTable, CTableHead, CTableRow, CTableBody, CTableFoot, CTableDataCell, CTableHeaderCell, CImage } from "@coreui/vue";
// import { toRaw } from "vue";
// import Vue3EasyDataTable from 'vue3-easy-data-table';
// import { entities } from '../../data/results_1.json';
// import 'vue3-easy-data-table/dist/style.css';

export default {
    name: 'VisuEntities',
    props: {
        results: {
            type: Object
        }
    },
    components: {
        CTable, CTableHead, CTableRow, CTableBody, CTableFoot, CTableDataCell, CTableHeaderCell, CImage
    },
    methods: {
        getColor(accepted) {
            if (accepted)
                return "success";
            return "danger";
        },
        detectAndHighlightLink(phenotype) {
            //eslint-disable-next-line
            phenotype = phenotype.replace(/<http:\/\/[a-zA-Z0-9\/\.\#\-]*>/g, (match) => {
                return `<<a href="${match.replace(/<|>/g, '')}" target="_blank">${match.replace(/<|>/g, '')}</a>>`;
            });
            return phenotype;
        }
    },
    data() {
        return {
            headers: [],
            entities: [],
            phenotypes: [],
            // searchValue: ref("Yolo")
        };
    },
    mounted() {
        // Items
        // console.log("entities: " + JSON.stringify(this.results.entities));
        this.results.entities.forEach((entity) => {
            if (this.phenotypes.indexOf(entity.phenotype) != -1) {
                console.log("~doublon: " + entity.phenotype)
            }
            this.entities.push(entity);
        });
        // Header
        this.headers = ["SHACL Shape (" + this.entities.length + ")", "Reference Cardinality", "#Exceptions", "#Confirmations", "Likelihood", "Fitness Score", "Acceptance", "Elite"];
    }
}
</script>

<style scoped>
.scroll {
    /* background-color: #fed9ff; */
    /* width: auto; */
    height: 400px;
    overflow-x: hidden;
    overflow-y: auto;
    /* text-align: center; */
    /* padding: 20px; */
}
</style>
<!-- 
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
    --easy-table-scrollbar-thumb-color: #4c5d7a;
    ;
    --easy-table-scrollbar-corner-color: #2d3a4f;

    /* --easy-table-loading-mask-background-color: #2d3a4f; */
} -->
