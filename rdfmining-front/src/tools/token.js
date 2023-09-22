import { useCookies } from "vue3-cookies";

export function getToken() { 
    return useCookies(["token"]).cookies.get("token"); 
}