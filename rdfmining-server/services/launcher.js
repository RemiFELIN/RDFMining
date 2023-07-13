let Project = require("../model/project");
let Experience = require("../model/experience");

function createProject(req, res) {
    const project = new Project();
    // set params
    project.id = req.body.id;
    project.username = req.body.username;
    project.command = req.body.command;
    // push into db
    project.save().then((data) => {
        if(data) {
            res.status(200).send("Project created !");
            // create an instance of experience
            const experience = new Experience();
            console.log(data);
        } else {
            res.status(401).send("POST PROJECT ERROR");
        }
    });
}

module.exports = { createProject }