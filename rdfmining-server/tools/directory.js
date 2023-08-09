const fs = require('fs');
const logger = require('../tools/logger');
const path = require('path');
const Global = require('../docker/global');

// let usersPath = path.resolve("/home/rfelin/projects/RDFMining/IO/users/");

function createFile(path, content) {
    fs.appendFileSync(path, content);
    logger.info("Creating file: " + path);
}

function createFolder(path) {
    fs.mkdirSync(path, { recursive: true });
    logger.info("Creating folder: " + path);
}

function deleteProject(user, projectName) {
    let projectPath = Global.PATH_USERS + "/" + user + "/" + projectName;
    fs.rmSync(projectPath, { recursive: true, force: true });
}

module.exports = { createFile, createFolder, deleteProject }