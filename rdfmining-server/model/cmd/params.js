let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let ParamsSchema = Schema({
    mod: String,
    outputFolder: String,
    populationSize: Number,
    kBase: Number,
    lenChromosome: Number,
    maxWrapp: Number,
    typeSelection: Number,
    typeMutation: Number,
    typeCrossover: Number,
    noveltySearch: Boolean,
    crowding: Boolean
});

module.exports = mongoose.model('params', ParamsSchema);