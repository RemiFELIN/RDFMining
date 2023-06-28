<template>
  <!-- OWL Axioms type and BNF Grammar -->
  <div class="col-25">
    <label>{{ description }}</label>
  </div>
  <div class="col-75">
    <input type="number" :min="min" v-model="value" @change="emitEventChanged">
  </div>
</template>
  
  
<script>
export default {
  name: 'CTextNumber',
  props: {
    description: String,
    defaultValue: Number,
    min: Number
  },
  data() {
    return {
      value: this.defaultValue,
    }
  },
  methods: {
    emitEventChanged() {
      console.log(this.value);
      this.$emit('textNumberChanged', this.value);
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