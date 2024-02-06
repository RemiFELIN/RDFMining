// Express server
const express = require('express');
const bodyParser = require('body-parser');
// socket.io
const socketIO = require("socket.io");

class Express {

    constructor() {
        this.app = express();
        // Form
        this.app.use(bodyParser.urlencoded({ extended: true }));
        this.app.use(bodyParser.json({limit: '500mb'}));
        // CORS
        this.app.use((req, res, next) => {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, X-Access-Token");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            next();
        });
        // init server
        this.server = require("http").Server(this.app);
        // socket.io
        this.io = socketIO(this.server, {
            cors: {
                origin: "*",
                methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
                allowedHeaders: ["Origin", "X-Requested-With", "Content-Type", "Accept", "X-Access-Token"],
            },
        });
    }

}

const server = new Express();

module.exports = server;
