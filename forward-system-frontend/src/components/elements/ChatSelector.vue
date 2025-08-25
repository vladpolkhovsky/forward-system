<script setup lang="ts">

import {ref} from "vue";
import type {ChatShortDto, ChatTag} from "@/core/dto/ChatShortDto.ts";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import Pill from "@/components/elements/Pill.vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";

interface Props {
  userId: number
}

const loading = ref(false);
const noMoreChats = ref(false);
const searchLine = ref("");
const selectedChatId = ref<number>(null);

const props = defineProps<Props>()

interface ChatItem {
  id: number,
  chat: ChatShortDto,
  isActive: boolean
}

let items = ref<ChatItem[]>([]);

const appendChatsToTop = (chats: ChatShortDto[]) => {
  console.log("chats to top", chats);

  let newChatIds = new Set(chats.map(t => t.id));
  items.value = items.value.filter(chat => !newChatIds.has(chat.chat.id));

  let wrapped: ChatItem[] = chats.map<ChatItem>(t => {
    return {
      id: t.id,
      chat: t,
      isActive: selectedChatId.value == t.id
    }
  });

  items.value.unshift(...wrapped);
}

const appendChatsToBottom = (chats: ChatShortDto[]) => {
  console.log("chats to bottom", chats);

  let newChatIds = new Set(chats.map(t => t.id));
  items.value = items.value.filter(chat => !newChatIds.has(chat.chat.id));

  let wrapped: ChatItem[] = chats.map<ChatItem>(t => {
    return {
      id: t.id,
      chat: t,
      isActive: selectedChatId.value == t.id
    }
  });

  items.value.push(...wrapped);
}

const setNoMoreChats = () => {
  console.log("no more chats");
  noMoreChats.value = true;
}

const setHasMoreChats = () => {
  console.log("has more chats");
  noMoreChats.value = false;
}

const clearChats = () => {
  items.value = [];
}

const setLoaded = () => {
  loading.value = false;
}

const setLoading = () => {
  loading.value = true;
}

const clearSearch = () => {
  searchLine.value = "";
  selectedChatId.value = null;
}

const updateSearch = (searchLineRequest: string) => {
  searchLine.value = searchLineRequest;
  selectedChatId.value = null;
  emit("search-value", searchLineRequest);
}

const setMessageViewed = (chatId: number) => {
  items.value.filter(t => t.chat.id == chatId)
      .forEach(t => t.chat.newMessageCount = 0);
}

const incNotViewedMessages = (chatId: number) => {
  items.value.filter(t => t.chat.id == chatId)
      .forEach(t => t.chat.newMessageCount++);
}

const selectChatById = (chatId: number) => {
  items.value.forEach(chat => {
    if (chat.chat.id == chatId) {
      handleSelectChat(chat);
    }
  });
}

defineExpose({
  setLoaded,
  setLoading,
  setNoMoreChats,
  setHasMoreChats,
  appendChatsToTop,
  appendChatsToBottom,
  selectChatById,
  clearChats,
  clearSearch,
  updateSearch,
  setMessageViewed,
  incNotViewedMessages
});

const emit = defineEmits<{
  (e: 'load-more'): void,
  (e: 'read-all'): void,
  (e: 'search-value', value: string): void,
  (e: 'select-chat', value: number): void
}>();

function handleLoadMoreChats() {
  loading.value = true;
  emit("load-more");
}

function handleSearch() {
  emit("search-value", searchLine.value)
}

function handleSelectChat(item: ChatItem) {
  emit("select-chat", item.id);
  selectedChatId.value = item.id;
  items.value.forEach(value => value.isActive = (value.id == item.id));
}

function countVisibleTags(item: ChatItem) {
  if (item.chat.tags && item.chat.tags.length > 0) {
    return item.chat.tags.filter(t => t.metadata.isVisible).length;
  }
  return 0;
}

function getNotPrimaryVisibleTags(item: ChatItem): ChatTag[] {
  if (item.chat.tags && item.chat.tags.length > 0) {
    return item.chat.tags.filter(t => t.metadata.isVisible && !t.metadata.isPrimaryTag);
  }
  return [];
}

function getPrimaryVisibleTags(item: ChatItem): ChatTag[] {
  if (item.chat.tags && item.chat.tags.length > 0) {
    return item.chat.tags.filter(t => t.metadata.isVisible && t.metadata.isPrimaryTag);
  }
  return [];
}
</script>

<template>
  <div class="row mt-2 mb-2 montserrat">
    <div class="input-group">
      <input type="text" class="form-control" placeholder="Поиск чата" v-model="searchLine">
      <button class="btn btn-outline-primary bi bi-search" type="button" @click="handleSearch"></button>
    </div>
  </div>
  <div class="row overflow-auto flex-grow-1">
    <div class="col-12">
      <div :class="['fs-7 card mt-1 position-relative', { 'border border-5 border-primary-subtle': item.isActive }]"
           v-for="item in items"
           @click="handleSelectChat(item)">
        <div class="card-header p-1 ps-2">
          <p class="m-0 p-0 fs-6 d-flex gap-2 justify-content-between">
            <span>
              <span class="me-1 badge text-bg-primary d-inline-block" v-if="item.isActive"><i
                  class="bi bi-check2 me-1"></i>Открыт</span>
              <span class="badge rounded-pill bg-danger me-1 ms-1 d-inline-block"
                    v-if="item.chat.newMessageCount > 0">
                <i class="bi bi-chat-left-text me-1"></i>{{ item.chat.newMessageCount }}</span>

              <span class="ms-1 fw-bold d-inline-block">{{ item.chat.displayName }}</span>
            </span>
            <span class="d-inline-block"><OrderStatusIcon v-if="item.chat.orderId" :name="item.chat.orderStatus"
                                                          :rus-name="item.chat.orderStatusRus"/></span>
          </p>
        </div>
        <div class="card-body p-2 d-flex flex-column gap-2" v-if="countVisibleTags(item)">
          <div class="d-inline-flex gap-1 flex-wrap fs-6"
               v-if="getPrimaryVisibleTags(item).length > 0">
            <template v-for="tag in getPrimaryVisibleTags(item)">
              <Pill :text="tag.name"
                    :color="(tag.metadata.isPersonalTag ? (props.userId == tag.metadata.userId ? tag.metadata.cssColorName : 'secondary') : tag.metadata.cssColorName)"></Pill>
            </template>
          </div>
          <div class="d-inline-flex gap-1 flex-wrap fs-7"
               v-if="getNotPrimaryVisibleTags(item).length > 0">
            <template v-for="tag in getNotPrimaryVisibleTags(item)">
              <Pill :text="tag.name"
                    :color="(tag.metadata.isPersonalTag ? (props.userId == tag.metadata.userId ? tag.metadata.cssColorName : 'secondary') : tag.metadata.cssColorName)"></Pill>
            </template>
          </div>
        </div>
        <div :class="['card-footer text-body-secondary p-1 ps-2', { 'border-top-0': item.chat.tags.length == 0 }]">
          {{ item.chat.lastMessageDate }}
        </div>
      </div>
      <div class="mb-5">
        <div class="row mt-2" v-if="loading">
          <LoadingSpinner/>
        </div>
        <div class="row mt-2" v-if="noMoreChats">
          <p class="m-0 text-center">Чатов больше нет.</p>
        </div>
        <div class="row mt-2 ps-3 pe-3">
          <button class="btn btn-sm btn-primary" v-if="!loading && !noMoreChats" @click="handleLoadMoreChats">Ещё
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.fs-7 {
  font-size: 0.85rem !important;
}

.montserrat {
  font-family: "Montserrat", serif;
  font-optical-sizing: auto;
  font-style: normal;
}

.start-100-minus-35px {
  left: calc(100% - 35px) !important;
}
</style>