<template>
  <CHeader :isAuth="isAuth" @login="toggleLoginPopup" @logout="logout" @subscribe="toggleSubscribePopup"></CHeader>
  <!-- <b>Hello ! connected:{{ isAuth }} and userId:{{ userId }}</b> -->
  <!-- <b style="font-size: 3vw;">Hello {{ userId }} !</b> -->
  <!-- <h1 v-if="!isAuth">Welcome to the RDFMiner UI !</h1> -->
  <!-- <h1 v-else>Hello {{ userId }} ! Nice to see you !</h1> -->
  <div>
    <router-view></router-view>
    <VSubscribe :enable="enableSubscribePopup" @close="toggleSubscribePopup"></VSubscribe>
    <LogIn :enable="enableLoginPopup" @close="toggleLoginPopup" @login="login"></LogIn>
  </div>
  <!-- Footer -->
  <CFooter :style="cfooterStyle" position="fixed">
    <div>
      <!-- <CLink href="https://coreui.io">CoreUI</CLink> -->
      <span>RDFMiner UI&copy; 2023.</span>
    </div>
    <div>
      <CLink href="https://team.inria.fr/wimmics/">WIMMICS</CLink>
      <span> team - Inria, I3S</span>
    </div>
  </CFooter>
</template>

<script>
// IMPORTANT
// CoreUI VueJS: https://coreui.io/vue/docs
import CHeader from './components/Header.vue'
import LogIn from './vues/auth/logIn.vue'
import VSubscribe from './vues/auth/VSubscribe.vue'
import { CFooter, CLink } from '@coreui/vue'

import '@coreui/coreui/dist/css/coreui.min.css'

export default {
  name: 'App',
  components: {
    CHeader,
    LogIn,
    VSubscribe,
    CFooter,
    CLink
  },
  data() {
    return {
      userId: "",
      // token: this.$cookies.get("token"),
      isAuth: false,
      enableLoginPopup: false,
      enableSubscribePopup: false,
      cfooterStyle: {
        '--cui-footer-color': "rgb(0, 63, 127)",
        '--cui-footer-min-height': "1em"
      }
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
      this.$cookies.set("token", data.token);
      this.$cookies.set("id", data.userId);
      // this.token = this.$cookies.get("token");
      this.isAuth = true;
    },
    logout() {
      this.$cookies.remove("token");
      this.$cookies.remove("id");
      // this.token = "",
      this.isAuth = false;
      // this.userId = "";
    }
  },
  mounted() {
    // console.log(this.$cookies.get("token"));
    if(this.$cookies.get("token") != null) {
      this.isAuth = true;
    }
  }
}
</script>

<style>
#app {
  /* font-family: Avenir, Helvetica, Arial, sans-serif; */
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /* text-align: center; */
  color: rgb(0, 63, 127);
  background-color: rgba(255, 255, 255, 0.5);
  /* margin-top: 60px; */
  /* height: 99vh;  */
  overflow: hidden;
}

button {
  /* width: 100%; */
  background-color: #4CAF50;
  color: white;
  /* padding: 20px 20px; */
  margin: 8px 0;
  border-radius: 4px;
  cursor: pointer;
}

.debug {
  color: #4CAF50;
}
</style>
