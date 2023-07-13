let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let ProjectSchema = Schema({
    userId: String,
    projectName: String,
    command: String,
    status: Number
});

module.exports = mongoose.model('projects', ProjectSchema);