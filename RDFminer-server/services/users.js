const User = require("../model/user");

function login(req, res) {    
    User.findOne({ username: req.query.username }).then((user) => {
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

function getUser(req, res) {
    User.findOne({ username: req.query.username }).then((user) => {
        if (!user) return res.status(200).send();
        else return res.status(200).send(user);
    });
}

function createUser(req, res) {
    console.log(req.body);
    const user = new User();
    user.username = req.body.params.username;
    user.password = req.body.params.password;
    user.save().then((data) => {
        if (data) {
            res.status(200).send("User successfully created !");
        } else {
            res.status(401).send("Back error");
        }
    });
}

module.exports = { login, getUser, createUser }