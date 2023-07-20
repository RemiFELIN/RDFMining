let Project = require("../model/project");
let Params = require("../model/cmd/params");
// let Experience = require("../model/experience");

function createProject(req, res) {
    const project = new Project();
    // set params
    project.userId = req.body.userId;
    project.projectName = req.body.projectName;
    project.command = req.body.command;
    project.mod = req.body.mod;
    project.status = 0;
    project.params = fillParams(req.body.params);
    // push into db
    project.save().then((data) => {
        if (data) {
            res.status(200).send("Project created !");
            console.log(data);
            req.io.emit("newProject", data);
        } else {
            res.status(401).send("POST PROJECT ERROR");
        }
    });
}

function deleteProject(req, res) {
    Project.deleteOne({ userId: req.body.userId, projectName: req.body.projectName }).then((data) => {
        res.status(200).send("Project deleted !");
        req.io.emit("deleteProject", data);
        console.log("/project/delete: " + data);
    });
}

function fillParams(data) {
    const params = new Params();
    // setters
    params.mod = data.mod;
    params.outputFolder = data.outputFolder;
    params.populationSize = data.populationSize;
    params.kBase = data.kBase;
    params.lenChromosome = data.lenChromosome;
    params.maxWrapp = data.maxWrapp;
    params.typeSelection = data.typeSelection;
    params.typeMutation = data.typeMutation;
    params.typeCrossover = data.typeCrossover;
    params.noveltySearch = data.noveltySearch;
    params.crowding = data.crowding;
    console.log(params);
    // return it
    return params;
}

function getProjectsByUser(req, res) {
    Project.find({ userId: req.query.id }).then((projects) => {
        // console.log("get projects by userId:" + req.query.userId);
        res.status(200).send(projects);
    }).catch((error) => {
        res.send(error);
    });
}

module.exports = { createProject, deleteProject, getProjectsByUser }