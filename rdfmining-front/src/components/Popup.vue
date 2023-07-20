<template>
    <div class="modal-mask">
        <div class="modal-wrapper">
            <div class="modal-container">

                <div class="modal-body">
                    <slot name="body">
                        <label>{{ message }}</label>
                    </slot>
                </div>

                <div class="modal-footer">
                    <slot name="footer">
                        <button @click="confirm">Confirm</button>
                        <button @click="cancel" style="background-color: rgb(148, 0, 0);">Cancel</button>
                    </slot>
                </div>
            </div>
        </div>
    </div>
</template>
  
<script>
export default {
    name: 'AuthComponent',
    props: {
        message: {
            type: String
        }
    },
    methods: {
        confirm() {
            this.$emit('confirm');
        },
        cancel() {
            this.$emit('cancel');
        },
    }
}
</script>
  
<style scoped>
label {
    font-size: 2vw;
}

input,
select {
    width: 100%;
    padding: 12px 20px;
    margin: 8px 0;
    font-size: 2vw;
    display: inline-block;
    border: 1px solid #ccc;
    border-radius: 4px;
    box-sizing: border-box;
}

.modal-mask {
    position: fixed;
    /* z-index: 9998; */
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: table;
    transition: opacity 0.3s ease;
}

.modal-wrapper {
    display: table-cell;
    vertical-align: middle;
}

.modal-footer {
    /* right: 0; */
    bottom: 0;
}

.modal-container {
    /* width: 90%; */
    height: 40%;
    text-align: center;
    margin: 0px auto;
    padding: 20px 30px;
    background-color: #fff;
    border-radius: 10px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.33);
    flex-direction: column;
    justify-content: space-between;
    display: flex;
    transition: all 0.3s ease;
    font-family: Helvetica, Arial, sans-serif;
}

.modal-header {
    margin-top: 2%;
    font-size: 3vw;
    text-align: center;
    color: green;
}

.modal-body {
    margin: 20px 0;
    display: inline-block;
}
</style>