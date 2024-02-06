const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const ParamsSchema = Schema({
    prefixes: Object,
    grammar: Object,
    targetSparqlEndpoint: Object,
    trainSparqlEndpoint: Object,
    sparqlEndpoint: Object,
    timeCap: Object,
    populationSize: Object,
    pCrossover: Object,
    pMutation: Object,
    pSelection: Object,
    crowding: Object,
    shapesMining: Object,
    shapesAssessment: Object,
    shaclMod: Object,
    shaclAlpha: Object,
    shaclProb: Object,
    axiomsMining: Object,
    axiomsAssessment: Object,
    selection: Object,
    mutation: Object,
    crossover: Object,
    sizeChromosome: Object,
    maxWrap: Object,
    noveltySearch: Object,
    effort: Object,
    projectName: Object
});

module.exports = mongoose.model('params', ParamsSchema);