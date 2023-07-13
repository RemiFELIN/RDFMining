let mongoose = require('mongoose');
let Schema = mongoose.Schema;
let Distinction = require('./distinction').schema;

let PublicationSchema = Schema({
    link: String,
    distinction: [Distinction],
    title: String,
    authors: [String],
    keywords: [String],
    conferenceTitle: String,
    date: Number,
    place: String,
    abstract: String,
    citation: String
});

// C'est à travers ce modèle Mongoose qu'on pourra faire le CRUD
module.exports = mongoose.model('publications', PublicationSchema);