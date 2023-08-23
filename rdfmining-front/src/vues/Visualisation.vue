<template>
    <h1 style="text-align: center;">Summary Dashboard for the <i>{{ results.projectName }}</i> project</h1>
    <CAccordion class="customizedAccordion" :active-item-key="1" always-open>
        <CAccordionItem :item-key="1">
            <CAccordionHeader>Global information</CAccordionHeader>
            <CAccordionBody>
                <!-- <VisuEntities v-if="isReady" :results="results"></VisuEntities> -->
                <VueGlobal v-if="isReady" :results="results" :path="path"></VueGlobal>
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="3">
            <CAccordionHeader>Entities</CAccordionHeader>
            <CAccordionBody>
                <VisuEntities v-if="isReady" :results="results"></VisuEntities>
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="2">
            <CAccordionHeader>Statistics</CAccordionHeader>
            <CAccordionBody>
                <VueStatistics v-if="isReady" :results="results"></VueStatistics>
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="4">
            <CAccordionHeader>Console log</CAccordionHeader>
            <CAccordionBody>
                <ConsoleLog v-if="isReady" :path="path"></ConsoleLog>
            </CAccordionBody>
        </CAccordionItem>
    </CAccordion>
</template>

<script>
import { CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody } from '@coreui/vue';
import VueStatistics from './vis/Statistics.vue'
import VisuEntities from './vis/Entities.vue'
import ConsoleLog from './vis/ConsoleLog.vue'
import VueGlobal from './vis/Global.vue';
import axios from 'axios';

export default {
    name: 'VueVisualisation',
    props: {
        resultsId: {
            type: String,
        }
    },
    data() {
        return {
            id: "",
            results: {},
            path: "",
            isReady: false,
        };
    },
    // methods: {
    //     getPath() {
    //         return this.results.userId + "/" + this.results.projectName;
    //     }
    // },
    mounted() {
        this.id = this.$route.params.resultsId;
        console.log(this.id);
        // get results from server
        axios.get("http://localhost:9200/api/results", { params: { resultsId: this.id } }).then(
            (response) => {
                if (response.status === 200) {
                    // redirect on visualisation route with the results ID linked to the project
                    console.log("visualisation: " + JSON.stringify(response.data));
                    if (response.data != {}) {
                        this.results = response.data;
                        this.path = this.results.userId + "/" + this.results.projectName;
                        this.isReady = true;
                    }
                }
            }
        ).catch((error) => {
            console.log(error);
        });
    },
    components: {
        VueStatistics, VisuEntities, CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody, ConsoleLog, VueGlobal
    },
}
</script>

<style scoped>
.customizedAccordion {
    --cui-accordion-btn-color: rgb(14, 14, 163);
}
</style>
  