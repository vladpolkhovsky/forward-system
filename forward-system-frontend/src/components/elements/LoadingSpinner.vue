<script setup lang="ts">
interface Props {
  text?: string
  marginTop?: boolean,
  small?: boolean
}

const props = defineProps<Props>()
</script>

<template>
  <div :class="['container', { 'mt-3': marginTop }]">
    <div class="d-flex justify-content-center align-items-center flex-column gap-2">
      <div :class="[ 'spinner-border m-auto', { 'spinner-border-sm': small }]" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
      <p v-if="props.text">{{ props.text }}</p>
    </div>
  </div>
</template>

<style scoped>

</style>