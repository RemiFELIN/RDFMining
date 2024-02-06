const mongoose = require('mongoose');
const Schema = mongoose.Schema;
// const Distinction = require('./distinction').schema;

const PublicationSchema = Schema({
    link: String,
    distinction: Object,
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