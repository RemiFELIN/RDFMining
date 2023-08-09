const path = require('path');

class Global {

    // command
    static RDFMINER_DOCKER_COMMAND = "docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh";
    // services
    static SERVICE_RDFMINER = "rdfminer";
    // path
    static RDFMINER_IO_DOCKER = "/rdfminer/io/"
    static PATH_LOG = "logs/app.log";
    static PATH_WS = path.resolve("/home/rfelin/projects/RDFMining/");
    static PATH_USERS = path.resolve("/home/rfelin/projects/RDFMining/IO/users/");
    // files
    static FILE_GRAMMAR = "/grammar.bnf";
    static FILE_PREFIXES = "/prefix.txt";
    static FILE_AXIOMS = "/axioms.txt";
    static FILE_SHAPES = "/shapes.ttl";

}

module.exports = Global;
