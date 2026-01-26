<script setup lang="ts">

import {ref} from "vue";
import {ChatService} from "@/core/ChatService.ts";
import {ChatTypeEnum} from "@/core/enum/ChatTypeEnum.ts";
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";

const chatId = ref<string>();
const chatName = ref<string>();
const chats = ref<ChatShortDto[]>([]);
const selectedChat = ref<ChatShortDto>();

const handleSearch = () => {
  let cId = chatId.value.length > 0 ? chatId.value : null;
  ChatService.simpleSearchChat(ChatTypeEnum.ORDER_CHAT, cId, chatName.value, fetched => {
    chats.value = fetched;
  });
}

const selectChat = (chat: ChatShortDto) => {
  selectedChat.value = chat;
  emit('select', chat);
}

const emit = defineEmits<{
  (e: 'select', value: ChatShortDto): void,
}>();

</script>

<template>
  <div class="container p-2 mb-3">
    <div class="row">
      <div class="col-12">
        <h4 class="m-0">Поиск чата</h4>
      </div>
    </div>
    <div class="row mt-2 mb-2">
      <div class="col-12 d-flex flex-column gap-3">
        <input class="form-control w-100" placeholder="Индентификатор чата" @change="chatName = ''" v-model="chatId" />
        <input class="form-control w-100" placeholder="Название чата" @change="chatId = ''" v-model="chatName" />
        <button class="btn btn-sm btn-primary" :disabled="!(chatName?.length > 0 || chatId?.length > 0)" @click="handleSearch">
          Поиск
        </button>
      </div>
    </div>
    <div class="row">
      <div class="col-12">
        <ul class="list-group">
          <li class="list-group-item" :class="{'active': selectedChat?.id == chat.id}" v-for="chat in chats"
              @click="selectChat(chat)">
            <div class="p-2" @click="selectedChat = chat">
              <p class="h5 m-0 p-0">{{ chat.displayName }}</p>
              <p class="h6 m-0 p-0">Идентификатор: {{ chat.id }}</p>
            </div>
          </li>
          <li class="list-group-item" v-if="chats.length == 0">Чатов не найдено</li>
        </ul>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>