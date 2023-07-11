<template>
    <input type="username" v-model="username" />
    <input type="password" v-model="password" />
    <button @click="submit">Log in</button>
</template>
  
<script>
export default {
    name: 'LoginForm',
    data() {
        return {
            username: "",
            password: ""
        };
    },
    methods: {
        submit() {
            this.$emit("auth", {
                username: this.username,
                password: this.password
            });
        }
    }
};
</script>