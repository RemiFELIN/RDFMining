<template>
    <CModal :visible="enable" alignment="center" @close="reset" scrollable>
        <CModalHeader>
            <CModalTitle><b>{{ projectName }}</b> has been created !</CModalTitle>
        </CModalHeader>
        <CModalBody>
            <!-- USERNAME -->
            <CRow class="mb-3">
                This project has been created and save it in the queue, 
                this project will be automatically launched as soon as the server is available.
            </CRow>
        </CModalBody>
    </CModal>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import { CRow, CModal, CModalHeader, CModalTitle, CModalBody } from "@coreui/vue";

export default {
    name: 'SuccessPopup',
    components: {
        CRow, CModal, CModalHeader, CModalTitle, CModalBody
    },
    props: {
        projectName: {
            type: String
        },
        enable: {
            type: Boolean
        }
    },
}
</script>
  
<style scoped></style>