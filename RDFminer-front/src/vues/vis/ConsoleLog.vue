<template>
    <CRow class="mb-3">
        <CFormLabel class="col-sm-2 col-form-label"><b>RDFMiner log</b></CFormLabel>
        <CCol sm="10">
            <!-- Content -->
            <CFormTextarea style="color: rgb(1, 108, 157)" v-model="logsContent" readonly>
                {{ logsContent }}
            </CFormTextarea>
            <!-- <p>{{ selectedPrefixes }}</p> -->
        </CCol>
    </CRow>
</template>
  
<script>
// import LoginForm from '@/vues/auth/LoginForm.vue';
// https://coreui.io/vue/docs/components/modal.html
import { CRow, CCol, CFormTextarea, CFormLabel } from "@coreui/vue";
import { get } from "@/tools/api";

export default {
    name: 'ConsoleLog',
    components: {
        CRow, CCol, CFormTextarea, CFormLabel
    },
    data() {
        return {
            logsContent: "",
        };
    },
    props: {
        path: {
            type: String
        }
    },
    mounted() {
        // get logs
        this.getLogs();
    },
    methods: {
        async getLogs() {
            this.logsContent = await get("api/logs", { path: this.path });
        }
    },
}
</script>
  
<style scoped></style>