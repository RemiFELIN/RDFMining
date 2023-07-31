<template>
  <CHeader :isAuth="isAuth" @login="toggleLoginPopup" @logout="logout" @subscribe="toggleSubscribePopup"></CHeader>
  <!-- <b>Hello ! connected:{{ isAuth }} and username:{{ username }}</b> -->
  <!-- <b style="font-size: 3vw;">Hello {{ username }} !</b> -->
  <!-- <h1 v-if="!isAuth">Welcome to the RDFMiner UI !</h1> -->
  <!-- <h1 v-else>Hello {{ username }} ! Nice to see you !</h1> -->
  <div style="height: 86vh;">
    <router-view :id="id"></router-view>
    <VSubscribe :enable="enableSubscribePopup" @close="toggleSubscribePopup"></VSubscribe>
    <LogIn :enable="enableLoginPopup" @close="toggleLoginPopup" @login="login"></LogIn>
  </div>
</template>

<script>
// IMPORTANT
// CoreUI VueJS: https://coreui.io/vue/docs
import CHeader from './components/Header.vue'
// import AuthComponent from './vues/auth/AuthComponent.vue';
import LogIn from './vues/auth/logIn.vue'
import VSubscribe from './vues/auth/VSubscribe.vue'

import '@coreui/coreui/dist/css/coreui.min.css'

export default {
  name: 'App',
  components: {
    CHeader,
    LogIn,
    VSubscribe
  },
  data() {
    return {
      username: "",
      id: "",
      isAuth: false,
      enableLoginPopup: false,
      enableSubscribePopup: false,
    }
  },
  methods: {
    toggleLoginPopup() {
      this.enableLoginPopup = !this.enableLoginPopup;
    },
    toggleSubscribePopup() {
      this.enableSubscribePopup = !this.enableSubscribePopup;
    },
    login(data) {
      this.id = data.id;
      this.isAuth = data.isAuth;
      this.username = data.username;
    },
    logout() {
      this.id = "",
      this.isAuth = false;
      this.username = "";
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  color: rgb(0, 63, 127);
  background-color: rgba(255, 255, 255, 0.5);
  /* margin-top: 60px; */
  /* height: 99vh;  */
  overflow: hidden;
}

.container {
  overflow-y: auto;
  height: 85vh;
}

.widget {
  background-color: #ffffff;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.27);
}

button {
  width: 100%;
  font-size: 2vw;
  background-color: #4CAF50;
  color: white;
  padding: 20px 20px;
  margin: 8px 0;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

button:hover {
  background-color: #dde9de;
  color: black;
}

.disconnect {
  background-color: #b01313;
}
</style>
