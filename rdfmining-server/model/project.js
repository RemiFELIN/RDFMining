let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let ProjectSchema = Schema({
    id: String,
    username: String,
    command: String
});

module.exports = mongoose.model('projects', ProjectSchema);