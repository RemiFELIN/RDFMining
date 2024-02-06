<template>
    <CModal :visible="enable" alignment="center" scrollable>
        <CModalHeader>
            <CModalTitle>SUBSCRIPTION</CModalTitle>
        </CModalHeader>
        <CModalBody>
            <CForm class="row gx-3 gy-2 align-items-center">
                <!-- USERNAME -->
                <CRow class="mb-3">
                    <CFormInput type="email" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Example: MyUserId; jean_didier; USER-1234; ..." label="Username" v-model="username"
                        :valid="isValidUsername" />

                </CRow>
                <CRow class="mb-3">
                    <CFormInput type="email" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Put here the same username" label="Confirm your username" v-model="confUsername"
                        :valid="isValidUsername && confUsername == username && confUsername != ''" />
                </CRow>
                <!-- PASSWORD -->
                <CRow class="mb-3">
                    <CFormInput type="password" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Example: MyVeryStrongPWD1234" label="Password" v-model="password"
                        :valid="isValidUsername && password != ''" />
                </CRow>
                <CRow class="mb-3">
                    <CFormInput type="password" class="form-control form-control" id="colFormLabelLg"
                        placeholder="Put here the same password" label="Confirm your password" v-model="confPassword"
                        :valid="isValidUsername && confPassword == password && confPassword != ''" />
                </CRow>
            </CForm>
        </CModalBody>
        <CModalFooter>
            <CButton color="success"
                :disabled="!isValidUsername || username == '' || password == '' || confUsername == '' || confPassword == ''"
                @click="submit(username, password)">Submit</CButton>
            <CButton color="danger" @click="$emit('close')">Close</CButton>
        </CModalFooter>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import { get, post } from "@/tools/api";
import { CButton, CForm, CFormInput, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter } from "@coreui/vue";

export default {
    name: 'VSubscribe',
    components: {
        CButton, CFormInput, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CForm
    },
    data() {
        return {
            username: "",
            confUsername: "",
            password: "",
            confPassword: "",
            isValidUsername: false,
        };
    },
    props: {
        enable: {
            type: Boolean
        }
    },
    methods: {
        async submit(username, password) {
            const user = await post("api/auth", {}, {
                username: username,
                password: password
            });
            if (user) {
                alert(user);
                this.$emit("close");
            }
        }
    },
    watch: {
        async username() {
            if (this.username != '') {
                // We'll check if this username does not already exist in our DB
                const isExists = await get("api/user", { username: this.username });
                this.isValidUsername = !isExists;
            }
        }
    }
}
</script>
  
<style scoped></style>