const logger = require('../tools/logger');
const path = require('path');
const Settings = require('../model/settings');
const Directory = require('../tools/directory');
const Global = require('./global');

// path
let fullPath = Global.PATH_USERS;
let directory = "";
let command = Global.RDFMINER_DOCKER_COMMAND;

function getDirectoryArgs(username, dir) {
    // directly linked to the username
    // create the output folder into RDFMiner shared space
    directory = dir;
    fullPath += "/" + username + "/" + dir;
    Directory.createFolder(fullPath);
    return " -user " + username + " -dir " + dir;
}

function getSparqlEndpoint(endpoint) {
    return " -target " + endpoint;
}

function getBNFArg(content) {
    let filePath = path.resolve(fullPath + Global.FILE_GRAMMAR);
    // fs.appendFileSync(filePath, content);
    Directory.createFile(filePath, content);
    logger.info("BNF Grammar saved");
    return " -g " + directory + Global.FILE_GRAMMAR;
}

function getPrefixArg(content) {
    let filePath = path.resolve(fullPath + Global.FILE_PREFIXES);
    // fs.appendFileSync(filePath, content);
    Directory.createFile(filePath, content);
    logger.info("Prefixes saved");
    return " -p " + directory + Global.FILE_PREFIXES;
}

function getAxiomsFileArg(content) {
    let filePath = path.resolve(fullPath + Global.FILE_AXIOMS);
    // fs.appendFileSync(filePath, content);
    Directory.createFile(filePath, content);
    logger.info("OWL Axioms saved");
    return " -af " + directory + Global.FILE_AXIOMS;
}

function getShapesFileArg(content) {
    let filePath = path.resolve(fullPath + Global.FILE_SHAPES);
    // fs.appendFileSync(filePath, content);
    Directory.createFile(filePath, content);
    logger.info("SHACL Shapes saved");
    return " -sf " + directory + Global.FILE_SHAPES;
}

function getPopSizeArg(popSize) {
    return " -ps " + popSize;
}

function getEffortArg(effort) {
    return " -kb " + effort;
}

function getSizeChromosomeArg(sizeChromosome) {
    return " -init " + sizeChromosome;
}

function getMaxWrapArg(maxWrap) {
    return " -mxw " + maxWrap;
}

function getCrossoverArgs(type, rate) {
    return " -cr " + type + " -pc " + rate;
}

function getMutationArgs(type, rate) {
    return " -mu " + type + " -pm " + rate;
}

function getSelectionArgs(type, rate) {
    return " -se " + type + " -sez " + rate;
}

function getSHACLArgs(pvalue, alpha) {
    return " -shacl-p " + pvalue + " -shacl-a " + alpha;
}

function constructDockerCmdFromData(data) {
    // console.log(data);
    // username and projectName
    command += getDirectoryArgs(data.userId, data.projectName);
    // sparql endpoint 
    command += getSparqlEndpoint(data.targetSparqlEndpoint);
    // prefixes
    command += getPrefixArg(data.prefixes);
    // map settings
    const settings = new Settings(data.settings);
    if (data.mod.includes("ge")) {
        // grammatical evolution
        command += " " + data.mod;
        // bnf
        command += getBNFArg(settings.bnf);
        // pop size
        command += getPopSizeArg(settings.populationSize);
        // effort
        command += getEffortArg(settings.effort);
        // size chrom
        command += getSizeChromosomeArg(settings.sizeChromosome);
        // selection 
        command += getSelectionArgs(settings.selectionType, settings.selectionRate);
        // crossover
        command += getCrossoverArgs(settings.crossoverType, settings.crossoverRate);
        // mutation
        command += getMutationArgs(settings.mutationType, settings.mutationRate);
        // max wrap
        command += getMaxWrapArg(settings.maxWrap);
        // Probabilistic SHACL
        if (data.mod.includes('rs')) {
            command += getSHACLArgs(settings.shaclProb, settings.shaclAlpha);
        } else {
            // enable loop (faster !)
            command += " -l ";
        }
    } else {
        if (data.mod.includes("sf")) {
            command += getShapesFileArg(settings.shapes);
            command += getSHACLArgs(settings.shaclProb, settings.shaclAlpha);
        } else {
            command += getAxiomsFileArg(settings.axioms);
        }
    }
    logger.info(command);
    return { cmd: command, output: fullPath };
}


module.exports = { constructDockerCmdFromData }