// import * as compose from 'docker-compose'
const express = require('express')
let bodyParser = require('body-parser');
const app = express()
const port = 3000

// Pour les formulaires
app.use(bodyParser.urlencoded({ extended: true }));
app.use(bodyParser.json());

// Mongoose
const settings = require("./settings.json");
let mongoose = require('mongoose');
mongoose.Promise = global.Promise;
// Connection
mongoose.connect(settings.uri, settings.options)
    .then(() => {
        // console.log("URI = " + uri);
        console.log("MongoDB Cluster> CONNECTED");
    },
        err => {
            console.log('MongoDB Cluster> ERROR', err);
        }
    );

// REST API SERVICES
const prefix = "/api/"
// authentification
const auth = require("./services/authentification");
// launcher
const launcher = require("./services/launcher");
// publications
const publications = require("./services/publications");

// Pour accepter les connexions cross-domain (CORS)
app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Access-Token");
    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    next();
});

// Routes settings
app.route(prefix + "auth").get(auth.login);
app.route(prefix + "experience/setup").post(launcher.createProject);
app.route(prefix + "publications").get(publications.getAll);


// SERVER SETUP
app.listen(port, () => {
    console.log("##########################################");
    console.log("RDFMiner Server v1.0");
    console.log(`Example app listening on port ${port}`)
    console.log("##########################################")
})

// compose.exec