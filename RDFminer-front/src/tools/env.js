import io from "socket.io-client";

console.log("~~~")
console.log(process.env.NODE_ENV)
console.log("~~~")

export const base = process.env.RDFMINER_FRONT_ENDPOINT
export const socket = io(process.env.RDFMINER_FRONT_SOCKET_ENDPOINT)