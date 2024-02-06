import axios from "axios";
import { getToken } from "./token";
import { base } from "./env";

export async function get(endpoint, params) {
    console.log(base + "/" + endpoint)
    const r = await axios.get(
        base + "/" + endpoint, 
        { headers: { "x-access-token": getToken() },
        params: params
    }).catch((error) => {
        console.log(error);
    });
    return await process(r);
}

export async function post(endpoint, params, data) {
    const r = await axios.post(base + "/" + endpoint, data, 
        { headers: { "x-access-token": getToken() },
        params: params
    }).catch((error) => {
        console.log(error);
    });
    return await process(r);
}

export async function del(endpoint, params) {
    const r = await axios.delete(base + "/" + endpoint, {
        headers: { "x-access-token": getToken() },
        params: params
    }).catch((error) => {
        console.log(error);
    });
    return await process(r);
}

async function process(r) {
    if (r && r.status === 200) {
        return await r.data;
    } else {
        return null;
    }
}