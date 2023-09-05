<template>
    <h1 style="text-align: center;">Summary Dashboard for the <i>{{ results.projectName }}</i> project</h1>
    <CAccordion class="customizedAccordion" :active-item-key="1" always-open>
        <CAccordionItem :item-key="1">
            <CAccordionHeader>Global information</CAccordionHeader>
            <CAccordionBody>
                <!-- <VisuEntities v-if="isReady" :results="results"></VisuEntities> -->
                <VueGlobalGE v-if="isReady && task=='Mining'" :results="results" :path="path"></VueGlobalGE>
                <VueGlobalEval v-if="isReady && task=='Assessment'" :results="results" :path="path"></VueGlobalEval>
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="3">
            <CAccordionHeader>Entities</CAccordionHeader>
            <CAccordionBody>
                <VisuEntities v-if="isReady" :results="results"></VisuEntities>
            </CAccordionBody>
        </CAccordionItem>
        <CAccordionItem :item-key="2" v-if="task=='Mining'">
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
import { useCookies } from "vue3-cookies";
import VueStatistics from './Statistics.vue';
import VisuEntities from './Entities.vue';
import ConsoleLog from './ConsoleLog.vue';
import VueGlobalGE from './GlobalGE.vue';
import VueGlobalEval from './GlobalEval.vue';
import axios from 'axios';

export default {
    name: 'VueVisualisation',
    // props: {
    //     resultsId: {
    //         type: String,
    //     },
    //     task: {
    //         type: String
    //     }
    // },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            id: "",
            task: "",
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
        this.task = this.$route.params.task;
        this.id = this.$route.params.resultsId;
        console.log(this.task);
        console.log(this.id);
        // get results from server
        axios.get("http://localhost:9200/api/results", { 
            params: { resultsId: this.id },
            headers: { "x-access-token": this.cookies.get("token") } 
        }).then(
            (response) => {
                if (response.status === 200) {
                    // redirect on visualisation route with the results ID linked to the project
                    // console.log("visualisation: " + JSON.stringify(response.data));
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
        VueStatistics, VisuEntities, CAccordion, CAccordionItem, CAccordionHeader, CAccordionBody, ConsoleLog, VueGlobalGE, VueGlobalEval
    },
}
</script>

<style scoped>
.customizedAccordion {
    --cui-accordion-btn-color: rgb(14, 14, 163);
}
</style>
  