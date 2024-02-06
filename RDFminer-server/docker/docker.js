// let compose = require('docker-compose');
// const path = require('path');
const shell = require('shelljs');
// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
const Directory = require('../tools/directory');
const Global = require('./global');
const Project = require("../model/project");
const Status = require('../model/status');
const socket = require("../servers/express.js");

// const ws = path.resolve("/home/rfelin/projects/RDFMining/");
// const rdfminer = "rdfminer";

async function exec(job) {
    return new Promise(async (resolve, reject) => {
        try {
            logger.info("[" + job.data.project.projectName + "] Updating project status...");
            // update status
            Project.updateOne(
                { userId: job.data.project.userId, projectName: job.data.project.projectName },
                { status: Status.IN_PROGRESS }
            ).then((data) => {
                logger.info("[" + job.data.project.projectName + "] Done !");
                socket.io.emit("update-status", { projectName: job.data.project.projectName, status: Status.IN_PROGRESS });
            });
            logger.info("[" + job.data.project.projectName + "] Launching RDFMiner service with the following cmd");
            // cd into workspace
            shell.cd(Global.PATH_WS);
            // console.log(shell.ls());
            // exec command 
            await new Promise(resolve => shell.exec(job.data.cmd, { silent: true }, (code, stdout, stderr) => {
                logger.info("[" + job.data.project.projectName + "] Exit code: " + code);
                Directory.createFile(job.data.output + "/log.log", stdout);
                Directory.createFile(job.data.output + "/err.log", stderr);
                // update status
                Project.updateOne(
                    { userId: job.data.project.userId, projectName: job.data.project.projectName },
                    { status: Status.FINISH }).then((data) => {
                        logger.info("[" + job.data.project.projectName + "] Finish !");
                        socket.io.emit("update-status",
                            { projectName: job.data.project.projectName, status: Status.FINISH }
                        );
                    });
                resolve();
            }));
            resolve("completed");
            // compose.exec(rdfminer, command, { cwd: ws });
        } catch (error) {
            reject(error);
        }
    });

}

module.exports = { exec }
