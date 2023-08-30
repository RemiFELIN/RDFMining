<template>
    <CModal :visible="enable" alignment="center" @close="reset" scrollable>
        <CModalHeader>
            <CModalTitle>Delete <b>{{ projectName }}</b> project</CModalTitle>
        </CModalHeader>
        <CModalBody>
            <!-- USERNAME -->
            <CRow class="mb-3">
                Are you sure to delete the project ?
            </CRow>
            <!-- ALERT -->
            <CAlert :visible="isDeleted" color="success" class="d-flex align-items-center">
                <!-- <CIcon class="flex-shrink-0 me-2" width="24" height="24" /> -->
                <div>
                    The project <b>{{ projectName }}</b> has been deleted !
                </div>
            </CAlert>
            <CAlert :visible="isError" color="danger" class="d-flex align-items-center">
                <!-- <CIcon class="flex-shrink-0 me-2" width="24" height="24" /> -->
                <div>
                    Error during deletion ({{ code }}), please try again later...
                </div>
            </CAlert>
        </CModalBody>
        <CModalFooter v-if="isDeleted == false">
            <CButton color="success" @click="deleteProject">Yes
            </CButton>
        </CModalFooter>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import axios from "axios"
import { CButton, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CAlert } from "@coreui/vue";
import { useCookies } from "vue3-cookies";

export default {
    name: 'DeletePopup',
    components: {
        CButton, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CAlert
    },
    data() {
        return {
            cookies: useCookies(["token", "id"]).cookies,
            isDeleted: false,
            isError: false,
            code: "",
        };
    },
    props: {
        // token: {
        //     type: String
        // },
        projectName: {
            type: String
        },
        enable: {
            type: Boolean
        }
    },
    methods: {
        reset() {
            this.isDeleted = false;
            this.isError = false;
            this.code = "";
        },
        deleteProject() {
            axios.delete("http://localhost:9200/api/project", {
                params: { projectName: this.projectName },
                headers: { "x-access-token": this.cookies.get("token") }
            }).then(
                (response) => {
                    if (response.data == true) {
                        this.isDeleted = true;
                        this.$emit("deleted", this.projectName);
                    } else {
                        this.isError = true;
                        this.code = response.status;
                    }
                }
            ).catch((error) => {
                console.log(error);
            });
        },
    },
}
</script>
  
<style scoped></style>