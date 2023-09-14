<template>
    <CModal :visible="enable" alignment="center" scrollable>
        <CModalHeader>
            <CModalTitle>Settings of <b>{{ data.projectName }}</b></CModalTitle>
        </CModalHeader>
        <CModalBody>
            <h3>Global settings</h3>
            <br>
            <!--TASK-->
            <!-- <CRow class="mb-3">
                <CFormLabel class="col-sm-4 col-form-label"><b>Task</b></CFormLabel>
                <CCol sm="8">
                    <CFormInput :value="data.task" readonly plain-text />
                </CCol>
            </CRow> -->
            <!-- SPARQL Endpoint -->
            <CRow class="mb-3">
                <CFormLabel class="col-sm-4 col-form-label"><b>SPARQL Endpoint</b></CFormLabel>
                <CCol sm="8">
                    <CFormInput :value="data.targetSparqlEndpoint" readonly plain-text />
                </CCol>
            </CRow>
            <!-- Prefixes -->
            <CRow class="mb-3">
                <CFormLabel class="col-sm-4 col-form-label"><b>Prefixes</b></CFormLabel>
                <CCol sm="8">
                    <CFormTextarea readonly>
                        {{ data.prefixes }}
                    </CFormTextarea>
                </CCol>
            </CRow>
            <!-- Grammatical Evolution -->
            <div v-if="data.task == 'Mining'">
                <h3>Grammatical evolution settings</h3>
                <br>
                <!-- Axioms ? Shapes ? -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Type of entities</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="data.mod.includes('-rs') ? 'SHACL Shapes' : 'OWL Axioms'" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- Probabilistic SHACL ? -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Probabilistic SHACL ?</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="data.settings.shaclProb != 0 ? 'Yes' : 'No'" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- if yes -->
                <!-- <div v-if="selectedFeature.includes('rs')"> -->
                <CRow class="mb-3" v-if="data.settings.shaclProb != 0">
                    <CCol sm="3">
                        <!-- Alpha -->
                        <CFormLabel class="col-sm-4 col-form-label"><b>significance level</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.shaclAlpha" readonly plain-text />
                    </CCol>
                    <CCol sm="3">
                        <!-- p-value -->
                        <CFormLabel class="col-sm-4 col-form-label"><b>P-value</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.shaclProb" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- BNF Grammar -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>BNF Grammar</b></CFormLabel>
                    <CCol sm="8">
                        <CFormTextarea readonly>
                            {{ data.settings.bnf }}
                        </CFormTextarea>
                    </CCol>
                </CRow>
                <!-- Chromosome size -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Chromosome size</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="data.settings.sizeChromosome" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- Pop size / Effort -->
                <CRow class="mb-3">
                    <CCol sm="3">
                        <CFormLabel class="col-sm-4 col-form-label"><b>Population size</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.populationSize" readonly plain-text />
                    </CCol>
                    <CCol sm="3">
                        <CFormLabel class="col-sm-4 col-form-label"><b>Effort</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.effort" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- Selection / Crossover / Mutation-->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>Selection</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="selectionType[parseInt(data.settings.selectionType) - 1].description" readonly plain-text />
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="(data.settings.selectionRate * 100) + '%'" readonly plain-text />
                    </CCol>
                </CRow>
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>Crossover</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="crossoverType[parseInt(data.settings.crossoverType) - 1].description" readonly plain-text />
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="(data.settings.crossoverRate * 100) + '%'" readonly plain-text />
                    </CCol>
                </CRow>
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>Mutation</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="mutationType[parseInt(data.settings.mutationType) - 1].description" readonly plain-text />
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="(data.settings.mutationRate * 100) + '%'" readonly plain-text />
                    </CCol>
                </CRow>
            </div>
            <!-- Evaluator -->
            <div v-else>
                <h3>Assessment settings</h3>
                <br>
                <!-- Axioms ? Shapes ? -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Type of entities</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="data.mod == '-sf' ? 'SHACL Shapes' : 'OWL Axioms'" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- Probabilistic SHACL ? -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Probabilistic SHACL ?</b></CFormLabel>
                    <CCol sm="8">
                        <CFormInput :value="data.settings.shaclProb != 0 ? 'Yes' : 'No'" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- if yes -->
                <!-- <div v-if="selectedFeature.includes('rs')"> -->
                <CRow class="mb-3" v-if="data.settings.shaclProb != 0">
                    <CCol sm="3">
                        <!-- Alpha -->
                        <CFormLabel class="col-sm-4 col-form-label"><b>significance level</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.shaclAlpha" readonly plain-text />
                    </CCol>
                    <CCol sm="3">
                        <!-- p-value -->
                        <CFormLabel class="col-sm-4 col-form-label"><b>P-value</b></CFormLabel>
                    </CCol>
                    <CCol sm="2">
                        <CFormInput :value="data.settings.shaclProb" readonly plain-text />
                    </CCol>
                </CRow>
                <!-- Input file -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-4 col-form-label"><b>Entities provided</b></CFormLabel>
                    <CCol sm="8">
                        <CFormTextarea readonly>
                            {{ data.settings.shapes }}
                        </CFormTextarea>
                    </CCol>
                </CRow>
            </div>
        </CModalBody>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import axios from "axios"
import { CModal, CModalHeader, CModalTitle, CModalBody, CCol, CRow, CFormLabel, CFormInput, CFormTextarea } from "@coreui/vue";
// import { markRaw } from "vue";
// import { useCookies } from "vue3-cookies";
// import { JsonViewer } from "vue3-json-viewer";
// import "vue3-json-viewer/dist/index.css";

export default {
    name: 'DetailsPopup',
    components: {
        CModal, CModalHeader, CModalTitle, CModalBody, CCol, CRow, CFormLabel, CFormInput, CFormTextarea
    },
    props: {
        enable: {
            type: Boolean
        },
        data: {
            type: Object
        }
    },
    data() {
        return {
            selectionType: {},
            crossoverType: {},
            mutationType: {}
        }
    },
    watch: {
        data() {
            // console.log(this.data)
        }
    },
    mounted() {
        axios.get("http://localhost:9200/api/params").then(
            (response) => {
                const data = response.data[0];
                // selection
                this.selectionType = data.selection.values;
                // crossover
                this.crossoverType = data.crossover.values;
                // mutation
                this.mutationType = data.mutation.values;
                console.log(data.mutation.values);
            }
        ).catch((error) => {
            console.log(error);
        });

    }
}
</script>
  
<style scoped></style>