<template>
  <div class="modal-mask" v-if="enable">
    <div class="modal-wrapper">
      <div class="modal-container">

        <div class="modal-header">
          <slot name="header">
            AUTHENTIFICATION
          </slot>
        </div>

        <div class="modal-body" v-if="!isConnected">
          <slot name="body">
            <label>Username</label>
            <input type="text" v-model="username" />
            <label>Password</label>
            <input type="password" v-model="password" />
          </slot>
        </div>
        <div v-else>
          <label style="text-align: center;">Hello {{ username }}, welcome back !</label>
        </div>

        <div class="modal-footer">
          <slot name="footer">
            <!-- Submit -->
            <button @click="submit(username, password)" v-if="!isConnected">Submit</button>
            <!-- Create an account -->
            <button style="background-color: rgb(183, 183, 183);" v-if="!isConnected">Create an account</button>
            <!-- Close -->
            <button style="background-color: rgb(148, 0, 0);" @click="$emit('close')">Close</button>
          </slot>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
import axios from "axios"

export default {
  name: 'AuthComponent',
  data() {
    return {
      username: "",
      password: "",
      isConnected: false,
    };
  },
  props: {
    enable: {
      type: Boolean
    }
  },
  methods: {
    // Connection service
    submit(username, password) {
      // build a request to the API
      axios.get("http://localhost:3000/api/auth", {
        params: {
          username,
          password
        }
      }).then(
        (response) => {
          if (response.status === 200) {
            console.log(response.data);
            // this.user = { username, password };
            this.auth = true;
            this.username = username;
            // emit auth to App
            this.$emit("auth-conf", {
              isAuth: true,
              username: this.username,
              id: response.data._id
            });
            // update isConnected status
            this.isConnected = true;
          }
        }
      ).catch((error) => {
        console.log(error);
        alert("Incorrect username/password");
      });
    },
    // subscription service
    subscribe() {
      // TODO
    },
  }
}
</script>

<style scoped>
label {
  font-size: 2vw;
}

input,
select {
  width: 100%;
  padding: 12px 20px;
  margin: 8px 0;
  font-size: 2vw;
  display: inline-block;
  border: 1px solid #ccc;
  border-radius: 4px;
  box-sizing: border-box;
}

.modal-mask {
  position: fixed;
  /* z-index: 9998; */
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: table;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  display: table-cell;
  vertical-align: middle;
}

.modal-footer {
  /* right: 0; */
  bottom: 0;
}

.modal-container {
  /* width: 90%; */
  height: 40%;
  text-align: center;
  margin: 0px auto;
  padding: 20px 30px;
  background-color: #fff;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
  flex-direction: column;
  justify-content: space-between;
  display: flex;
  transition: all 0.3s ease;
  font-family: Helvetica, Arial, sans-serif;
}

.modal-header {
  margin-top: 2%;
  font-size: 3vw;
  text-align: center;
  color: green;
}

.modal-body {
  margin: 20px 0;
  display: inline-block;
}
</style>