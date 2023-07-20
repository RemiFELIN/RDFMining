<template>
  <CNavbar expand="lg" color-scheme="light" class="bg-light">
    <CContainer fluid>
      <CNavbarBrand>RDFMiner UI</CNavbarBrand>
      <!-- <CNavbarToggler aria-label="Toggle navigation" aria-expanded={visible} @click="visible = !visible" /> -->
      <CCollapse class="navbar-collapse" :visible="visible">
        <CNavbarNav component="nav">
          <CNavLink><router-link to="/">Home</router-link></CNavLink>
          <CNavLink :disabled="!isAuth"><router-link to="/projects">Projects</router-link></CNavLink>
          <CNavLink><router-link to="/publications">Publications</router-link></CNavLink>
          <!-- <CNavLink class="right"><router-link to="/publications">Log in</router-link></CNavLink> -->
          <CButton type="submit" :color="!isAuth ? 'success' : 'danger'" variant="outline"
            @click="onSubmitClick">
            {{ !isAuth ? "Log In" : "Log Out " }}
          </CButton>
          <CButton v-if="!isAuth" type="submit" color="info" variant="outline"
            @click="onSubscribeClick">Subscribe
          </CButton>
        </CNavbarNav>
      </CCollapse>
    </CContainer>
  </CNavbar>
</template>
  
<script>
import { CNavbar, CContainer, CNavbarBrand, CCollapse, CNavbarNav, CNavLink, CButton } from '@coreui/vue';

export default {
  name: 'CHeader',
  components: {
    CNavbar, CContainer, CNavbarBrand, CCollapse, CNavbarNav, CNavLink, CButton
  },
  props: {
    isAuth: {
      type: Boolean
    }
  },
  // data() {
  //   return {
  //     isLogIn: true,
  //   }
  // },
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
.right {
  position: absolute;
  right: 0;
}
</style>
  