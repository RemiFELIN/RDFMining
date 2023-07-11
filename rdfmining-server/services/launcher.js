function createProject(req, res) {
    const fs = require('fs');
    // console.log(req.body);
    // get body 
    // const id = req.body.id;
    // const username = req.body.username;
    // const command = req.body.command;
    // add it into db
    // fake db (tmp)
    fs.writeFileSync(__dirname + "/../data/projects.json", JSON.stringify(req.body), err => {
        if (err) {
            console.log('Error writing file', err);
        } else {
            console.log('Successfully wrote file');
        }
    });
    res.status(200).send("Ok !");
}

module.exports = { createProject }