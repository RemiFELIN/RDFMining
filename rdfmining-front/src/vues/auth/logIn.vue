<template>
    <CModal :visible="enable" alignment="center" scrollable>
        <CModalHeader>
            <CModalTitle>AUTHENTIFICATION</CModalTitle>
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
import axios from "axios"
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
        submit(username, password) {
            // build a request to the API
            axios.get("http://localhost:9200/api/login", {
                params: {
                    username: username,
                    password: password
                }
            }).then(
                (response) => {
                    if (response.status === 200) {
                        console.log(response.data);
                        // this.user = { username, password };
                        // this.username = response.data.username;
                        // emit auth to App
                        this.$emit("login", response.data);
                        // update isConnected status
                        this.isConnected = true;
                        // this.$emit('close');
                    } else if (response.status === 401 || response.status === 500) {
                        this.errorMessage = response.data.message;
                    }
                }
            ).catch((error) => {
                // console.log(error);
                this.errorMessage = error;
            });
        },
        // subscription service
        subscribe() {
            // TODO
        },
    }
}
</script>
  
<style scoped></style>