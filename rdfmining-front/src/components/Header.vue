<template>
    <div class="navbar">
      <nav>
        <router-link to="/"              class="nav-link">Home</router-link>
        <router-link to="/experience"    class="nav-link">Experience</router-link>
        <router-link to="/visualisation" class="nav-link">Visualisations</router-link>
        <router-link to="/publications" class="nav-link">Publications</router-link>
      </nav>
    </div>
</template>
  
<script>
export default {
    name: 'CHeader'
}
</script>
  
<style scoped>
/* Style the navigation menu */
.navbar {
  width: 100%;
  background-color: #376b00;
  overflow: auto;
}

/* Navigation links */
.navbar a {
  float: left;
  /* padding: 1px; */
  color: white;
  text-decoration: none;
  font-size: 30px;
  width: 25%; /* Four equal-width links. If you have two links, use 50%, and 33.33% for three links, etc.. */
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
  