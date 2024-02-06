// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
// Mongoose
const mongoose = require('mongoose');
mongoose.Promise = global.Promise;

logger.info("connection to MongoDB...");

// MONGOOSE
// Connection
// "mongodb://172.19.0.7:27017/rdfmining-db"
mongoose.connect(process.env.RDFMINER_SERVER_MONGODB_CONNECTION).then(() => {
        logger.info("MongoDB    : Connected !")
    },
    err => {
        logger.error(err);
    }
);

module.exports = mongoose;
