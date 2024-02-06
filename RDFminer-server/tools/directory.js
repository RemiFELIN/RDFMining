const fs = require('fs');
const Global = require('../docker/global');
const shell = require('shelljs');
// logger
const Logger = require("./logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));

// let usersPath = path.resolve("/home/rfelin/projects/RDFMining/IO/users/");

function createFile(path, content) {
    fs.appendFileSync(path, content);
    // logger.info("[*] Creating file: " + path);
}

function createFolder(path) {
    fs.mkdirSync(path, { recursive: true });
    // logger.info("[*] Creating folder: " + path);
}

async function deleteProject(user, projectName) {
    // const projectPath = Global.PATH_USERS + "/" + user + "/" + projectName;
    const command = "docker-compose exec -T rdfminer rm -rf /rdfminer/io/users/" + user + "/" + projectName;
    // fs.rmSync(projectPath, { recursive: true, force: true });
    await new Promise(resolve => shell.exec(command, { silent: true }, (code, stdout, stderr) => {
        logger.info("Deleting project - Exit code: " + code);
        if (code != 0) {
            logger.info(stdout);
            logger.error(stderr);
        }
        resolve();
    }));
}

module.exports = { createFile, createFolder, deleteProject }