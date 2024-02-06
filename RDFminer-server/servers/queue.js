const settings = require("../settings.json");
// logger
const Logger = require("../tools/logger.js");
const path = require("path");
const logger = Logger.getLogger(path.basename(__filename));
// Redis server
const RedisServer = require('redis-server');
// Bull
const Bull = require("bull");
// launcher
const Docker = require("../docker/docker");

class Queue {

    constructor() {
        // REDIS
        // connection
        const redis = new RedisServer(process.env.RDFMINER_SERVER_REDIS_PORT);
        // open server
        redis.open((err) => {
            if (err === null) {
                logger.info("Redis      : service open !");
            } else {
                logger.warn("Redis      : " + err.message);
            }
        });
        // Init queue system using Bull
        this.bull = new Bull("rdfminer-front-queue", {
            redis: {
                host: 'localhost',
                port: process.env.RDFMINER_SERVER_REDIS_PORT
            },
            limiter: {
                max: settings.maxJobAvalaible,  // Max number of active jobs
                duration: 1                     
            }
        });
        // is ready ?
        this.bull.isReady().then((bull) => {
            if(bull != null) logger.warn("Queue      : Ready (" + bull.name + ")");
        });
        // Process tasks for future jobs
        this.bull.process(async (job) => {
            logger.warn("process the job(" + job.id + ") ...");
            const task = await Docker.exec(job);
            return task;
        });
        // listeners
        this.bull.on("waiting", id => {
            logger.warn("Job(" + id + ") is waiting...");
        });
        this.bull.on("completed", job => {
            logger.warn("Job(" + job.id + ") is completed...");
        });
        this.bull.on("failed", (job, error) => {
            logger.error("Job(" + job.id + ") has been failed: " + error.message);
        });
    }

    async addJob(data) {
        const job = await this.bull.add(data, { delay: 0, removeOnComplete: true });
        logger.warn("The job has been submitted !");
        this.bull.getJobs().then((jobs) => {
            logger.warn("debug: " + jobs.length);
        });
    }

}

const queue = new Queue();

module.exports = queue;