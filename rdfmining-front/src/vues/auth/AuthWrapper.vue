<template>
    <div>
        <App v-if="auth" :username="username"/>
        <LoginForm v-else @auth="login"/>
    </div>
    <p>ETAT: {{ auth }}</p>
</template>
  
<script>
// https://www.synbioz.com/blog/tech/bibliotheque-authentification-vuejs
// https://www.koderhq.com/tutorial/vue/http-axios/
import App from "../../App";
import LoginForm from "./LoginForm";
import axios from "axios"

export default {
    data() {
        return {
            username: "",
            auth: false,
        };
    },

    methods: {
        login({ username, password }) {
            // build a request to the API
            axios.get("http://localhost:3000/api/auth", {
                params: {
                    username,
                    password
                }
            }).then(
                (response) => { 
                    if(response.status === 200) {
                        console.log("OK !");
                        // this.user = { username, password };
                        this.auth = true;
                        this.username = username;
                    }  
                }
            ).catch((error) => {
                console.log(error);
                alert("Incorrect username/password");
            });
        }
    },

    components: { App, LoginForm }
};
</script>