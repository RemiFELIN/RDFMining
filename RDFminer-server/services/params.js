const params = require('../model/params');

function get(req, res){
    params.find().then((data) => {
        res.status(200).send(data);
    });
}

module.exports = { get }