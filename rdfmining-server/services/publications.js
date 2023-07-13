let publications = require('./../model/publication');

function getAll(req, res){
    publications.find().then((data) => {
        res.status(200).send(data);
    });
}


module.exports = { getAll }