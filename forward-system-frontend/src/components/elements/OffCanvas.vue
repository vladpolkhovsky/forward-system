<script setup lang="ts">
import {onMounted, ref, useId} from "vue";
import {Offcanvas} from "bootstrap";

interface Props {
  offCanvasName: string
  offcanvasSide: "start" | "end"
  initialShow: boolean
}

const props = defineProps<Props>()

const offcanvasId = ref(useId())
const offcanvasLabledById = ref(useId())
let offcanvas: Offcanvas = null;

onMounted(() => {
  offcanvas = new Offcanvas('#' + offcanvasId.value);
  if (props.initialShow) {
    offcanvas.show();
  }
})

const show = () => {
  offcanvas.show();
}

const hide = () => {
  offcanvas.hide();
}

defineExpose({
  offcanvasId,
  show,
  hide
})

</script>

<template>
  <div :class="['offcanvas', `offcanvas-${props.offcanvasSide}`]" tabindex="-1"
       :id="offcanvasId">
    <div class="offcanvas-header pt-2 pb-1 ps-3 pe-3 border-bottom border-2">
      <h5 :id="offcanvasLabledById">{{ props.offCanvasName }}</h5>
      <button type="button" class="btn-close text-reset" @click="hide"></button>
    </div>
    <div class="offcanvas-body p-1 h-100 d-flex flex-column overflow-hidden">
      <slot/>
    </div>
  </div>
</template>

<style scoped>

</style>