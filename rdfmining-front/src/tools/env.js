var nodeEnv = process.env.NODE_ENV || "development";
console.log("~~~")
console.log(nodeEnv)
console.log("~~~")
let endpoint = "";

if (nodeEnv === "production") {
    endpoint =  "http://172.19.0.6:9200/";
} else if (nodeEnv === "development") {
    endpoint =  "http://localhost:9200/";
}

export const base = endpoint;
