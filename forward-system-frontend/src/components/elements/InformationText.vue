<script setup lang="ts">
import AccordionItem from "@/components/elements/AccordionItem.vue";
import {ref, useId} from "vue";

interface Props {
  showIcon: boolean,
  icon: string,
  tittle: string,
  expand: boolean
  mt: number
}

let props = defineProps<Props>();
const id = ref(useId());

</script>

<template>
  <div :class="['card', 'mt-' + props.mt]">
    <div class="card-body p-2">
      <h5 class="card-title">
        <i :class="[props.icon, 'me-1']" v-if="props.showIcon"></i> {{ props.tittle }}
      </h5>
      <div class="accordion fs-7" :id="id" ref="accordionRef">
        <AccordionItem name="Информация" :open="props.expand">
          <slot></slot>
        </AccordionItem>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fs-7 {
  font-size: 0.85rem !important;
}
</style>