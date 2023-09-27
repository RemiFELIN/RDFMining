import io from "socket.io-client";

// var nodeEnv = process.env.NODE_ENV || "development";
// console.log("~~~")
// console.log(nodeEnv)
// console.log("~~~")

// let endpoint = "";
// if (nodeEnv === "production") {
//     endpoint =  "http://local:9200/";
// } else if (nodeEnv === "development") {
//     endpoint =  "http://localhost:9200/";
// }

export const base = "http://localhost:9200";
export const socket = io(base);