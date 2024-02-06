const Result = require("../model/results");
// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
const auth = require("../auth/auth");
const socket = require("../servers/express");

const resultFile = "results.json"
const shaclReportFile = "shacl_report.ttl"

function getFilePath(path, file) {
    return "../RDFMining/IO/users/" + path + "/" + file;
}

function getResults(req, res) {
    logger.info("### getResults ###");
    const decoded = auth.verify(req, res);
    if (decoded != null) {
        if (req.query.projectName != null) {
            Result.findOne({ userId: decoded.id, projectName: req.query.projectName }).then((result) => {
                // return the result ID
                if (result == null) {
                    return res.status(401).send("Result not found...");
                }
                return res.status(200).send(result._id);
            });
        } else if (req.query.resultsId != null) {
            // Case: only user ID provided
            const query = Result.findById(req.query.resultsId);
            query.exec().then((result) => {
                return res.status(200).send(result);
            }).catch((error) => {
                return res.status(500).send(error);
            });
        } else if (req.query.path != null && req.query.file != null) {
            // return the required file
            if (req.query.file == "results") {
                const logPath = path.resolve(getFilePath(req.query.path, resultFile));
                logger.info("Querying results informations: " + logPath);
                return res.status(200).sendFile(logPath);
            } else if (req.query.file == "shacl") {
                const logPath = path.resolve(getFilePath(req.query.path, shaclReportFile));
                logger.info("Querying SHACL report informations: " + logPath);
                return res.status(200).sendFile(logPath);
            } else {
                logger.warn("Querying SHACL report informations: " + logPath);
                return res.status(401).send("The specified file cannot be downloaded: " + req.query.file);
            }
        } 
    }
}

function createResults(req, res) {
    logger.info("### createResults ###");
    const r = req.body;
    const result = new Result();
    result.userId = r.userId;
    result.projectName = r.projectName;
    result.nEntities = r.nEntities;
    result.entities = r.entities;
    result.statistics = r.statistics;
    result.save().then((data) => {
        if (data) {
            const msg = "[" + r.projectName + "] Result created (" + data._id + ")";
            logger.info(msg);
            socket.io.emit("results-created");
            res.status(200).send(msg);
        }
    }).catch((error) => {
        const msg = "Error during the creation of the result: (" + r.userId + "; " + r.projectName + ")\n" + error;
        res.status(401).send(msg);
        logger.error(msg);
    });
}

function update(req, res) {
    logger.info("### update ###");
    // console.log(req.body);
    // push entities
    if (req.body.entities != null) {
        Result.findOneAndUpdate({ userId: req.body.userId, projectName: req.body.projectName }, { entities: req.body.entities }, { new: true }).then((data) => {
            const msg = "[" + req.body.projectName + "] updating entities (" + data._id + ")";
            logger.info(msg);
            // IO
            socket.io.emit("update-entities", req.body.entities.pop());
            res.status(200).send(msg);
        });
    } else if (req.body.statistics != null) {
        Result.findOneAndUpdate({ userId: req.body.userId, projectName: req.body.projectName }, { statistics: req.body.statistics }, { new: true }).then((data) => {
            const msg = "[" + req.body.projectName + "] updating generations (" + data._id + ")";
            logger.info(msg);
            // IO
            socket.io.emit("update-generation", req.body.statistics.generations.pop());
            res.status(200).send(msg);
        });
    } else {
        logger.error("the received req.body is not valid ...");
        res.status(401).send("the received req.body is not valid ...");
    }
}

module.exports = { getResults, createResults, update }