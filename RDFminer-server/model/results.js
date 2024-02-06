const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const ResultsSchema = Schema({
    userId: String,
    projectName: String,
    entities: Array,
    nEntities: Number,
    statistics: Object
});

// C'est à travers ce modèle Mongoose qu'on pourra faire le CRUD
module.exports = mongoose.model('results', ResultsSchema);