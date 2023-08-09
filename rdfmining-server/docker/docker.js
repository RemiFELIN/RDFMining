// let compose = require('docker-compose');
// const path = require('path');
const shell = require('shelljs');
const logger = require('../tools/logger');
const Directory = require('../tools/directory');
const Global = require('./global');

// const ws = path.resolve("/home/rfelin/projects/RDFMining/");
// const rdfminer = "rdfminer";

function exec(command, outputPath) {
    // cd into workspace
    shell.cd(Global.PATH_WS);
    // console.log(shell.ls());
    // exec command 
    shell.exec(command, { silent: true }, (code, stdout, stderr) => {
        logger.info(outputPath + ' - Exit code:', code);
        Directory.createFile(outputPath + "/log.log", stdout);
        Directory.createFile(outputPath + "/err.log", stderr);
    });
    // compose.exec(rdfminer, command, { cwd: ws });
}

module.exports = { exec }
