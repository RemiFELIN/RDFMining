<template>
  <!-- OWL Axioms type and BNF Grammar -->
  <div class="col-25">
    <label>Kind of {{ description }} to extract</label>
  </div>
  <div class="col-75">
    <select v-model="choosenSample">
      <option disabled value="">Select a preset</option>
      <option v-for="preset in grammarPreset" :key="preset" :value="preset.content">{{ preset.key }}</option>
    </select>
  </div>
  <!-- TEXT AREA -->
  <div v-if="choosenSample != 'TODO'">
    <div class="col-25">
      <label>BNF Grammar</label>
    </div>
    <div class="col-75">
      <Codemirror 
        style="height:500px" 
        :extensions="extension"
        placeholder="Write your text here" 
        v-model="choosenSample"
        @change="emitEventChanged">
      </Codemirror>
    </div>
  </div>
</template>


<script>
import { rdfminer } from '../../data/form.json';
import { Codemirror } from 'vue-codemirror';
import { basicSetup } from 'codemirror';
import { javascript } from '@codemirror/lang-javascript';

export default {
  name: 'BNFGrammar',
  components: {
    Codemirror
  },
  props: {
    type: String
  },
  setup() {
    const extensions = [javascript(), basicSetup];
    return { extensions }
  },
  data() {
    return {
      value: this.defaultValue,
      description: "",
      choosenSample: "TODO",
      grammarPreset: []
    }
  },
  mounted() {
    if (rdfminer) {
      if (this.type == "AXIOM") {
        this.grammarPreset = rdfminer.axiomsGrammar;
        this.description = "OWL Axioms"
      } else if (this.type == "SHACL") {
        this.grammarPreset = rdfminer.SHACLGrammar;
        this.description = "SHACL Shape"
      }
    }
  },
  methods: {
    emitEventChanged() {
      console.log(this.value);
      this.$emit('textarea', this.choosenSample);
    }
  }
}
</script>


<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
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

/* Responsive layout - when the screen is less than 600px wide, make the two columns stack on top of each other instead of next to each other */
@media screen and (max-width: 600px) {

  .col-25,
  .col-75,
  input[type=submit] {
    width: 100%;
    margin-top: 0;
  }
}
</style>