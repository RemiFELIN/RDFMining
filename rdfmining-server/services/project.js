let Project = require("../model/project");
// let Experience = require("../model/experience");

function createProject(req, res) {
    const project = new Project();
    // set params
    project.userId = req.body.userId;
    project.projectName = req.body.projectName;
    project.command = req.body.command;
    project.status = 0;
    // push into db
    project.save().then((data) => {
        if(data) {
            res.status(200).send("Project created !");
            console.log(data);
        } else {
            res.status(401).send("POST PROJECT ERROR");
        }
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

module.exports = { createProject , getProjectsByUser }