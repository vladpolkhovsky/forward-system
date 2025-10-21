<script setup lang="ts">

import {computed, h, nextTick, onBeforeUnmount, onMounted, ref, render, useId} from "vue";
import OffCanvas from "@/components/elements/OffCanvas.vue";
import type {ChatTabDescription} from "@/core/dto/ChatTabDescription.ts";
import {ChatTabsService} from "@/core/ChatTabsService.ts";
import {QueryParamService} from "@/core/QueryParamService";
import ChatSelector from "@/components/elements/ChatSelector.vue";
import type {UserDto} from "@/core/dto/UserDto.ts";
import {UserService} from "@/core/UserService.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {ChatService} from "@/core/ChatService.ts";
import ChatWindow from "@/components/elements/ChatWindow.vue";
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";
import {LastSeenAtService} from "@/core/LastSeenAtService.ts";
import type {NewMessageDto} from "@/core/dto/NewMessageDto.ts";
import {Toast} from "bootstrap";
import ChatSideBar from "@/components/elements/ChatSideBar.vue";
import type {ChatType} from "@/core/type/ChatType.ts";

const notificationAudio = new Audio("/static/notification.mp3");

const windowWidth = ref(window.innerWidth);
const windowHeight = ref(window.innerHeight);

const isSmallDevice = computed(() => windowWidth.value < 992 || windowHeight.value < 720);

let firstTimeLoad = true;

const chatSelectorRef = ref<InstanceType<typeof ChatSelector>>();
const chatWindowRef = ref<InstanceType<typeof ChatWindow>>();
const toastContainerRef = ref<InstanceType<typeof HTMLDivElement>>();

const offcanvasChatSelectorRef = ref<InstanceType<typeof OffCanvas>>();
const offcanvasChatSelectorRefTemplateId = ref(useId());

const offcanvasDatapanelRef = ref<InstanceType<typeof OffCanvas>>();
const offcanvasDatapanelRefTemplateId = ref(useId());

const leftColumnId = ref(useId());
const rightColumnId = ref(useId());

const user = ref<UserDto>()

interface ChatTab {
  description: ChatTabDescription,
  unreadedChatsCount: number,
  isActive: boolean
}

let chatService: ChatService = null;
let search: string = null;
let page: number = 0;

const selectedChat = ref<ChatShortDto>();

const initialTabParam = QueryParamService.getParam("tab") ?? QueryParamService.setParam("tab", "all");
const initialChatIdParam = QueryParamService.getParam("chatId");

const tabs = ref<ChatTab[]>(ChatTabsService.getDefaultTabs().map<ChatTab>((value) => {
  return {
    isActive: initialTabParam == value.queryParam,
    unreadedChatsCount: 0,
    description: value
  }
}));

const selectedTab = ref<ChatTab>(tabs.value.filter(t => t.isActive)[0])

const handleResize = () => {
  windowWidth.value = window.innerWidth;
  windowHeight.value = window.innerHeight;
};

onMounted(() => {
  window.addEventListener('resize', handleResize);
  initChatService();
  renewNewMessageOnTab();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);

  chatService.close();
  LastSeenAtService.stop();
});

function handleTabSelected(newSelectedTab: ChatTab) {
  const oldSelectedTab = tabs.value.filter(t => t.isActive)[0];
  if (oldSelectedTab != newSelectedTab) {
    oldSelectedTab.isActive = false;
    newSelectedTab.isActive = true;

    resetSearchParams(true);
    initChatService();

    if (offcanvasChatSelectorRef?.value && isSmallDevice?.value) {
      offcanvasChatSelectorRef.value.show();
    }

    QueryParamService.setParam('tab', newSelectedTab.description.queryParam);
    selectedTab.value = newSelectedTab;
  }
}

function getSelectedTab(): ChatTab {
  return tabs.value.filter(t => t.isActive)[0];
}

function clearChatSelector() {
  if (chatSelectorRef.value) {
    chatSelectorRef.value.clearChats();
  }
}

function handleSearchUpdate(value: string) {
  resetSearchParams();
  clearChatSelector();

  search = value;
  handleLoadMoreChats();
}

function resetSearchParams(clearSearch: boolean = false) {
  search = null;
  page = 0;
  if (clearSearch && chatSelectorRef.value) {
    chatSelectorRef.value.clearSearch()
  }
}

function handleLoadMoreChats() {
  chatSelectorRef.value.setLoading();

  chatService.search(search, page, page => {
    chatSelectorRef.value.appendChatsToBottom(page.content);
    chatSelectorRef.value.setLoaded();
    if (page.last) {
      chatSelectorRef.value.setNoMoreChats();
    }
  });

  page = page + 1;
}

function handleLoadMoreChatMessages(pageNumber: number) {
  chatService.loadChatMessages(selectedChat.value.id, pageNumber, page => {

    if (pageNumber == 0) {
      chatWindowRef.value.appendMessageToBottom(selectedChat.value.id, page.content);
    } else {
      chatWindowRef.value.appendMessageToTop(selectedChat.value.id, page.content);
    }

    chatWindowRef.value.setMessagesAdded();

    if (page.last) {
      chatWindowRef.value.setNoMoreMessages();
    }

    handleMessageViewed(selectedChat.value.id);
  });
}

function appendToastAndShow(message: NewMessageDto, chat: ChatShortDto) {
  const id = `toast-message-${message.id}-chat-${chat.id}`;

  const toastElement = h('div', {id: id, class: 'toast', role: 'alert'}, [
    h('div', {class: 'toast-header'}, [
      h('strong', {class: 'me-auto'}, chat.displayName),
      h('button', {type: 'button', class: 'btn-close', 'data-bs-dismiss': 'toast', 'aria-label': "Close"})
    ]),
    h('div', {class: 'toast-body'}, `${message.fromUserUsername} : ${message.message ?? '<Сообщение без текста>'}`),
  ]);

  render(toastElement, toastContainerRef.value);
  let documentToastElement = document.getElementById(id);

  new Toast(documentToastElement, {delay: 7000}).show();
}

function renewNewMessageOnTab() {
  ChatService.fetchNewMessageCount((newMessageCount) => {
    tabs.value.forEach(tab => {
      let count = 0;

      tab.description.chatTypes.forEach(tabType => {
        count += newMessageCount[tabType];
      });

      tab.unreadedChatsCount = count;
    });
  });
}

function initChatService() {
  if (chatService) {
    chatService.close();

    if (chatSelectorRef.value) {
      chatSelectorRef.value.clearChats();
      chatSelectorRef.value.setLoading();
      chatSelectorRef.value.setHasMoreChats();
    }
  }

  UserService.fetchUserData(true, null, userDto => {
    user.value = userDto;
    chatService = new ChatService(userDto.id, selectedChat.value?.id ?? null, getSelectedTab().description.chatTypes)
    chatService.start(
        () => {
          if (firstTimeLoad && initialChatIdParam && initialChatIdParam.length > 0) {
            chatService.fetchChat(parseInt(initialChatIdParam), chat => {
              chatSelectorRef.value.appendChatsToTop([chat]);
              nextTick(() => {
                chatSelectorRef.value.selectChatById(chat.id);
              });
            }, true);
          }
          handleLoadMoreChats();
          firstTimeLoad = false;
        },
        (message, chat) => {
          if (selectedChat?.value?.id != chat.id) {
            chatSelectorRef?.value?.incNotViewedMessages(message.chatId);
            appendToastAndShow(message, chat);
            notificationAudio.play();
          }
          if (selectedChat?.value?.id == chat.id) {
            ChatService.fetchMessageById(message.id, (chatId, message) => {
              chatWindowRef?.value?.appendMessageToBottom(chatId, message);
              chatService.sendMessageViewed(chat.id);
            });
          }
          if (selectedTab.value.description.chatTypes.filter(t => chat.type == t).length > 0) {
            chatSelectorRef?.value?.appendChatsToTop([chat]);
          }
          setTimeout(() => renewNewMessageOnTab(), 1200)
        }
    );
  });

  LastSeenAtService.start();
}

function decUnreadedChatsCount(chatType: ChatType) {
  tabs.value.forEach(tab => {
    tab.description.chatTypes.forEach(type => {
      if (type == chatType) {
        tab.unreadedChatsCount = Math.max(tab.unreadedChatsCount - 1, 0)
      }
    });
  });
}

function handleChatSelection(chatId: number) {
  console.log("handleChatSelection", chatId);

  chatService.fetchChat(chatId, (chat) => {
    selectedChat.value = chat
    console.log("chatService.fetchChat", chat.id, selectedChat.value);

    chatService.updateChatId(chat.id);
    QueryParamService.setParam("chatId", chatId.toString());

    decUnreadedChatsCount(selectedChat.value.type as ChatType)
  });

  offcanvasChatSelectorRef?.value?.hide();
}

function handleMessageViewed(chatId: number) {
  console.log("handleMessageViewed", chatId);
  chatService.sendMessageViewed(chatId);
  chatSelectorRef.value.setMessageViewed(chatId);
}

console.log(isSmallDevice, windowWidth, windowHeight)

</script>

<template>
  <OffCanvas off-canvas-name="Выбор чата" offcanvas-side="start" ref="offcanvasChatSelectorRef"
             :initialShow="isSmallDevice">
    <div :id="offcanvasChatSelectorRefTemplateId" class="p-1 h-100 d-flex flex-column overflow-hidden"></div>
  </OffCanvas>

  <OffCanvas off-canvas-name="Данные чата" offcanvas-side="end" ref="offcanvasDatapanelRef" :initialShow="false">
    <div :id="offcanvasDatapanelRefTemplateId" class="p-1 h-100 d-flex flex-column overflow-hidden"></div>
  </OffCanvas>

  <div class="container-fluid flex-grow-1 d-flex flex-column vh-100 p-2 gap-1 montserrat" id="center">
    <div class="row w-100 m-auto">
      <div class="input-group" v-if="isSmallDevice">
        <label class="input-group-text" for="small-device-tab-selector">Вкладка</label>
        <select class="form-select" id="small-device-tab-selector"
                @change="handleTabSelected(tabs.filter(t => t.description.tabName == ($event.target as HTMLSelectElement).value)[0])">
          <option :value="tab.description.tabName" :selected="tab.isActive" v-for="tab in tabs">
            {{
              tab.description.tabName + (tab.unreadedChatsCount > 0
                  ? ' (' + tab.unreadedChatsCount + ' не прочитано)' : '')
            }}
          </option>
        </select>
      </div>
      <ul class="nav nav-tabs border-2 flex-nowrap overflow-x-scroll overflow-y-hidden" v-else>
        <li class="nav-item" v-for="tab in tabs">
          <a :class="['nav-link fw-semibold border-2 text-nowrap', { 'active': tab.isActive, 'fw-bold': tab.isActive }]"
             @click="handleTabSelected(tab)">
            <span class="position-relative">
              {{ tab.description.tabName }}
              <span class="badge text-bg-danger ms-1"
                    v-if="tab.unreadedChatsCount > 0">
                {{ tab.unreadedChatsCount }}
              </span>
            </span>
          </a>
        </li>
      </ul>
    </div>
    <div class="row w-100 mt-1 mb-1 m-auto" v-if="isSmallDevice">
      <div class="col-6 text-start">
        <button class="btn btn-sm btn-outline-primary container" @click="offcanvasChatSelectorRef.show()"><i
            class="bi bi-arrow-left me-1"></i>Выбор чата <span
            class="badge text-bg-danger ms-1" v-if="selectedTab && selectedTab.unreadedChatsCount > 0"> {{ selectedTab.unreadedChatsCount }}</span>
        </button>
      </div>
      <div class="col-6 text-end">
        <button class="btn btn-sm btn-outline-primary container" @click="offcanvasDatapanelRef.show()">Информация<i
            class="ms-1 bi bi-arrow-right"></i></button>
      </div>
    </div>
    <div class="row flex-grow-1 g-3 overflow-hidden">
      <div :id="leftColumnId" :class="['col-3', 'h-100', 'd-flex', 'flex-column', { 'visually-hidden': isSmallDevice }]"
           v-show="!isSmallDevice"></div>
      <div
          :class="[ { 'col-6': (!isSmallDevice), 'col-12': isSmallDevice }, 'h-100', 'd-flex', 'flex-column', 'overflow-hidden']">
        <ChatWindow :chat="selectedChat"
                    :user="user"
                    v-if="selectedChat && user"
                    :is-small-device="isSmallDevice"
                    ref="chatWindowRef"
                    @load-more="value => handleLoadMoreChatMessages(value)"
        />
        <div v-else
             class="card overflow-auto flex-grow-1 mt-2 mb-3 d-flex align-items-center justify-content-center gap-2">
          <i class="bi bi-chat-right-dots-fill display-6"></i>
          <p class="m-0 p-3 display-6 text-center">Выберите чат для просмотр</p>
        </div>
      </div>
      <div :id="rightColumnId"
           :class="['col-3', 'h-100', 'd-flex', 'flex-column', { 'visually-hidden': isSmallDevice }]"
           v-show="!isSmallDevice"></div>
    </div>
  </div>

  <Teleport :to="'#' + (isSmallDevice ? offcanvasChatSelectorRefTemplateId : leftColumnId)">
    <ChatSelector :user="user" ref="chatSelectorRef" v-if="user"
                  :tab="selectedTab.description.queryParam"
                  @load-more="() => handleLoadMoreChats()"
                  @search-value="value => handleSearchUpdate(value)"
                  @select-chat="value => handleChatSelection(value)"/>
    <LoadingSpinner v-else text="Загрузка пользователя"/>
  </Teleport>
  <Teleport :to="'#' + (isSmallDevice ? offcanvasDatapanelRefTemplateId : rightColumnId)">
    <ChatSideBar :user="user" :chat="selectedChat" v-if="user && selectedChat"/>
    <div v-else
         class="card overflow-auto flex-grow-1 mt-2 mb-3 d-flex align-items-center justify-content-center gap-2">
      <i class="bi bi-info-square display-6"></i>
      <p class="m-0 p-3 display-6 text-center">Информация появится после выбора чата</p>
    </div>
  </Teleport>

  <div class="position-fixed top-0 end-0 p-3" ref="toastContainerRef">
  </div>
</template>

<style scoped>

.montserrat {
  font-family: "Montserrat", serif;
  font-optical-sizing: auto;
  font-style: normal;
}

.new-chat-position {
  left: calc(100% + 10px) !important;
  top: -10px !important;
}

</style>