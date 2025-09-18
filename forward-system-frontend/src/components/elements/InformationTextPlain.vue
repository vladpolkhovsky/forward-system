<script setup lang="ts">

interface Props {
  showIcon: boolean,
  icon: string,
  tittle: string,
  mt: number
}

let props = defineProps<Props>();

</script>

<template>
  <div :class="['card', 'mt-' + props.mt]">
    <div class="card-body p-2">
      <h5 class="card-title">
        <i :class="[props.icon, 'me-1']" v-if="props.showIcon"></i> {{ props.tittle }}
      </h5>
      <slot></slot>
    </div>
  </div>
</template>

<style scoped>
.fs-7 {
  font-size: 0.85rem !important;
}
</style>