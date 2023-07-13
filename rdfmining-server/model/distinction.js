let mongoose = require('mongoose');
let Schema = mongoose.Schema;

let DistinctionSchema = Schema({
    link: String,
    type: String
});

// C'est à travers ce modèle Mongoose qu'on pourra faire le CRUD
module.exports = mongoose.model('distinction', DistinctionSchema);