// TODO: s'inspirer du backend effectué dans le projet CV-Angular
// https://github.com/RemiFELIN/cv-angular/blob/master/backend/server.js
let users = require("../model/user");

function login(req, res) {    
    users.findOne({ username: req.query.username }).then((user) => {
        console.log(req.query);
        // console.log(req.body);
        // if (err) return res.status(500).send('Error on the server');
        // if the user is not found, return 401 error
        if (!user) return res.status(401).send("Wrong username/password");
        // var isValidPassword = bcrypt.compareSync(req.body.password, user.password);
        if (req.query.password === user.password) {
            console.log("/api/auth : Success !")
            res.status(200).send(user);
        } else {
            res.status(401).send("Wrong username/password");
        }
    });
}

module.exports = { login }