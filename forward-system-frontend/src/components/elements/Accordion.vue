<script setup lang="ts">

import {ref, useId} from "vue";

interface Props {
  name?: string
}

const props = defineProps<Props>();

const id = ref(useId());

</script>

<template>
  <p class="h6 mt-2" v-if="props.name">{{ props.name }}</p>
  <div class="accordion" :id="id" ref="accordionRef">
    <slot/>
  </div>
</template>

<style scoped>

</style>