<template>
  <CNavbar expand="lg" color-scheme="light" class="bg-light">
    <CContainer fluid>
      <CNavbarBrand style="pointer-events: none;">RDFMiner GUI</CNavbarBrand>
      <!-- <CNavbarToggler aria-label="Toggle navigation" aria-expanded={visible} @click="visible = !visible" /> -->
      <CCollapse class="navbar-collapse" :visible="visible">
        <CNavbarNav component="nav">
          <CNavItem>
            <CNavLink><router-link to="/">Home</router-link></CNavLink>
          </CNavItem>
          <CNavLink :disabled="!isAuth" :style="!isAuth ? 'opacity: 0.3' : ''"><router-link
              to="/projects">Projects</router-link></CNavLink>
          <CNavLink><router-link to="/publications">Publications</router-link></CNavLink>
          <!-- <CNavLink><router-link to="/api">API</router-link></CNavLink> -->
          <CButtonGroup class="right">
            <CButton type="submit" :color="!isAuth ? 'success' : 'danger'" shape="rounded-0"
              @click="onSubmitClick">
              {{ !isAuth ? "Log In" : "Log Out " }}
            </CButton>
            <CButton v-if="!isAuth" type="submit" color="info" shape="rounded-0" @click="onSubscribeClick">
              Subscribe
            </CButton>
          </CButtonGroup>
        </CNavbarNav>
      </CCollapse>
    </CContainer>
  </CNavbar>
</template>
  
<script>
import { CNavbar, CContainer, CNavbarBrand, CCollapse, CNavbarNav, CNavLink, CButton, CButtonGroup, CNavItem } from '@coreui/vue';

export default {
  name: 'CHeader',
  components: {
    CNavbar, CContainer, CNavbarBrand, CCollapse, CNavbarNav, CNavLink, CButton, CNavItem, CButtonGroup
  },
  props: {
    isAuth: {
      type: Boolean
    }
  },
  methods: {
    onSubmitClick() {
      if (!this.isAuth) {
        this.$emit("login");
      } else {
        this.$emit("logout");
      }
    },
    onSubscribeClick() {
      this.$emit("subscribe");
    }
  }
}
</script>
  
<style scoped>
a {
  color: #0060B6;
  text-decoration: none;
}

a:hover {
  color: #00A0C6;
  text-decoration: none;
  cursor: pointer;
}


.right {
  position: absolute;
  right: 0;
  top: 0;
  /* margin-right: 30px; */
  /* margin-left: 30px; */
  /* width: 100px; */
}

/*.right+.right {
  margin-right: 10%;
} */
</style>
  