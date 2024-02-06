const Project = require("../model/project");
const Result = require("../model/results");
const Settings = require("../model/settings");
const Status = require("../model/status");
const Directory = require('../tools/directory');
const queue = require("../servers/queue");
const builder = require("../docker/builder");
// const Experience = require("../model/experience");
// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
const auth = require("../auth/auth");
const socket = require("../servers/express");

async function create(req, res) {
    logger.info("### createProject ###");
    const decoded = auth.verify(req, res);
    if (decoded != null) {
        // console.log(req.body);
        logger.info(decoded.id + " will create a project !");
        const project = new Project();
        // set general settings
        project.userId = decoded.id;
        const body = req.body;
        project.projectName = body.projectName;
        project.mod = body.mod;
        project.status = Status.PENDING;
        project.prefixes = body.prefixes;
        project.task = body.task;
        project.targetSparqlEndpoint = body.targetSparqlEndpoint;
        project.settings = new Settings(body.settings);
        // push into db
        project.save().then((data) => {
            if (data) {
                // console.log(data);
                logger.info("[" + project.projectName + "] Project created (" + data._id + ")");
                // Queue system: automatically manage the project launching
                // adding job
                const dockerData = builder.constructDockerCmdFromData(data);
                // adding on queue
                queue.addJob({
                    project: data,
                    cmd: dockerData.cmd,
                    output: dockerData.output
                });
                // job submitted to the queue
                return res.status(200).send(data);
            }
        }).catch((error) => {
            res.send(error);
        });
    }
}

function deleteProject(req, res) {
    logger.info("### deleteProject ###");
    const decoded = auth.verify(req, res);
    if (decoded != null) {
        Project.deleteOne({ userId: decoded.id, projectName: req.query.projectName }).then((data) => {
            logger.info("delete results related to this project (if exists)...");
            Result.deleteOne({ userId: decoded.id, projectName: req.query.projectName }).then((data) => {
                if (data) logger.info("results deleted !");
            });
            socket.io.emit("deleteProject", data);
            Directory.deleteProject(decoded.id, req.query.projectName);
            res.status(200).send(true);
            // console.log("/project/delete: " + data);
            logger.info("project '" + req.query.projectName + "' deleted !");
        }).catch((error) => {
            res.status(500).send(error);
        });
    }
}

function getProjectsByUser(req, res) {
    logger.info("### getProjectsByUser ###");
    const decoded = auth.verify(req, res);
    if (decoded != null) {
        Project.find({ userId: decoded.id }).then((projects) => {
            // console.log("get projects by userId:" + req.query.userId);
            res.status(200).send(projects);
        }).catch((error) => {
            res.send(error);
        });
    }
}

function getProjectByNameAndUser(req, res) {
    logger.info("### getProjectByNameAndUser ###");
    const decoded = auth.verify(req, res);
    if (decoded != null) {
        Project.find({ userId: decoded.id, projectName: req.query.projectName }).then((project) => {
            res.status(200).send(project);
            // console.log(project);
        }).catch((error) => {
            res.send(error);
        });
    }
}

module.exports = { create, deleteProject, getProjectsByUser, getProjectByNameAndUser }