import io from "socket.io-client";

console.log("~~~")
console.log(process.env.NODE_ENV)
console.log("~~~")

export const base = process.env.NODE_ENV === "production" ? "http://134.59.130.136" : "http://localhost:8890";
export const socket = process.env.NODE_ENV === "production" ? io("http://134.59.130.136/ws") : io("http://localhost:8891");