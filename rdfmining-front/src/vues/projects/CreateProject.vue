<template>
    <CForm>
        <!-- 
            Project Name 
        -->
        <CRow class="mb-3">
            <CFormLabel for="projectName" class="col-sm-2 col-form-label">Name of the project</CFormLabel>
            <CCol sm="10">
                <CFormInput type="email" class="col-sm-2 col-form-label" id="projectName"
                    placeholder="Example: MyProject; my-project; my_project" v-model="projectName" />
            </CCol>
        </CRow>
        <!-- 
            Mod selection 
        -->
        <CRow class="mb-3">
            <CFormLabel for="modSelection" class="col-sm-2 col-form-label">I would like to ...</CFormLabel>
            <CCol sm="10">
                <CFormSelect aria-label="select-mod" v-model="mod">
                    <option selected disabled>Select the required mode</option>
                    <option v-for="feature in features" :key="feature" :value="feature.cmd">{{ feature.text }}</option>
                </CFormSelect>
            </CCol>
        </CRow>
        <!-- 
            Depending on the mod, we load the right component 
            Just below: Grammatical Evolution Mod
        -->
        <div v-if="mod.includes('ge')">
            <!--
                BNF Grammar template
            -->
            <CRow class="mb-3">
                <CFormLabel for="bnfTemplate" class="col-sm-2 col-form-label">Select a BNF Grammar template</CFormLabel>
                <CCol sm="10">
                    <CFormSelect v-if="mod.includes('ra')" aria-label="select-bnf-axioms" v-model="selectedTemplate">
                        <option selected disabled>Select the required template</option>
                        <option v-for="bnf in axiomTemplates" :key="bnf" :value="bnf.content">{{ bnf.key }}</option>
                    </CFormSelect>
                    <CFormSelect v-else aria-label="select-bnf-shacl" v-model="selectedTemplate">
                        <option selected disabled>Select the required template</option>
                        <option v-for="bnf in shaclTemplates" :key="bnf" :value="bnf.content">{{ bnf.key }}</option>
                    </CFormSelect>
                </CCol>
            </CRow>
            <!--
                BNF Grammar content
            -->
            <CRow class="mb-3">
                <CFormLabel for="bnfContent" class="col-sm-2 col-form-label">The selected BNF Grammar (editable)
                </CFormLabel>
                <CCol sm="10">
                    <CFormTextarea id="bnfGrammar" style="color: rgb(1, 108, 157)">
                        {{ selectedTemplate }}
                    </CFormTextarea>
                </CCol>
            </CRow>
            <!--
                Population size
            -->
            <CRow class="mb-3">
                <CFormLabel for="populationSize" class="col-sm-2 col-form-label">Population size</CFormLabel>
                <CCol sm="9">
                    <CFormRange :min="50" :max="1000" :step="50" id="slider-popSize" v-model="populationSize" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-popSize" class="col-form-label">Value: <b>{{ populationSize }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Total effort
            -->
            <CRow class="mb-3">
                <CFormLabel for="totalEffort" class="col-sm-2 col-form-label">Total effort</CFormLabel>
                <CCol sm="9">
                    <CFormRange :min="1000" :max="maxEffort" :step="1000" id="slider-totalEffort" v-model="totalEffort" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-totalEffort" class="col-form-label">Value: <b>{{ totalEffort }}</b></CFormLabel>
                </CCol>
            </CRow>
            <CAlert color="success">
                You will perform <b>{{ numberOfGenerations }} generations !</b>
            </CAlert>
            <!-- 
                Selection: type of selection and proportion 
            -->
            <CRow class="mb-3">
                <CFormLabel for="selectionType" class="col-sm-2 col-form-label">Selection</CFormLabel>
                <CCol sm="3">
                    <CFormSelect aria-label="select-selection" v-model="choosenSelection">
                        <option selected disabled>Select the required selection</option>
                        <option v-for="selection in typeSelection" :key="selection" :value="selection.cmd">{{ selection.text }}</option>
                    </CFormSelect>
                </CCol>
                <CCol sm="6">
                    <CFormRange :min="0" :max="1" :step="0.05" id="slider-selection" v-model="selectionRate" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-selectionRate" class="col-form-label">Rate: <b>{{ selectionRate }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Crossover: type of crossover and proportion 
            -->
            <CRow class="mb-3">
                <CFormLabel for="crossoverType" class="col-sm-2 col-form-label">Crossover</CFormLabel>
                <CCol sm="3">
                    <CFormSelect aria-label="select-crossover" v-model="choosenCrossover">
                        <option selected disabled>Select the required crossover</option>
                        <option v-for="selection in typeCrossover" :key="selection" :value="selection.cmd">{{ selection.text }}</option>
                    </CFormSelect>
                </CCol>
                <CCol sm="6">
                    <CFormRange :min="0" :max="1" :step="0.05" id="slider-crossover" v-model="crossoverRate" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-crossoverRate" class="col-form-label">Rate: <b>{{ crossoverRate }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Mutation: type of mutation and proportion 
            -->
            <CRow class="mb-3">
                <CFormLabel for="mutationType" class="col-sm-2 col-form-label">Mutation</CFormLabel>
                <CCol sm="3">
                    <CFormSelect aria-label="select-mutation" v-model="choosenMutation">
                        <option selected disabled>Select the required mutation</option>
                        <option v-for="selection in typeMutation" :key="selection" :value="selection.cmd">{{ selection.text }}</option>
                    </CFormSelect>
                </CCol>
                <CCol sm="6">
                    <CFormRange :min="0" :max="1" :step="0.01" id="slider-mutation" v-model="mutationRate" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-mutationRate" class="col-form-label">Rate: <b>{{ mutationRate }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Size of chromosomes 
            -->
            <CRow class="mb-3">
                <CFormLabel for="chromSize" class="col-sm-2 col-form-label">Chromosomes size</CFormLabel>
                <CCol sm="9">
                    <CFormRange :min="1" :max="20" :step="1" id="slider-chromSize" v-model="chromSize" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-chromSize" class="col-form-label">Value: <b>{{ chromSize }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Max Wrapping
            -->
            <CRow class="mb-3">
                <CFormLabel for="maxWrap" class="col-sm-2 col-form-label">Max Wrap</CFormLabel>
                <CCol sm="9">
                    <CFormRange :min="1" :max="100" :step="1" id="slider-maxWrap" v-model="maxWrap" />
                </CCol>
                <CCol sm="1">
                    <CFormLabel for="val-maxWrapp" class="col-form-label">Value: <b>{{ maxWrap }}</b></CFormLabel>
                </CCol>
            </CRow>
            <!-- 
                Crowding Method - Novelty Search
            -->
            <CRow class="mb-3">
                <CFormLabel for="maxWrap" class="col-sm-2 col-form-label">Some Optimizers</CFormLabel>
                <CCol sm="5">
                    <CFormSwitch label="Crowding method (boost the population diversity)" id="switch-crowding" v-model="enableCrowding"/>
                </CCol>
                <CCol sm="5">
                    <CFormSwitch label="Novelty Search (Experimental: only for SubClassOf axioms)" id="switch-ns" v-model="enableNoveltySearch" :disabled="!mod.includes('ra')"/>
                </CCol>
            </CRow>
        </div>
    </CForm>
</template>


<script>
import { rdfminer } from '../../data/form.json'
import axios from "axios"
import { CForm, CRow, CFormLabel, CCol, CFormInput, CFormSelect, CFormTextarea, CFormRange, CAlert, CFormSwitch } from '@coreui/vue'

export default {
    name: 'CreateProject',
    props: {
        id: {
            type: String
        },
    },
    components: {
        CForm, CRow, CFormLabel, CCol, CFormInput, CFormSelect, CFormTextarea, CFormRange, CAlert, CFormSwitch
    },
    watch: {
        totalEffort() {
            this.updateTotalEffort();
        },
        populationSize() {
            this.updateMaxEffort();
        }
    },
    data() {
        return {
            // features
            features: [],
            // BNF Grammar templates
            shaclTemplates: [],
            axiomTemplates: [],
            selectedTemplate: "",
            descriptionTemplate: "",
            // cmdline of the experiment
            cmdline: "",
            cmdlineBase: "docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh ",
            // params json
            params: {},
            projectName: "",
            mod: "",
            modKey: "",
            // BNF Grammar
            bnfContent: "",
            // pop size
            populationSize: 200,
            // total effort 
            maxEffort: 10000,
            totalEffort: 10000,
            // ngen
            numberOfGenerations: 50,
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
            crossoverRate: 0.75,
            // Mutation
            typeMutation: [],
            choosenMutation: 1,
            mutationRate: 0.01,
            // crowding
            enableCrowding: false,
            // novelty search
            enableNoveltySearch: false,
        }
    },
    mounted() {
        // read json
        if (rdfminer) {
            // features
            this.features = rdfminer.features;
            // BNF templates
            this.axiomTemplates = rdfminer.axiomsGrammar;
            this.shaclTemplates = rdfminer.SHACLGrammar;
            this.typeSelection = rdfminer.typeSelection;
            this.typeCrossover = rdfminer.typeCrossover;
            this.typeMutation = rdfminer.typeMutation;
        }
    },
    methods: {
        updateTotalEffort() {
            this.numberOfGenerations = Math.floor(this.totalEffort / this.populationSize);
        },
        updateMaxEffort() {
            this.maxEffort = this.populationSize * 50;
            this.totalEffort = this.maxEffort;
            this.updateTotalEffort();
        },
        getProjectName() {
            // if the project name is not defined 
            if (this.projectName == "") {
                this.projectName = "MyProject"
            }
            return " -dir " + this.projectName;
        },
        addParam(cmd, value) {
            return cmd + " " + value + " ";
        },
        registerProject() {
            // Mod
            this.cmdline = this.cmdlineBase + this.mod + " " + this.getProjectName() + " "
            // In the GE Context
            if (this.mod.includes('-ge')) {
                this.cmdline += this.addParam(rdfminer.parameters.populationSize, this.populationSize);
                this.cmdline += this.addParam(rdfminer.parameters.kBase, this.totalEffort);
                this.cmdline += this.addParam(rdfminer.parameters.lenChromosome, this.chromSize);
                this.cmdline += this.addParam(rdfminer.parameters.maxWrapp, this.maxWrap);
                this.cmdline += this.addParam(rdfminer.parameters.typeSelection, this.choosenSelection);
                this.cmdline += this.addParam(rdfminer.parameters.typeCrossover, this.choosenCrossover);
                this.cmdline += this.addParam(rdfminer.parameters.typeMutation, this.choosenMutation);
                // novelty search
                if (this.enableNoveltySearch) {
                    this.cmdline += rdfminer.parameters.noveltySearch + " ";
                }
                // crowding ?
                if (this.enableCrowding) {
                    this.cmdline += rdfminer.parameters.diversity + " 1";
                } else {
                    this.cmdline += rdfminer.parameters.diversity + " 0";
                }
            }
            // fill params object
            this.params = {
                mod: this.mod,
                projectName: this.projectName,
                populationSize: this.populationSize,
                kBase: this.totalEffort,
                lenChromosome: this.chromSize,
                maxWrapp: this.maxWrap,
                typeSelection: this.choosenSelection,
                typeMutation: this.choosenMutation,
                typeCrossover: this.choosenCrossover,
                noveltySearch: this.enableNoveltySearch,
                crowding: this.enableCrowding
            }
        },
        postProject() {
            this.registerProject();
            console.log(this.params);
            // console.log(this.cmdline);
            // build a request to the API
            axios.post("http://localhost:3000/api/project/setup", {
                userId: this.id,
                projectName: this.projectName,
                command: this.cmdline,
                mod: this.modKey,
                params: this.params,
            }).then(
                (response) => {
                    if (response.status === 200) {
                        console.log("OK !" + response);
                        // this.user = { username, password };
                        this.auth = true;
                    }
                }
            ).catch((error) => {
                console.log(error);
                alert("Incorrect username/password");
            });
        }
    }
}
</script>