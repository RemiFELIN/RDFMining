<template>
    <div class="row">
        <div class="col-25">
            <label>{{ description }}</label>
        </div>
        <div class="col-75">
            <select v-model="val" @change="emitEventChanged">
                <option disabled value="">Select a {{ description }}</option>
                <option v-for="value in values" :key="value" :value="value.cmd">{{ value.text }}</option>
            </select>
        </div>
        <!-- <p>{{ values }}</p> -->
        <!-- <p>Selected mod: {{ mod }}</p> -->
    </div>
</template>
    
    
<script>
import { toRaw } from 'vue';

export default {
    name: 'CSelect',
    props: {
        description: String,
        defaultValue: Number,
        values: Object
    },
    data() {
        return {
            val: this.defaultValue,
            // data: this.values,
            key: ""
        }
    },
    methods: {
        emitEventChanged() {
            this.$emit('selectValueChanged', { description: this.getDescription(this.val), value: this.val });
        },
        getDescription(value) {
            let json = toRaw(this.values);
            let res = "";
            Object.keys(json).forEach((key) => {
                if(json[key].cmd === value) {
                    res = json[key].text;
                }
            });
            return res;
        }
    }
}
</script>
    
    
    <!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
* {
    font-size: 1.1em;
}

.row:after {
    content: "";
    display: table;
    clear: both;
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