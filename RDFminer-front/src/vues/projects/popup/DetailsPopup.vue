<template>
    <CModal :visible="enable" alignment="center" scrollable size="xl">
        <CModalHeader>
            <CModalTitle>Settings of <b>{{ data.projectName }}</b></CModalTitle>
        </CModalHeader>
        <CModalBody>
            <CCard>
                <CCardTitle>
                    Global settings
                </CCardTitle>
                <CCardBody>
                    <!-- SPARQL Endpoint -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel>SPARQL Endpoint</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <a :href="data.targetSparqlEndpoint">{{ data.targetSparqlEndpoint }}</a>
                        </CCol>
                    </CRow>
                    <!-- Prefixes -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel>Prefixes</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormTextarea readonly>{{ data.prefixes }}</CFormTextarea>
                        </CCol>
                    </CRow>
                    <!-- Grammatical Evolution -->
                </CCardBody>
            </CCard>
            <br />
            <CCard v-if="data.task == 'Mining'">
                <CCardTitle>
                    Grammatical evolution settings
                </CCardTitle>
                <CCardBody>
                    <!-- Axioms ? Shapes ? -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Type of entities</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput :value="data.mod.includes('-rs') ? 'SHACL Shapes' : 'OWL Axioms'" readonly
                                plain-text style="font-weight: bold;" />
                        </CCol>
                    </CRow>
                    <!-- Probabilistic SHACL ? -->
                    <CRow v-if="data.settings.shaclProb != 0">
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Probabilistic SHACL:</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormLabel class="col-form-label">Significance level</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.shaclAlpha * 100 + '%'" readonly plain-text />
                        </CCol>
                        <CCol sm="2">
                            <CFormLabel class="col-form-label">P-Value</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.shaclProb * 100 + '%'" readonly plain-text />
                        </CCol>
                    </CRow>
                    <!-- BNF Grammar -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">BNF Grammar</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormTextarea readonly>{{ data.settings.bnf }}</CFormTextarea>
                        </CCol>
                    </CRow>
                    <!-- Chromosome size -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Chromosome size</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput :value="data.settings.sizeChromosome" readonly plain-text />
                        </CCol>
                    </CRow>
                    <!-- Pop size / Effort -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Population size</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.populationSize" readonly plain-text />
                        </CCol>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Effort</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.effort" readonly plain-text />
                        </CCol>
                    </CRow>
                    <!-- Elite -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Elite selection rate</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput :value="(data.settings.eliteSelectionRate * 100) + '%'" readonly plain-text />
                        </CCol>
                    </CRow>
                    <!-- Selection / Crossover / Mutation-->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Selection</CFormLabel>
                        </CCol>
                        <CCol sm="4">
                            <CFormInput
                                :value="selectionType[parseInt(data.settings.selectionType) - 1].description + ' (' + (data.settings.selectionRate * 100) + '%)'"
                                readonly plain-text />
                        </CCol>
                        <CCol sm="4">
                            <CFormInput
                                :value="'Tournament size (' + (data.settings.tournamentSelectionRate * 100) + '% of the population)'"
                                readonly plain-text />
                        </CCol>
                    </CRow>
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Crossover</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput
                                :value="crossoverType[parseInt(data.settings.crossoverType) - 1].description + ' (' + (data.settings.crossoverRate * 100) + '%)'"
                                readonly plain-text />
                        </CCol>
                    </CRow>
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Mutation</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput
                                :value="mutationType[parseInt(data.settings.mutationType) - 1].description + ' (' + (data.settings.selectionRate * 100) + '%)'"
                                readonly plain-text />
                        </CCol>
                    </CRow>
                </CCardBody>
            </CCard>

            <CCard v-else>
                <CCardTitle>
                    Assessment settings
                </CCardTitle>
                <CCardBody>
                    <!-- Axioms ? Shapes ? -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Type of entities</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormInput :value="data.mod.includes('-rs') ? 'SHACL Shapes' : 'OWL Axioms'" readonly
                                plain-text style="font-weight: bold;" />
                        </CCol>
                    </CRow>
                    <!-- Probabilistic SHACL ? -->
                    <CRow v-if="data.settings.shaclProb != 0">
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Probabilistic SHACL:</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormLabel class="col-form-label">Significance level</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.shaclAlpha * 100 + '%'" readonly plain-text />
                        </CCol>
                        <CCol sm="2">
                            <CFormLabel class="col-form-label">P-Value</CFormLabel>
                        </CCol>
                        <CCol sm="2">
                            <CFormInput :value="data.settings.shaclProb * 100 + '%'" readonly plain-text />
                        </CCol>
                    </CRow>
                    <!-- Input file -->
                    <CRow>
                        <CCol sm="4">
                            <CFormLabel class="col-form-label">Entities provided</CFormLabel>
                        </CCol>
                        <CCol sm="8">
                            <CFormTextarea readonly>{{ data.settings.shapes }}</CFormTextarea>
                        </CCol>
                    </CRow>
                </CCardBody>
            </CCard>

        </CModalBody>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import {
    CModal, CModalHeader, CModalTitle, CModalBody, CCol, CRow, CFormLabel, CFormInput,
    CFormTextarea, CCard, CCardBody, CCardTitle
} from "@coreui/vue";
import { get } from "@/tools/api";

export default {
    name: 'DetailsPopup',
    components: {
        CModal, CModalHeader, CModalTitle, CModalBody, CCol, CRow, CFormLabel,
        CFormInput, CFormTextarea, CCard, CCardBody, CCardTitle
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
    methods: {
        async setupParams() {
            const params = (await get("api/params", {}))[0];
            // selection
            this.selectionType = params.selection.values;
            // crossover
            this.crossoverType = params.crossover.values;
            // mutation
            this.mutationType = params.mutation.values;
        }
    },
    mounted() {
        this.setupParams();
    }
}
</script>
  
<style scoped></style>