<template>
    <CForm novalidate :validated="validated">
        <CCard>
            <CCardTitle class="text-center">
                General Settings
            </CCardTitle>
            <CCardBody>
                <!-- 
            Project Name 
        -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>{{ directory.description }}</b></CFormLabel>
                    <CCol sm="10">
                        <CFormInput type="email" class="col-sm-2 col-form-label"
                            placeholder="Example: MyProject; my-project; my_project" v-model="selectedProjectName" required
                            :feedback-invalid="errorMessage" :invalid="selectedProjectName == '' || alreadyExist" />
                    </CCol>
                </CRow>
                <!-- Load parameters from existing project-->
                <!-- <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label">Load parameters from existing project</CFormLabel>
                    <CCol sm="10">
                        <CFormSelect v-model="projectToLoad">
                            <option selected disabled>Select an existing project settings </option>
                            <option v-for="project in projects" :key="project" :value="project">{{ project.projectName
                            }}
                            </option>
                        </CFormSelect>
                    </CCol>
                </CRow> -->
                <!-- 
            Mod selection 
        -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>I would like to ...</b></CFormLabel>
                    <CCol sm="10">
                        <CFormSelect v-model="selectedFeature" @change="selectedBNFTemplate = ''"
                            feedback-invalid="Please, select a feature" :invalid="selectedFeature == ''">
                            <option selected disabled>Select the required mode</option>
                            <option v-for="feature in features" :key="feature" :value="feature.cmd">{{ feature.description
                            }}
                            </option>
                        </CFormSelect>
                    </CCol>
                </CRow>
                <!-- 
            SPARQL Endpoint
        -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>{{ targetSparqlEndpoint.description }}</b></CFormLabel>
                    <CCol sm="3">
                        <CFormSelect v-model="selectedTargetEndpoint" feedback-invalid="Please, select a SPARQL endpoint"
                            :invalid="selectedTargetEndpoint == ''">
                            <option selected disabled>Select the required endpoint</option>
                            <option v-for="endpoint in endpoints" :key="endpoint" :value="endpoint.value">{{
                                endpoint.description }}
                            </option>
                        </CFormSelect>
                    </CCol>
                    <CCol sm="7">
                        <CFormInput v-model="selectedTargetEndpoint" feedback-invalid="It must be a valid SPARQL endpoint"
                            :invalid="!selectedTargetEndpoint.includes('http://')" />
                    </CCol>
                </CRow>
                <!--
            Prefixes
        -->
                <CRow class="mb-3">
                    <CFormLabel class="col-sm-2 col-form-label"><b>{{ prefixes.description }}</b></CFormLabel>
                    <CCol sm="3">
                        <!-- Template -->
                        <CFormSelect v-model="selectedPrefixes" feedback-invalid="Please select a prefixes sample" required
                            :invalid="selectedPrefixes == ''">
                            <option selected disabled>Select the required prefixes</option>
                            <option v-for="sample in prefixesSamples" :key="sample" :value="sample.value">{{
                                sample.description }}
                            </option>
                        </CFormSelect>
                        <!-- <br /> -->
                        <!-- <p>{{ selectedPrefixes }}</p> -->
                    </CCol>
                    <CCol sm="7">
                        <!-- Content -->
                        <CFormTextarea style="color: rgb(1, 108, 157)" v-model="selectedPrefixes">
                            {{ selectedPrefixes }}
                        </CFormTextarea>
                    </CCol>
                </CRow>
            </CCardBody>
        </CCard><br />
        <!-- 
            Depending on the mod, we load the right component 
            Just below: Grammatical Evolution Mod
        -->
        <div v-if="selectedFeature.includes('ge')">
            <CCard>
                <CCardTitle class="text-center">Grammatical Evolution</CCardTitle>
                <CCardBody>
                    <!--
                SHACL
            -->
                    <div v-if="selectedFeature.includes('rs')">
                        <CRow class="mb-3">
                            <CFormLabel class="col-sm-2 col-form-label"><b>Probabilistic SHACL</b></CFormLabel>
                            <CCol sm="2">
                                <!-- Alpha -->
                                <CFormLabel>{{ shaclAlpha.description }}</CFormLabel>
                            </CCol>
                            <CCol sm="2">
                                <!-- Alpha -->
                                <CFormSelect v-model="selectedShaclAlpha" required>
                                    <option selected disabled>Select the required significance level</option>
                                    <option v-for="alpha in alphaValues" :key="alpha" :value="alpha.value">{{
                                        alpha.description
                                    }}</option>
                                </CFormSelect>
                            </CCol>
                            <CCol sm="1">
                                <!-- p-value -->
                                <CFormLabel>{{ shaclProb.description }}</CFormLabel>
                            </CCol>
                            <CCol sm="4">
                                <CFormRange :min="0" :max="1" :step="0.05" v-model="selectedShaclProb" />
                            </CCol>
                            <CCol sm="1">
                                <CFormLabel class="col-form-label"><b>{{ selectedShaclProb }}</b></CFormLabel>
                            </CCol>
                        </CRow>
                        <CAlert color="warning" v-if="selectedShaclProb == 0">
                            <b>Standard SHACL validation !</b> increase the <b>P-value</b> to enable the <b>probabilistic
                                SHACL
                                validation</b>
                        </CAlert>
                        <CAlert color="warning" v-else>
                            <b>Probabilistic SHACL validation !</b> set the <b>P-value</b> at 0 to enable the <b>standard
                                SHACL
                                validation</b>
                        </CAlert>
                    </div>
                    <!--
                BNF Grammar
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ grammar.description }}</b></CFormLabel>
                        <CCol sm="3">
                            <!-- Template -->
                            <CFormSelect aria-label="select-bnf-axioms" v-model="selectedBNFTemplate"
                                feedback-invalid="Please select a BNF template (You can customize it !)" required
                                :invalid="selectedBNFTemplate == ''">
                                <option selected disabled>Select the required template</option>
                                <option v-for="bnf in filteredTemplates" :key="bnf" :value="bnf.value">{{ bnf.description }}
                                </option>
                            </CFormSelect>
                            <!-- <br /> -->
                        </CCol>
                        <CCol sm="7">
                            <!-- Content -->
                            <CFormTextarea style="color: rgb(1, 108, 157)">
                                {{ selectedBNFTemplate }}
                            </CFormTextarea>
                        </CCol>
                        <!-- <p>filtered: {{ filteredTemplates.length }} / templates: {{ templates.length }}</p> -->
                    </CRow>
                    <!-- 
                Size of chromosomes 
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ sizeChromosome.description }}</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="1" :max="20" :step="1" v-model="selectedSizeChromosome" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ selectedSizeChromosome }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Max Wrapping
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ maxWrap.description }}</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="1" :max="100" :step="1" v-model="selectedMaxWrap" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ selectedMaxWrap }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!--
                Population size
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ populationSize.description }}</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="10" :max="1000" :step="10" v-model="selectedPopulationSize" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ selectedPopulationSize }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Total effort
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ effort.description }}</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="minEffort" :max="maxEffort" :step="10" v-model="selectedTotalEffort" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ selectedTotalEffort }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <CAlert color="success">
                        You will perform <b>{{ numberOfGenerations }} generation(s) !</b>
                    </CAlert>
                    <!-- 
                Elite Selection
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>Elite selection rate</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="0.05" :max="1" :step="0.01" v-model="eliteSelectionRate" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ eliteSelectionRate }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Selection: type of selection and proportion 
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ selection.description }}</b></CFormLabel>
                        <CCol sm="3">
                            <CFormSelect aria-label="select-selection" v-model="selectedSelection"
                                feedback-invalid="Please, choose a selection operator" :invalid="selectedSelection == ''">
                                <option selected disabled>Select the required selection</option>
                                <option v-for="selection in selectionType" :key="selection" :value="selection.value">{{
                                    selection.description
                                }}</option>
                            </CFormSelect>
                        </CCol>
                        <CCol sm="6">
                            <CFormRange :min="0.05" :max="1" :step="0.05" v-model="selectionRate" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ selectionRate }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- Tournament size (depending of the selection mod) -->
                    <CRow class="mb-3" v-if="selectedSelection == '4'">
                        <CFormLabel class="col-sm-2 col-form-label"><b>Tournament selection rate</b></CFormLabel>
                        <CCol sm="9">
                            <CFormRange :min="0.05" :max="1" :step="0.05" v-model="tournamentSelectionRate" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ tournamentSelectionRate }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Crossover: type of crossover and proportion 
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ crossover.description }}</b></CFormLabel>
                        <CCol sm="3">
                            <CFormSelect aria-label="select-crossover" v-model="selectedCrossover"
                                feedback-invalid="Please, choose a crossover operator" :invalid="selectedCrossover == ''">
                                <option selected disabled>Select the required crossover</option>
                                <option v-for="crossover in crossoverType" :key="crossover" :value="crossover.value">{{
                                    crossover.description
                                }}</option>
                            </CFormSelect>
                        </CCol>
                        <CCol sm="6">
                            <CFormRange :min="0" :max="1" :step="0.05" v-model="crossoverRate" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ crossoverRate }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Mutation: type of mutation and proportion 
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>{{ mutation.description }}</b></CFormLabel>
                        <CCol sm="3">
                            <CFormSelect aria-label="select-mutation" v-model="selectedMutation"
                                feedback-invalid="Please, choose a mutation operator" :invalid="selectedMutation == ''">
                                <option selected disabled>Select the required mutation</option>
                                <option v-for="mutation in mutationType" :key="mutation" :value="mutation.value">{{
                                    mutation.description
                                }}</option>
                            </CFormSelect>
                        </CCol>
                        <CCol sm="6">
                            <CFormRange :min="0" :max="1" :step="0.01" v-model="mutationRate" />
                        </CCol>
                        <CCol sm="1">
                            <CFormLabel class="col-form-label"><b>{{ mutationRate }}</b></CFormLabel>
                        </CCol>
                    </CRow>
                    <!-- 
                Crowding Method - Novelty Search
            -->
                    <CRow class="mb-3">
                        <CFormLabel class="col-sm-2 col-form-label"><b>Some Optimizers</b></CFormLabel>
                        <CCol sm="5">
                            <CFormSwitch :label="crowding.description" v-model="enableCrowding" />
                        </CCol>
                        <CCol sm="5">
                            <CFormSwitch :label="noveltySearch.description" v-model="enableNoveltySearch"
                                :disabled="!selectedFeature.includes('ra')" />
                        </CCol>
                    </CRow>
                </CCardBody>
            </CCard>
        </div>

        <div v-if="selectedFeature.includes('sf') || selectedFeature.includes('af')">
            <CCard>
                <CCardTitle class="text-center">Entities assessment</CCardTitle>
                <CCardBody>
                    <!-- 
                Evaluator Mod 
            -->
                    <div v-if="selectedFeature.includes('sf')">

                        <!-- 
                    Probabilistic SHACL
                -->
                        <CRow class="mb-3">
                            <CFormLabel class="col-sm-2 col-form-label"><b>Probabilistic SHACL</b></CFormLabel>
                            <CCol sm="2">
                                <!-- Alpha -->
                                <CFormLabel>{{ shaclAlpha.description }}</CFormLabel>
                            </CCol>
                            <CCol sm="2">
                                <!-- Alpha -->
                                <CFormSelect v-model="selectedShaclAlpha" required :disabled="selectedShaclProb == 0">
                                    <option selected disabled>Select the required significance level</option>
                                    <option v-for="alpha in alphaValues" :key="alpha" :value="alpha.value">{{
                                        alpha.description
                                    }}</option>
                                </CFormSelect>
                            </CCol>
                            <CCol sm="1">
                                <!-- p-value -->
                                <CFormLabel>{{ shaclProb.description }}</CFormLabel>
                            </CCol>
                            <CCol sm="4">
                                <CFormRange :min="0" :max="1" :step="0.05" v-model="selectedShaclProb" />
                            </CCol>
                            <CCol sm="1">
                                <CFormLabel class="col-form-label"><b>{{ selectedShaclProb }}</b></CFormLabel>
                            </CCol>
                        </CRow>
                        <CAlert color="warning" v-if="selectedShaclProb == 0">
                            <b>Standard SHACL validation !</b> increase the <b>P-value</b> to enable the <b>probabilistic
                                SHACL
                                validation</b>
                        </CAlert>
                        <CAlert color="warning" v-else>
                            <b>Probabilistic SHACL validation !</b> set the <b>P-value</b> at 0 to enable the <b>standard
                                SHACL
                                validation</b>
                        </CAlert>
                        <!-- 
                    Shapes file 
                -->
                        <CRow class="mb-3">
                            <CFormLabel class="col-sm-2 col-form-label"><b>{{ shapesFile.description }}</b></CFormLabel>
                            <CCol sm="10">
                                <input class="col-form-label" type="file" @change="readFile" />
                                <!-- Content -->
                                <CFormTextarea style="color: rgb(1, 108, 157)" v-model="shapes">
                                    {{ shapes }}
                                </CFormTextarea>
                            </CCol>
                        </CRow>
                    </div>
                    <div v-if="selectedFeature.includes('af')">
                        <!-- 
                    OWL axioms file 
                -->
                        <CRow class="mb-3">
                            <CFormLabel class="col-sm-2 col-form-label"><b>{{ axiomsFile.description }}</b></CFormLabel>
                            <CCol sm="10">
                                <input class="col-form-label" type="file" @change="readFile" />
                                <!-- Content -->
                                <CFormTextarea style="color: rgb(1, 108, 157)" v-model="axioms">
                                    {{ axioms }}
                                </CFormTextarea>
                            </CCol>
                        </CRow>
                    </div>
                </CCardBody>
            </CCard>
        </div>
        <!--
            Launch XP
        -->
        <CButton color="success" @click="postProject">Let's go</CButton>
    </CForm>
</template>


<script>
// import { rdfminer } from '../../data/form.json'
import axios from "axios"
import { CCard, CCardBody, CCardTitle, CForm, CRow, CFormLabel, CCol, CFormInput, CFormSelect, CFormTextarea, CFormRange, CAlert, CFormSwitch, CButton } from '@coreui/vue'
import { markRaw } from "vue";
// import { toRaw } from "vue";
import { useCookies } from "vue3-cookies";

export default {
    name: 'CreateProject',
    // props: {
    //     projects: {
    //         type: Object
    //     },
    // },
    components: {
        CCard, CCardBody, CCardTitle, CForm, CRow, CFormLabel, CCol, CFormInput, CFormSelect, CFormTextarea, CFormRange, CAlert, CFormSwitch, CButton
    },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            validated: null,
            //
            // projectToLoad: null,
            // project name 
            directory: {},
            selectedProjectName: "",
            alreadyExist: true,
            errorMessage: "Please, choose a name for your project",
            // features
            features: [],
            selectedFeature: "",
            task: "",
            // prefixes
            prefixes: {},
            prefixesSamples: [],
            selectedPrefixes: "",
            // SPARQL endpoint
            targetSparqlEndpoint: {},
            endpoints: [],
            selectedTargetEndpoint: "",
            // shacl - axioms content
            axiomsFile: {},
            axioms: "",
            shapesFile: {},
            shapes: "",
            // BNF Grammar templates
            grammar: {},
            templates: [],
            filteredTemplates: [],
            selectedBNFTemplate: "",
            // pop size
            populationSize: {},
            selectedPopulationSize: 0,
            // total effort 
            effort: {},
            minEffort: 0,
            maxEffort: 0,
            selectedTotalEffort: -1,
            // ngen
            numberOfGenerations: 200,
            // selection 
            selection: {},
            selectionType: [],
            selectedSelection: 0,
            selectionRate: -1,
            eliteSelectionRate: 0.2,
            tournamentSelectionRate: 0.25,
            // Crossover
            crossover: {},
            crossoverType: [],
            selectedCrossover: 0,
            crossoverRate: -1,
            // Mutation
            mutation: {},
            mutationType: [],
            selectedMutation: 0,
            mutationRate: -1,
            // chromosomes size
            sizeChromosome: {},
            selectedSizeChromosome: -1,
            // max wrapp
            maxWrap: {},
            selectedMaxWrap: -1,
            // crowding
            crowding: {},
            enableCrowding: false,
            // novelty search
            noveltySearch: {},
            enableNoveltySearch: false,
            // shacl: p-value et alpha
            shaclAlpha: {},
            alphaValues: [],
            selectedShaclAlpha: -1,
            shaclProb: {},
            selectedShaclProb: -1
        }
    },
    methods: {
        updateNumberGenerations() {
            this.numberOfGenerations = Math.ceil(this.selectedTotalEffort / this.selectedPopulationSize);
        },
        updateEffort() {
            this.minEffort = this.selectedPopulationSize;
            this.maxEffort = this.selectedPopulationSize * 200;
            this.selectedTotalEffort = this.maxEffort;
            this.updateNumberGenerations();
        },
        getDefaultValue(data) {
            return data.values[0].value;
        },
        readFile(event) {
            const file = event.target.files[0];
            const reader = new FileReader();
            reader.onload = e => {
                if (this.selectedFeature.includes('sf')) {
                    this.shapes = e.target.result;
                } else {
                    this.axioms = e.target.result;
                }
            }
            reader.readAsText(file);
        },
        // setValuesFromExistingProject() {
        //     console.log(this.projectToLoad);
        //     this.selectedFeature = this.projectToLoad.mod;
        //     this.selectedPrefixes = this.projectToLoad.prefixes;
        //     this.selectedTargetEndpoint = this.projectToLoad.targetSparqlEndpoint;
        //     this.task = this.projectToLoad.task;
        //     this.selectedBNFTemplate = this.projectToLoad.settings.bnf;
        //     this.selectedPopulationSize = this.projectToLoad.settings.populationSize;
        //     this.selectedTotalEffort = this.projectToLoad.settings.effort;
        //     this.selectedSizeChromosome = this.projectToLoad.settings.sizeChromosome;
        //     this.selectedMaxWrap = this.projectToLoad.settings.maxWrap;
        //     this.eliteSelectionRate = this.projectToLoad.settings.eliteSelectionRate;
        //     this.tournamentSelectionRate = this.projectToLoad.settings.tournamentSelectionRate;
        //     this.selectedSelection = this.projectToLoad.settings.selectionType;
        //     this.selectionRate = this.projectToLoad.settings.selectionRate;
        //     this.selectedMutation = this.projectToLoad.settings.mutationType;
        //     this.mutationRate = this.projectToLoad.settings.mutationRate;
        //     this.selectedCrossover = this.projectToLoad.settings.crossoverType;
        //     this.crossoverRate = this.projectToLoad.settings.crossoverRate;
        //     this.enableNoveltySearch = this.projectToLoad.settings.noveltySearch;
        //     this.enableCrowding = this.projectToLoad.settings.crowding;
        //     this.selectedShaclAlpha = this.projectToLoad.settings.shaclAlpha;
        //     this.selectedShaclProb = this.projectToLoad.settings.shaclProb;
        //     this.axioms = this.projectToLoad.settings.axioms;
        //     this.shapes = this.projectToLoad.settings.shapes;
        // },
        getForm() {
            return {
                // userId: this.token.userId,
                projectName: this.selectedProjectName,
                mod: this.selectedFeature,
                prefixes: this.selectedPrefixes,
                targetSparqlEndpoint: this.selectedTargetEndpoint,
                task: this.task,
                settings: {
                    bnf: this.selectedBNFTemplate,
                    populationSize: this.selectedPopulationSize,
                    effort: this.selectedTotalEffort,
                    sizeChromosome: this.selectedSizeChromosome,
                    maxWrap: this.selectedMaxWrap,
                    eliteSelectionRate: this.eliteSelectionRate,
                    tournamentSelectionRate: this.tournamentSelectionRate,
                    selectionType: this.selectedSelection,
                    selectionRate: this.selectionRate,
                    mutationType: this.selectedMutation,
                    mutationRate: this.mutationRate,
                    crossoverType: this.selectedCrossover,
                    crossoverRate: this.crossoverRate,
                    noveltySearch: this.enableNoveltySearch,
                    crowding: this.enableCrowding,
                    shaclAlpha: this.selectedShaclAlpha,
                    shaclProb: this.selectedShaclProb,
                    axioms: this.axioms,
                    shapes: this.shapes
                }
            }
        },
        postProject() {
            // set task field
            this.task = this.selectedFeature.includes('ge') ? "Mining" : "Assessment";
            // console.log(this.cmdline);
            // build a request to the API
            axios.post("http://localhost:9200/api/project",
                [
                    this.getForm()
                ],
                {
                    headers: {
                        "x-access-token": this.cookies.get("token")
                    }
                }).then(
                    (response) => {
                        if (response.status === 200) {
                            // console.log("OK !" + response);
                            this.$emit("new", this.getForm().projectName);
                        }
                    }
                ).catch((error) => {
                    console.log(error);
                });
        },
    },
    watch: {
        // projectToLoad() {
        //     console.log(this.projectToLoad)
        //     this.setValuesFromExistingProject();
        // },
        selectedFeature() {
            if (this.selectedFeature.includes("-rs")) {
                // filtering BNF templates with grammars related to SHACL shapes
                this.filteredTemplates = this.templates.filter((bnf) => {
                    return bnf.description.includes("SHACL") == true;
                });
            } else if (this.selectedFeature.includes("-ra")) {
                // filtering BNF templates with grammars related to SHACL shapes
                this.filteredTemplates = this.templates.filter((bnf) => {
                    return bnf.description.includes("OWL") == true;
                });
            }
        },
        selectedTotalEffort() {
            // console.log(this.selectedTotalEffort)
            this.updateNumberGenerations();
        },
        selectedPopulationSize() {
            this.updateEffort();
        },
        selectedProjectName() {
            if (this.selectedProjectName != '') {
                // checking project name 
                // already exists ?
                axios.get("http://localhost:9200/api/project", {
                    headers: { "x-access-token": this.cookies.get("token") },
                    params: { projectName: this.selectedProjectName }
                }).then(
                    (response) => {
                        // console.log(response.data)
                        if (response.status === 200) {
                            if (response.data.length == 0) {
                                // Does not exists ! 
                                this.alreadyExist = false;
                            } else {
                                // error from the server
                                this.alreadyExist = true;
                                this.errorMessage = "This project name already exists !"
                            }
                        }
                    }
                ).catch((error) => {
                    console.log(error);
                });
            } else {
                this.errorMessage = "Please, choose a name for your project"
            }

        }
    },
    // beforeUpdated() {
    //     console.log("updated")
    //     console.log(this.projects)
    // },
    mounted() {
        
        // get params 
        axios.get("http://localhost:9200/api/params").then(
            (response) => {
                const data = response.data[0];
                // Mapping variables
                // directory 
                this.directory = data.projectName;
                // features
                this.features.push(data.axiomsMining, data.axiomsAssessment, data.shapesMining, data.shapesAssessment);
                // prefixes
                this.prefixes = data.prefixes;
                this.prefixesSamples = markRaw(data.prefixes.values);
                // this.selectedPrefixes = this.getDefaultValue(data.prefixes);
                // SPARQL endpoint
                this.targetSparqlEndpoint = data.targetSparqlEndpoint;
                this.endpoints = markRaw(data.targetSparqlEndpoint.values);
                // BNF grammar
                this.grammar = data.grammar;
                this.templates = markRaw(data.grammar.values);
                // population size 
                this.populationSize = data.populationSize;
                this.selectedPopulationSize = this.getDefaultValue(data.populationSize);
                // max effort
                this.effort = data.effort;
                this.selectedTotalEffort = this.getDefaultValue(data.effort);
                // selection
                this.selection = data.selection;
                this.selectionType = markRaw(data.selection.values);
                this.selectionRate = this.getDefaultValue(data.pSelection);
                // crossover
                this.crossover = data.crossover;
                this.crossoverType = markRaw(data.crossover.values);
                this.crossoverRate = this.getDefaultValue(data.pCrossover);
                // mutation
                this.mutation = data.mutation;
                this.mutationType = markRaw(data.mutation.values);
                this.mutationRate = this.getDefaultValue(data.pMutation);
                // chromosome size
                this.sizeChromosome = data.sizeChromosome;
                this.selectedSizeChromosome = this.getDefaultValue(data.sizeChromosome);
                // max wrapp
                this.maxWrap = data.maxWrap;
                this.selectedMaxWrap = this.getDefaultValue(data.maxWrap);
                // optimizers
                this.crowding = data.crowding;
                this.enableCrowding = this.getDefaultValue(data.crowding);
                this.noveltySearch = data.noveltySearch;
                this.enableNoveltySearch = this.getDefaultValue(data.noveltySearch);
                // SHACL: alpha and prob
                this.shaclAlpha = data.shaclAlpha;
                this.alphaValues = markRaw(data.shaclAlpha.values);
                this.selectedShaclAlpha = this.getDefaultValue(data.shaclAlpha);
                this.shaclProb = data.shaclProb;
                this.selectedShaclProb = this.getDefaultValue(data.shaclProb);
                // assessment 
                this.axiomsFile = data.axiomsAssessment;
                this.shapesFile = data.shapesAssessment;
            }
        ).catch((error) => {
            console.log(error);
        });
    },
}
</script>