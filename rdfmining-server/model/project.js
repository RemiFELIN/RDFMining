let mongoose = require('mongoose');
let Params = require('./cmd/params').schema;
let Schema = mongoose.Schema;

let ProjectSchema = Schema({
    userId: String,
    projectName: String,
    command: String,
    mod: String,
    params: Params,
    status: Number
});

module.exports = mongoose.model('projects', ProjectSchema);