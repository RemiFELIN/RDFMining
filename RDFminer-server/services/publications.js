const publications = require('./../model/publication');

function get(req, res){
    publications.find().then((data) => {
        res.status(200).send(data);
    });
}


module.exports = { get }