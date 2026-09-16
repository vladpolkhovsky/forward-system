<script setup lang="ts">

import {onMounted, ref} from "vue";
import {UserService} from "@/core/UserService.ts";
import type {ManagerSubDto} from "@/core/dto/ManagerSubDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import UserSelector from "@/components/elements/UserSelector.vue";
import type {UserDto} from "@/core/dto/UserDto.ts";

const loading = ref(true);
const loadedSubManager = ref<ManagerSubDto>(null);
const selectedSubManager = ref<UserDto>(null);

onMounted(() => {
  UserService.fetchUserSub(subManager => {
    loadedSubManager.value = subManager;
    loading.value = false;
  });
});

const handleApplyClick = (subUserId: number) => {
  loading.value = true

  UserService.updateUserSub(subUserId, subManager => {
    loadedSubManager.value = subManager;
    loading.value = false;
  });
};

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div v-else class="container">
    <div class="row">
      <div class="col-12">
        <div v-if="loadedSubManager?.hasSubManager" class="alert alert-primary">
          Текущий замещающий: <strong>{{ loadedSubManager?.subManager?.username }}</strong>
        </div>
        <div v-else class="alert alert-warning">Нет замещающего</div>
        <UserSelector authority="MANAGER" @select="value => selectedSubManager = value"/>
        <div v-if="selectedSubManager && selectedSubManager.id != loadedSubManager?.subManager?.id">
          <button class="btn btn-primary" @click="handleApplyClick(selectedSubManager.id)">Назначить
            {{ selectedSubManager.username }} замещающим
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>