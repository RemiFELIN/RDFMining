var jwt = require('jsonwebtoken');
// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));

/**
 * Verify if the token is provided and correct, if true we return the decoded token. Return error otherwise
 * @param {*} req the HTTP request 
 * @param {*} res the HTTP result
 * @returns the decoded token
 */
function verify(req, res) {
    logger.info("Token verification ...");
    // get toker from headers
    // console.log(req.headers);
    var token = req.headers['x-access-token'];
    let result = null;
    // if it does not exists (or not provided), we block the service and return erros 
    if (!token) {
        logger.error('No token provided.');
        console.log(req.headers);
        res.status(401).send({ auth: false, message: 'No token provided.' });
    } else {
        jwt.verify(token, process.env.RDFMINER_SERVER_AUTH_KEY, function (err, decoded) {
            if (err) res.status(500).send({ auth: false, message: 'Failed to authenticate token.' });
            logger.info("verification: success (" + decoded.id + ")");
            result = decoded;
        });
    }
    return result;
}

module.exports = { verify }