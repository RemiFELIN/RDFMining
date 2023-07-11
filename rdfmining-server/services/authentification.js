// TODO: s'inspirer du backend effectué dans le projet CV-Angular
// https://github.com/RemiFELIN/cv-angular/blob/master/backend/server.js
function login(req, res) {
    const db = require("../data/users.json");
    // console.log("Username: " + req.query.username);
    // console.log("Password: " + req.query.password);
    // verification
    let authSuccess = false;
    // checking
    db.users.forEach((user) => {
        if (req.query.username === user.username) {
            if (req.query.password === user.password) {
                // res.status(200).send("Auth Success");
                authSuccess = true;
            }
        }
    });
    if (authSuccess) {
        console.log("/api/auth : Success !")
        res.status(200).send("Auth Success");
    } else {
        console.log("/api/auth : Failed !")
        res.status(401).send("Wrong username/password");
    }
}

module.exports = { login }