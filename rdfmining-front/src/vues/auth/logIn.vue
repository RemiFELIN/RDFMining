<template>
    <CModal :visible="enable" alignment="center" scrollable>
        <CModalHeader>
            <CModalTitle>Authentification</CModalTitle>
        </CModalHeader>
        <CModalBody>
            <CForm class="row gx-3 gy-2 align-items-center">
                <!-- USERNAME -->
                <CRow class="mb-3">
                    <CFormInput type="email" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Your username" label="Username" v-model="username" />
                </CRow>
                <!-- PASSWORD -->
                <CRow class="mb-3">
                    <CFormInput type="password" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Your password" label="Password" v-model="password" />
                </CRow>
            </CForm>
            <!-- ALERT -->
            <CAlert :visible="isConnected" color="success" class="d-flex align-items-center">
                <CIcon class="flex-shrink-0 me-2" width="24" height="24" />
                <div>
                    You are connected ! Welcome {{ username }} !
                </div>
            </CAlert>
            <!-- Wrong username/password; error from server -->
            <CAlert :visible="errorMessage != ''" color="danger" class="d-flex align-items-center">
                <!-- <CIcon class="flex-shrink-0 me-2" width="24" height="24" /> -->
                <div>
                    {{ errorMessage }}
                </div>
            </CAlert>
        </CModalBody>
        <CModalFooter>
            <CButton v-if="!isConnected" color="success" :disabled="username == '' || password == ''" @click="submit(username, password)">Submit
            </CButton>
            <CButton color="danger" @click="close">Close</CButton>
        </CModalFooter>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import { get } from "@/tools/api";
import { CButton, CForm, CFormInput, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CAlert } from "@coreui/vue";

export default {
    name: 'LogIn',
    components: {
        CButton, CFormInput, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CForm, CAlert
    },
    data() {
        return {
            username: "",
            password: "",
            success: false,
            isConnected: false,
            errorMessage: "",
        };
    },
    props: {
        enable: {
            type: Boolean
        }
    },
    methods: {
        close() {
            this.username = "",
            this.password = "",
            this.success = false,
            this.isConnected = false,
            this.errorMessage = "",
            this.$emit('close');
        },
        // Connection service
        async submit(username, password) {
            const user = await get("api/login", { 
                username: username,
                password: password
            });
            if (user) {
                // emit auth to App
                this.$emit("login", user);
                // update isConnected status
                this.isConnected = true;
            } else {
                this.errorMessage = "This user does not exist...";
            }
        }
    }
}
</script>
  
<style scoped></style>