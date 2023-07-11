let Project = require("../model/project");

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
        } else {
            res.status(401).send("POST PROJECT ERROR");
        }
    });
    // const fs = require('fs');
    // fs.writeFileSync(__dirname + "/../data/projects.json", JSON.stringify(req.body), err => {
    //     if (err) {
    //         console.log('Error writing file', err);
    //     } else {
    //         console.log('Successfully wrote file');
    //     }
    // });
    // res.status(200).send("Ok !");
}

module.exports = { createProject }