const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const ProjectSchema = Schema({
    userId: String,
    resultsId: String,
    projectName: String,
    mod: String,
    status: Number,
    prefixes: String,
    targetSparqlEndpoint: String,
    task: String,
    settings: Object
});

module.exports = mongoose.model('projects', ProjectSchema);