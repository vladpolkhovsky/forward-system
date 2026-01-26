<script setup lang="ts">

import UserSelector from "@/components/elements/UserSelector.vue";
import type {UserDto} from "@/core/dto/UserDto.ts";
import {ref} from "vue";
import ChatSimpleSelector from "@/components/elements/ChatSimpleSelector.vue";
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";

const selectedUser = ref<UserDto>();
const selectedChat = ref<ChatShortDto>();

const handleSelectUser = (manager: UserDto) => {
  selectedUser.value = manager;
}

const handleSelectChat = (chat: ChatShortDto) => {
  selectedChat.value = chat;
}

const handleAddToChat = () => {
  fetch(`/api/v3/chat/${selectedChat.value.id}/${selectedUser.value.id}`, {
    method: "POST"
  }).then(_ => {
    alert("Пользователь добавлен в чат.")
  })
}

const handleDeleteFromChat = () => {
  fetch(`/api/v3/chat/${selectedChat.value.id}/${selectedUser.value.id}`, {
    method: "DELETE"
  }).then(_ => {
    alert("Пользователь добавлен в чат.")
  })
}

</script>

<template>
  <div class="container pt-3">

    <div class="row m-3" v-if="selectedUser && selectedChat">
      <div class="col-12 d-flex justify-content-between">
        <button @click="handleAddToChat" class="btn btn-primary">Добавить пользователя {{ selectedUser.username }} в чат
          {{ selectedChat.displayName }}
        </button>
        <button @click="handleDeleteFromChat" class="btn btn-danger">Удалить пользователя {{ selectedUser.username }} из
          чат
          {{ selectedChat.displayName }}
        </button>
      </div>
    </div>

    <div class="row mt-3">
      <div class="col-6">
        <UserSelector authority="MANAGER" @select="handleSelectUser"></UserSelector>
      </div>
      <div class="col-6">
        <ChatSimpleSelector @select="handleSelectChat"></ChatSimpleSelector>
      </div>
    </div>

  </div>
</template>

<style scoped>

</style>