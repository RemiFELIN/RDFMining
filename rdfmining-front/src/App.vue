<template>
  <CHeader :isAuth="isAuth"></CHeader>
  <!-- <b>Hello ! connected:{{ isAuth }} and username:{{ username }}</b> -->
  <!-- <b style="font-size: 3vw;">Hello {{ username }} !</b> -->
  <h1 v-if="!isAuth">Welcome to the RDFMiner UI !</h1>
  <h1 v-else>Hello {{ username }}</h1>
  <div style="height: 86vh;">
    <router-view :username="username"></router-view>
    <AuthComponent :enable="enablePopup" @close="togglePopup" @auth-conf="updateStatus"></AuthComponent>
  </div>
  <!-- Connection as footer -->
  <footer>
    <button v-if="!isAuth" @click="togglePopup">Log in</button>
    <button v-else class="disconnect" @click="logout">Log out</button>
  </footer>
</template>

<script>
import CHeader from './components/Header.vue'
import AuthComponent from './vues/auth/AuthComponent.vue';

export default {
  name: 'App',
  components: {
    CHeader,
    AuthComponent
  },
  data() {
    return {
      username: "",
      isAuth: false,
      enablePopup: false,
    }
  },
  methods: {
    togglePopup() {
      this.enablePopup = !this.enablePopup;
    },
    updateStatus(data) {
      this.isAuth = data.isAuth;
      this.username = data.username;
    },
    logout() {
      this.isAuth = false;
      this.username = "";
    }
  }
  // props: {
  //   username: {
  //     type: String
  //   }
  // }
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
