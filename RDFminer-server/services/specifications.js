// let swagger = require("./../doc/swagger.json");
// let html = require("./../swagger-html/index.html")
const path = require("path");

// return the swagger.json into folder /doc
function get(req, res) {
    // res.status(200).send(html);
    res.sendFile(path.resolve('swagger-html/index.html'));
}

// function readAPI(req, res) {
//     console.log("__dirname: " + __dirname);
    
// }


module.exports = { get }