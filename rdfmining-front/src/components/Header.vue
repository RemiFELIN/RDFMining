<template>
    <div class="navbar">
      <nav>
        <router-link class="nav-link" to="/">Home</router-link>
        <router-link class="nav-link" :class="{ disable: !isAuth }" to="/projects">My projects</router-link>
        <router-link class="nav-link" to="/publications">Publications</router-link>
        <!-- <router-link class="nav-link" to="/visualisation" :disabled="!auth">Visualisations</router-link> -->
        <!-- <router-link class="nav-link" to="/login">Login</router-link> -->
      </nav>
    </div>
</template>
  
<script>
export default {
    name: 'CHeader',
    props: {
      isAuth: {
        type: Boolean
      }
    }
}
</script>
  
<style scoped>
/* Style the navigation menu */
.navbar {
  width: 100%;
  /* height: 100px; */
  background-color: #376b00;
  overflow: auto;
}

/* Navigation links */
.navbar a {
  float: left;
  /* padding: 1px; */
  color: white;
  text-decoration: none;
  font-size: 2vw;
  width: 33.33333%; /* Four equal-width links. If you have two links, use 50%, and 33.33% for three links, etc.. */
  text-align: center; /* If you want the text to be centered */
}

.nav-link {
  position: relative;
}

/* Add a background color on mouse-over */
.nav-link:hover {
  background-color: #77ff00;
  color: #000;
}

/* Style the current/active link */
.router-link-active {
  background-color: #04AA6D;
}

.disable {
  pointer-events: none;
  opacity: 50%;
}

/* Add responsiveness - on screens less than 500px, make the navigation links appear on top of each other, instead of next to each other */
@media screen and (max-width: 500px) {
  .navbar a {
    float: none;
    display: block;
    width: 100%;
    text-align: left; /* If you want the text to be left-aligned on small screens */
  }
}
</style>
  