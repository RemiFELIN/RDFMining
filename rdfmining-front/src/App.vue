<template>
  <CHeader></CHeader>
  <div>
    <router-view></router-view>
  </div>
 
</template>

<script>
import CHeader from './components/Header.vue'

export default {
  name: 'App',
  components: {
    CHeader,
  },
  params: {
    username: {
      type: String
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
  background-color: rgb(230, 230, 230, 0.5);
  /* margin-top: 60px; */
  /* height: 99vh;  */
  overflow: hidden; 
}
</style>
