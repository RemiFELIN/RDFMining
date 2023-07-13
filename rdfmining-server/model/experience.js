let mongoose = require('mongoose');
let Schema = mongoose.Schema;

/*
Status:
0: TO DO
1: IN PROGRESS
2: DONE
*/
let ExperienceSchema = Schema({
    id: String,
    status: Number
});

module.exports = mongoose.model('experiences', ExperienceSchema);