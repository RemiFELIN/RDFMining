let Project = require("../model/project");
let Settings = require("../model/settings");

let builder = require("../docker/builder");
let docker = require("../docker/docker");
// let Experience = require("../model/experience");
let logger = require("../tools/logger");

function createProject(req, res) {
    const project = new Project();
    // set general settings
    project.userId = req.body.userId;
    project.projectName = req.body.projectName;
    project.mod = req.body.mod;
    project.status = 0;
    project.prefixes = req.body.prefixes;
    project.task = req.body.task;
    project.targetSparqlEndpoint = req.body.targetSparqlEndpoint;
    project.settings = new Settings(req.body.settings);
    // push into db
    project.save().then((data) => {
        if (data) {
            res.status(200).send("Project created: " + data._id);
            logger.info("Project created: " + data._id);
            // build cmdline for docker 
            let result = builder.constructDockerCmdFromData(data);
            docker.exec(result.cmd, result.output);
            // console.log(data);
            // req.io.emit("newProject", data);
        } else {
            res.status(401).send("POST PROJECT ERROR");
            logger.error("Error during the process 'createProject()'");
        }
    });
}

function deleteProject(req, res) {
    Project.deleteOne({ userId: req.body.userId, projectName: req.body.projectName }).then((data) => {
        res.status(200).send(data._id);
        req.io.emit("deleteProject", data);
        // console.log("/project/delete: " + data);
        logger.info("project " + data._id + " deleted !");
    });
}

function getProjectsByUser(req, res) {
    Project.find({ userId: req.query.id }).then((projects) => {
        // console.log("get projects by userId:" + req.query.userId);
        res.status(200).send(projects);
    }).catch((error) => {
        res.send(error);
    });
}

function getProjectByNameAndUser(req, res) {
    Project.find({ userId: req.query.id, projectName: req.query.projectName }).then((project) => {
        res.status(200).send(project);
        // console.log(project);
    }).catch((error) => {
        res.send(error);
    });
}

module.exports = { createProject, deleteProject, getProjectsByUser, getProjectByNameAndUser }