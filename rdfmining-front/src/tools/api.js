import axios from "axios";
import { getToken } from "./token";

export async function get(url, params) {
    const r = await axios.get(
        url, 
        { headers: { "x-access-token": getToken() },
        params: params
    }).catch((error) => {
        console.log(error);
    });
    return await process(r);
}

export async function post(url, params, data) {
    const r = await axios.post(url, data, 
        { headers: { "x-access-token": getToken() },
        params: params
    }).catch((error) => {
        console.log(error);
    });
    return await process(r);
}

export async function del(url, params) {
    const r = await axios.delete(url, {
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