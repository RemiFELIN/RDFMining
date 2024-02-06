const mongoose = require('mongoose');
const Schema = mongoose.Schema;

/*
Status:
0: TO DO
1: IN PROGRESS
2: DONE
*/
const ExperienceSchema = Schema({
    id: String,
    status: Number
});

module.exports = mongoose.model('experiences', ExperienceSchema);