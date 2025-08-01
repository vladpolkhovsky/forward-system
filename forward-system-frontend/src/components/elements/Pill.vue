<script setup lang="ts">
  const props = defineProps({
    text: String,
    color: String
  })

  const textBgColor = `text-bg-${props.color}`;
</script>

<template>
  <span :class="[`badge`, `rounded-pill`, textBgColor]">{{ props.text }}</span>
</template>

<style scoped>

</style>