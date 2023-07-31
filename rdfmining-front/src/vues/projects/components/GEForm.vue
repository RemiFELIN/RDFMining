<template>
    <!-- FOR GRAMMATICAL EVOLUTION of OWL Axioms -->
    <!-- <div v-if="mod.includes('-ge')"> -->
    <!-- Provide BNF Grammar file -->
    <!-- <BNFGrammar v-if="mod.includes('-ra')" :type="'AXIOM'" @textarea="updateBNFSample"></BNFGrammar>
                <BNFGrammar v-if="mod.includes('-rs')" :type="'SHACL'" @textarea="updateBNFSample"></BNFGrammar> -->
    <!-- Project Name -->
    <CRow class="mb-3">
        <CFormLabel for="projectName" class="col-sm-2 col-form-label">Select a BNF Grammar template</CFormLabel>
        <CCol sm="10">
            <CFormSelect aria-label="select-bnf" v-model="template">
                <option disabled>Select the required template</option>
                <option v-for="bnf in templates" :key="bnf" :value="bnf.content">{{ bnf.key }}</option>
            </CFormSelect>
        </CCol>
    </CRow>
    <!-- Enable Crowding method -->
    <!-- <CCheckbox :description="'Enable Crowding Method ?'" :defaultValue="false"
                    @checkboxChanged="updateEnableCrowding"></CCheckbox> -->
    <!-- Enable Novelty Search -->
    <!-- <CCheckbox :description="'Enable Novelty Search ?'" :defaultValue="false"
                    @checkboxChanged="updateEnableNoveltySearch"></CCheckbox> -->
    <!-- Timeout: SPARQL timeout and/or timecap -->
    <!-- </div>
            <div class="row"> -->
    <!-- <button type="button" v-on:click="generateCommandLine">Generate cmdline</button> -->
    <!-- <button type="button" v-on:click="postProject">Generate cmdline</button>
            </div>
        </form>
    </div> -->
    <!-- <b>La commande: {{ cmdline }}</b> -->
    <!-- <p>Mod: {{ mod }}</p> -->
    <!-- <CForm> -->

    <!-- Depending on the mod, we load the right component -->
    <!-- <CRow class="mb-3">
            <CFormLabel for="radios" class="col-sm-2 col-form-label">Radios</CFormLabel>
            <CCol sm="10">
                <CFormCheck type="radio" name="gridRadios" id="gridRadios1" value="option1" label="First radio" checked />
                <CFormCheck type="radio" name="gridRadios" id="gridRadios2" value="option2" label="Second radio" />
                <CFormCheck type="radio" name="gridRadios" id="gridRadios3" value="option3" label="Third disabled radio"
                    disabled />
            </CCol>
        </CRow>
        <CRow class="mb-3">
            <div class="col-sm-10 offset-sm-2">
                <CFormCheck type="checkbox" id="gridCheck1" label="Example checkbox" />
            </div>
        </CRow>
        <CButton type="submit">Sign in</CButton> -->
    <!-- </CForm> -->
</template>


<script>
// import { ref } from 'vue';
import { rdfminer } from '../../../data/form.json'
// import CSlider from '@/components/form/Slider.vue';
// import BNFGrammar from '@/components/form/BNFGrammar.vue';
// import CTextNumber from '@/components/form/TextNumber.vue';
// import CSelect from '@/components/form/Select.vue';
// import CCheckbox from '@/components/form/Checkbox.vue';
// import axios from "axios"
import { CRow, CFormLabel, CCol } from '@coreui/vue'

export default {
    name: 'GEForm',
    props: {
        mod: String
    },
    components: {
        CRow, CFormLabel, CCol
    },
    data() {
        return {
            // selected mod (in props)
            selectedMod: this.mod,
            // selected templates
            templates: [],
            selectedTemplate: "",
            templateDescription: "",
            // params json
            params: {},
            projectName: "",
            // template BNF Grammar
            // BNF Grammar
            bnfContent: "",
            // pop size
            populationSize: 200,
            // total effort 
            totalEffort: 1000,
            // ngen
            numberOfGenerations: 5,
            // chromosomes size
            chromSize: 2,
            // max wrapp
            maxWrap: 10,
            // typeSelection
            typeSelection: [],
            choosenSelection: 1,
            selectionRate: 0.1,
            // Crossover
            typeCrossover: [],
            choosenCrossover: 4,
            pCrossValue: 0.75,
            // Mutation
            typeMutation: [],
            choosenMutation: 1,
            pMutValue: 0.01,
            // crowding
            enableCrowding: false,
            // novelty search
            enableNoveltySearch: false,
        }
    },
    mounted() {
        if (rdfminer) {
            if (this.selectedMod.includes("ra")) {
                this.templates = rdfminer.axiomsGrammar;
                this.templateDescription = "OWL Axioms"
            } else {
                this.templates = rdfminer.SHACLGrammar;
                this.templateDescription = "SHACL Shape"
            }
        }
    },
}
</script>