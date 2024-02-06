const path = require('path');

class Global {

    // command
    static RDFMINER_DOCKER_COMMAND = "docker-compose exec -T rdfminer ./rdfminer/scripts/run.sh -prod ";
    // service
    static SERVICE_RDFMINER = "rdfminer";
    // path
    static RDFMINER_IO_DOCKER = "/rdfminer/io/"
    static PATH_LOG = "logs/";
    static PATH_WS = path.resolve(process.env.RDFMINER_SERVER_WS);
    static PATH_USERS = path.resolve(process.env.RDFMINER_SERVER_WS + "/IO/users/");
    // files
    static FILE_GRAMMAR = "/grammar.bnf";
    static FILE_PREFIXES = "/prefix.txt";
    static FILE_AXIOMS = "/axioms.txt";
    static FILE_SHAPES = "/shapes.ttl";

    static getPath() {
        return this.PATH_LOG + new Date().toISOString().slice(0, 10) + ".log";   
    }

}

module.exports = Global;
