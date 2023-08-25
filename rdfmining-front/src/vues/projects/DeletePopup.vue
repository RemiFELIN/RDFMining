<template>
    <CModal :visible="enable" alignment="center" scrollable>
        <CModalHeader>
            <CModalTitle>Delete {{ projectName }} project</CModalTitle>
        </CModalHeader>
        <CModalBody>
            <!-- USERNAME -->
            <CRow class="mb-3">
                Are you sure to delete the project ?
            </CRow>
            <!-- ALERT -->
            <CAlert :visible="deleted" color="success" class="d-flex align-items-center">
                <!-- <CIcon class="flex-shrink-0 me-2" width="24" height="24" /> -->
                <div>
                    The project <b>{{ projectName }}</b> has been deleted !
                </div>
            </CAlert>
        </CModalBody>
        <CModalFooter>
            <CButton v-if="!deleted" color="success" @click="deleteProject">Yes
            </CButton>
            <CButton color="danger" @click="$emit('close', project)">Close</CButton>
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
            deleted: false,
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
        deleteProject() {
            axios.delete("http://localhost:9200/api/project", {
                params: { projectName: this.projectName },
                headers: { "x-access-token": this.cookies.get("token") }
            }).then(
                () => {
                    // console.log("Delete project " + this.projectName + ": " + response);
                    this.deleted = true;
                    this.$emit("deleted", this.projectName);
                }
            ).catch((error) => {
                console.log(error);
            });
        },
    },
}
</script>
  
<style scoped></style>