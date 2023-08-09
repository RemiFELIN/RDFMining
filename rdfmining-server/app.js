// import * as compose from 'docker-compose'
const logger = require("./tools/logger");
const express = require('express');
let bodyParser = require('body-parser');
const socketIO = require("socket.io");
const app = express()
const port = 3000

const server = require("http").Server(app);

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
        // console.log("MongoDB Cluster> CONNECTED");
        logger.info("Connected to the cluster !")
    },
        err => {
            // console.log('MongoDB Cluster> ERROR', err);
            logger.error("Error during the connection to the cluster");
            logger.error(err);
        }
    );

// REST API SERVICES
const prefix = "/api/"
// authentification
const users = require("./services/users");
// launcher
const project = require("./services/project");
// publications
const publications = require("./services/publications");
// specifications
const specifications = require("./services/specifications");
// params
const params = require("./services/params");

// SOCKET IO
const io = socketIO(server, {
    cors: {
        origin: "*",
        methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
        allowedHeaders: ["Origin", "X-Requested-With", "Content-Type", "Accept", "X-Access-Token"],
    },
});

// Pour accepter les connexions cross-domain (CORS)
app.use((req, res, next) => {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Access-Token");
    res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    req.io = io;
    next();
});


// Routes settings
app.route(prefix + "auth").get(users.login);
app.route(prefix + "user").get(users.getUser).post(users.createUser);
app.route(prefix + "spec").get(specifications.get);
app.route(prefix + "publications").get(publications.getAll);
app.route(prefix + "project/setup").post(project.createProject);
app.route(prefix + "project/delete").post(project.deleteProject);
app.route(prefix + "params").get(params.get);
// Real-time services
app.route(prefix + "projects").get(project.getProjectsByUser);
app.route(prefix + "project").get(project.getProjectByNameAndUser);

io.on("connection", (socket) => {
    // console.log("connected !");
    logger.info("socket.io - connection");
});

// SERVER SETUP
server.listen(port, () => {
    logger.info("##########################################");
    logger.info("RDFMiner Server v1.0");
    logger.info(`Example app listening on port ${port}`)
    logger.info("##########################################")
})

