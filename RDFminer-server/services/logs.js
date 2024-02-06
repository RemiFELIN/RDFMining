// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));

function getLog(req, res) {
    logger.info("get logs; query: " + JSON.stringify(req.query));
    if (req.query.path != null) {
        logger.info("querying logs informations...");
        const logPath = path.resolve("../IO/users/" + req.query.path + "/log.log");
        logger.info("path: " + logPath);
        res.sendFile(logPath);
    } else {
        res.status(401).send("Error during the logs query");
    }
}

module.exports = { getLog }