let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let ProjectSchema = Schema({
    userId: String,
    projectName: String,
    mod: String,
    status: Number,
    prefixes: String,
    targetSparqlEndpoint: String,
    task: String,
    settings: Object
});

module.exports = mongoose.model('projects', ProjectSchema);