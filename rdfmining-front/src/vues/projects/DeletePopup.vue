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

export default {
    name: 'DeletePopup',
    components: {
        CButton, CRow, CModal, CModalHeader, CModalTitle, CModalBody, CModalFooter, CAlert
    },
    data() {
        return {
            deleted: false,
        };
    },
    props: {
        id: {
            type: String
        },
        projectName: {
            type: String
        },
        enable: {
            type: Boolean
        }
    },
    methods: {
        deleteProject() {
            axios.post("http://localhost:9200/api/project/delete", {
                userId: this.id,
                projectName: this.projectName
            }).then(
                (response) => {
                    console.log("Delete project " + this.projectName + ": " + response);
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