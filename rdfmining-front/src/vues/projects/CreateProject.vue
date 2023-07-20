<template>
    <div class="container">
        <!-- <h1>It's time to start your own projects with RDFMiner !</h1> -->
        <form>
            <!-- Project Name -->
            <div class="row">
                <div class="col-25">
                    <label for="pname">Name of the project</label>
                </div>
                <div class="col-75">
                    <input type="text" id="pname" name="projectName" placeholder="MyProject ; my_project ; my-project ; ..."
                        v-model="outputFolder">
                </div>
            </div>
            <CSelect :description="'Mod'" :values="features" @selectValueChanged="updateMod"></CSelect>
            <!-- FOR GRAMMATICAL EVOLUTION of OWL Axioms -->
            <div v-if="mod.includes('-ge')">
                <!-- Provide BNF Grammar file -->
                <BNFGrammar v-if="mod.includes('-ra')" :type="'AXIOM'" @textarea="updateBNFSample"></BNFGrammar>
                <BNFGrammar v-if="mod.includes('-rs')" :type="'SHACL'" @textarea="updateBNFSample"></BNFGrammar>
                <!-- Population size -->
                <CTextNumber :description="'Population Size (min: 10)'" :defaultValue="populationSize" :min="10"
                    @textNumberChanged="updatePopSize"></CTextNumber>
                <!-- Total effort -->
                <CTextNumber :description="'Total effort'" :defaultValue="totalEffort" :min="0"
                    @textNumberChanged="updateTotalEffort"></CTextNumber>
                <!-- N GENERATIONS -->
                <!-- <b>This will give you a number of {{ numberOfGenerations }} generations !</b> -->
                <!-- Size of chromosomes -->
                <CTextNumber :description="'Chromosome Size'" :defaultValue="chromSize" :min="1"
                    @textNumberChanged="updateChromSize"></CTextNumber>
                <!-- Max wrapping -->
                <CTextNumber :description="'Max Wrap'" :defaultValue="maxWrap" :min="1" @textNumberChanged="updateMaxWrap">
                </CTextNumber>
                <!-- Selection: type of selection and proportion -->
                <CSelect :description="'Selection Type'" :defaultValue="choosenSelection" :values="typeSelection"
                    @selectValueChanged="updateTypeSelection"></CSelect>
                <CSlider :description="'Selected Individuals Proportion'" :defaultValue="selectionRate"
                    @pValue="updateSelectionRate"></CSlider>
                <!-- Crossover: type of crossover and probability -->
                <CSelect :description="'Crossover Type'" :defaultValue="choosenCrossover" :values="typeCrossover"
                    @selectValueChanged="updateTypeCrossover"></CSelect>
                <CSlider :description="'P(Crossover)'" :defaultValue="pCrossValue" @pValue="updatePCrossValue"></CSlider>
                <!-- Mutation: type of mutation and probability -->
                <CSelect :description="'Mutation Type'" :defaultValue="choosenMutation" :values="typeMutation"
                    @selectValueChanged="updateTypeMutation"></CSelect>
                <CSlider :description="'P(Mutation)'" :defaultValue="pMutValue" @pValue="updatePMutValue"></CSlider>
                <!-- Enable Crowding method -->
                <CCheckbox :description="'Enable Crowding Method ?'" :defaultValue="false"
                    @checkboxChanged="updateEnableCrowding"></CCheckbox>
                <!-- Enable Novelty Search -->
                <CCheckbox :description="'Enable Novelty Search ?'" :defaultValue="false"
                    @checkboxChanged="updateEnableNoveltySearch"></CCheckbox>
                <!-- Timeout: SPARQL timeout and/or timecap -->
            </div>
            <div class="row">
                <!-- <button type="button" v-on:click="generateCommandLine">Generate cmdline</button> -->
                <button type="button" v-on:click="postProject">Generate cmdline</button>
            </div>
        </form>
    </div>
    <!-- <b>La commande: {{ cmdline }}</b> -->
    <!-- <p>Mod: {{ mod }}</p> -->
</template>


<script>
// import { ref } from 'vue';
import { rdfminer } from '../../data/form.json'
import CSlider from '@/components/form/Slider.vue';
import BNFGrammar from '@/components/form/BNFGrammar.vue';
import CTextNumber from '@/components/form/TextNumber.vue';
import CSelect from '@/components/form/Select.vue';
import CCheckbox from '@/components/form/Checkbox.vue';
import axios from "axios"

export default {
    name: 'CreateProject',
    props: {
        id: {
            type: String
        },
    },
    components: {
        CSlider,
        BNFGrammar,
        CTextNumber,
        CSelect,
        CCheckbox
    },
    data() {
        return {
            // map json
            features: [],
            // cmdline of the experiment
            cmdline: "",
            cmdlineBase: "docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh ",
            // params json
            params: {},
            outputFolder: "",
            mod: "",
            modKey: "",
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
            this.features = rdfminer.features;
            this.typeSelection = rdfminer.typeSelection;
            this.typeCrossover = rdfminer.typeCrossover;
            this.typeMutation = rdfminer.typeMutation;
        }
    },
    methods: {
        updateMod(data) {
            this.mod = data.value;
            this.modKey = data.description;
        },
        updatePopSize(populationSize) {
            this.populationSize = populationSize;
            this.updateNumberOfGenerations();
            // console.log("popSize= " + this.populationSize);
        },
        updateTotalEffort(totalEffort) {
            this.totalEffort = totalEffort;
            this.updateNumberOfGenerations();
        },
        updateChromSize(chromSize) {
            this.chromSize = chromSize;
        },
        updateMaxWrap(maxWrap) {
            this.maxWrap = maxWrap;
        },
        updateTypeSelection(type) {
            this.choosenSelection = type;
        },
        updateSelectionRate(selectionRate) {
            this.selectionRate = selectionRate;
        },
        updateTypeCrossover(choosenCrossover) {
            this.choosenCrossover = choosenCrossover;
        },
        updatePCrossValue(pCrossValue) {
            this.pCrossValue = pCrossValue;
        },
        updateTypeMutation(choosenMutation) {
            this.choosenMutation = choosenMutation;
        },
        updatePMutValue(pMutValue) {
            this.pMutValue = pMutValue;
        },
        updateEnableCrowding(enableCrowding) {
            this.enableCrowding = enableCrowding;
        },
        updateEnableNoveltySearch(enableNoveltySearch) {
            this.enableNoveltySearch = enableNoveltySearch;
        },
        updateBNFSample(bnfContent) {
            // console.log("New value into Experiences.vue: " + sample);
            this.bnfContent = bnfContent;
        },
        updateNumberOfGenerations() {
            // console.log("i: " + this.populationSize)
            // console.log("j: " + this.totalEffort)
            this.numberOfGenerations = Math.floor(this.totalEffort / this.populationSize);
        },
        getProjectName() {
            // if the project name is not defined 
            if (this.outputFolder == "") {
                this.outputFolder = "MyProject"
            }
            return " -dir " + this.outputFolder;
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
                outputFolder: this.outputFolder,
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
                projectName: this.outputFolder,
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


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
/* Style inputs, select elements and textareas */
* {
    font-size: 1.1em;
}

input[type=text],
select,
textarea {
    width: 100%;
    padding: 12px;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
    resize: vertical;
}

/* Style the label to display next to the inputs */
label {
    padding: 12px 12px 12px 0;
    display: inline-block;
}

/* Style the submit button */
input[type=submit] {
    background-color: #04AA6D;
    color: white;
    padding: 12px 20px;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    float: right;
}

/* Floating column for labels: 25% width */
.col-25 {
    float: left;
    width: 25%;
    margin-top: 6px;
}

/* Floating column for inputs: 75% width */
.col-75 {
    float: left;
    width: 75%;
    margin-top: 6px;
}

/* Clear floats after the columns */
.row:after {
    content: "";
    display: table;
    clear: both;
}

/* Responsive layout - when the screen is less than 600px wide, make the two columns stack on top of each other instead of next to each other */
@media screen and (max-width: 600px) {
    .col-25,
    .col-75,
    input[type=submit] {
        width: 100%;
        margin-top: 0;
    }
}</style>