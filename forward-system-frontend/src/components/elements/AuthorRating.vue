<script setup lang="ts">

import {ref} from "vue";

interface Props {
  userId: number
}

defineProps<Props>();
const loading = ref(false);

</script>

<template>
  <div class="container" v-if="loading">
    <div class="d-flex">
      <div class="spinner-border m-auto" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>
  <div class="mb-3" v-else>
    <p class="h4">Личный рейтинг</p>
    <div class="row">
      <p class="ms-3"><i>Всё ещё в разработке....</i></p>
    </div>
  </div>
</template>

<style scoped>

</style>