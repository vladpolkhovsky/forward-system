<script setup lang="ts">

import {ref} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";

interface Props {
  userId: number
}

defineProps<Props>();
const loading = ref(false);

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div class="mb-3" v-else>
    <p class="h4">Личный рейтинг</p>
    <div class="row">
      <p class="ms-3"><i>Всё ещё в разработке....</i></p>
    </div>
  </div>
</template>

<style scoped>

</style>