// REQUIREMENT 
// apt-get install redis
// logger
const Logger = require("./tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
// import Express JS 
// providing the app and server
const express = require("./servers/express.js");
// Launch Mongooose
const mongoose = require("./servers/mongoose.js");
// Launch Queue system
const queue = require("./servers/queue.js");

// REST API SERVICES
const prefix = "/api/"
// authentification
const users = require("./auth/users.js");
// projects
const project = require("./services/project.js");
// publications
const publications = require("./services/publications.js");
// params
const params = require("./services/params.js");
// results
const results = require("./services/results.js");
// logs 
const logs = require("./services/logs.js");

// clean queue 
// Queue.cleanQueue();

// Routes settings
express.app.route(prefix + "login").get(users.login);
express.app.route(prefix + "logout").get(users.logout);
express.app.route(prefix + "auth").post(users.register);
express.app.route(prefix + "user").get(users.isExists);
express.app.route(prefix + "params").get(params.get);
// app.route(prefix + "spec").get(specifications.get);
express.app.route(prefix + "publications").get(publications.get);
express.app.route(prefix + "project").get(project.getProjectByNameAndUser).post(project.create).delete(project.deleteProject);
express.app.route(prefix + "projects").get(project.getProjectsByUser);
// app.route(prefix + "project").get(project.getProjectByNameAndUser);
express.app.route(prefix + "results").get(results.getResults).post(results.createResults).put(results.update);
express.app.route(prefix + "logs").get(logs.getLog);

// SERVER SETUP
express.server.listen(process.env.RDFMINER_SERVER_PORT, '0.0.0.0', () => {
    logger.info("##########################################");
    logger.info("RDFMiner Server v1.0");
    logger.info(`Example app listening on port ${process.env.RDFMINER_SERVER_PORT}`)
    logger.info("##########################################")
});

// Close Redis service properly on shutdown
process.on('SIGTERM', () => {
    logger.warn("Server closed");
    // close redis as well
    redisServer.close().then(() => {
        logger.warn("Closing Redis server ...");
    });
    process.exit(err ? 1 : 0);
});