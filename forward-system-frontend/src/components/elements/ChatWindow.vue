<script setup lang="ts">
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";
import Message from "@/components/elements/Message.vue";
import {computed, nextTick, onMounted, ref, useTemplateRef, watch} from "vue";
import type {UserDto} from "@/core/dto/UserDto.ts";
import FileSelector from "@/components/elements/FileSelector.vue";
import type {MessageDto} from "@/core/dto/MessageDto.ts";
import {AuthorityEnum, hasAuthority} from "@/core/enum/AuthorityEnum";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {ChatService} from "@/core/ChatService.ts";

interface Props {
  chat: ChatShortDto
  user: UserDto,
  isSmallDevice: boolean
}

let page = 0;

const showMessageArea = computed(() => {
  if (props?.chat?.metadata?.onlyOwnerCanType == true) {
    return hasAuthority(props.user.authorities, AuthorityEnum.OWNER);
  }
  return true;
});

const showFileSelection = computed(() => {
  if (props?.chat?.metadata?.authorCanSubmitFiles == false) {
    return hasAuthority(props.user.authorities, AuthorityEnum.MANAGER)
        || hasAuthority(props.user.authorities, AuthorityEnum.ADMIN)
        || hasAuthority(props.user.authorities, AuthorityEnum.OWNER)
  }
  return true;
});

const props = defineProps<Props>();

const messagesRefs = useTemplateRef<InstanceType<typeof Message>[]>("msgRef");
const messages = ref<MessageDto[]>([]);

const filesSelectorRef = ref<InstanceType<typeof FileSelector>>();
const textAreaElementRef = ref<InstanceType<typeof HTMLTextAreaElement>>();
const buttonElementRef = ref<InstanceType<typeof HTMLButtonElement>>();
const bodyRef = ref<InstanceType<typeof HTMLDivElement>>();

const waitNewMessages = ref(true);
const hasMoreMessages = ref(true);
const isFirstLoad = ref(true);

const messageText = ref("");

const emit = defineEmits<{
  (e: 'load-more', value: number): void
}>();

const clear = () => {
  messages.value = [];
  hasMoreMessages.value = true;
  waitNewMessages.value = true;
  isFirstLoad.value = true;
  page = 0;
}

watch(() => props.chat, (newValue) => {
  console.log("Выбран чат", newValue.displayName)
  clear();
  requestMoreMessages();
});

onMounted(() => {
  clear();
  requestMoreMessages();
  setInterval(() => {
    messagesRefs.value?.forEach(value => {
      value.refreshLastSeenAt();
    });
  }, 30000);
});

const appendMessageToTop = (message: MessageDto) => {
  if (message.chatId == props.chat.id) {
    messages.value.unshift(message);
    messages.value.sort((a, b) => a.id - b.id);
  }
  nextTick(() => {
    bodyRef.value?.scroll({
      behavior: 'smooth',
      top: bodyRef.value.scrollHeight
    });
  });
}

const appendMessageToBottom = (message: MessageDto) => {
  if (message.chatId == props.chat.id) {
    messages.value.push(message);
    messages.value.sort((a, b) => a.id - b.id);
  }
  nextTick(() => {
    bodyRef.value?.scroll({
      behavior: 'smooth',
      top: bodyRef.value.scrollHeight
    });
  });
}

const refreshLastSeenAt = () => {
  messagesRefs.value.forEach(value => value.refreshLastSeenAt());
}

const setMessagesAdded = () => {
  waitNewMessages.value = false;
}

const setNoMoreMessages = () => {
  hasMoreMessages.value = false;
}

const requestMoreMessages = () => {
  waitNewMessages.value = true;
  emit("load-more", page);
  page = page + 1;
}

function handleSendMessage() {
  let fileIds = (filesSelectorRef?.value?.readyFiles ?? []).map(value => value.id);
  let text = messageText.value;

  buttonElementRef?.value?.setAttribute("disabled", "");
  textAreaElementRef?.value?.setAttribute("disabled", "");
  filesSelectorRef?.value?.setAttribute("disabled", "");

  ChatService.sendMessageToChat(props.user.id, props.chat.id, text, fileIds, () => {
    buttonElementRef?.value?.removeAttribute("disabled");
    textAreaElementRef?.value?.removeAttribute("disabled");
    filesSelectorRef?.value?.removeAttribute("disabled");

    filesSelectorRef?.value?.clearFiles();
    messageText.value = "";
  });
}

defineExpose({
  clear,
  requestMoreMessages,
  appendMessageToTop,
  appendMessageToBottom,
  refreshLastSeenAt,
  setMessagesAdded,
  setNoMoreMessages
});

</script>

<template>
  <div class="card overflow-auto flex-grow-1 mt-0 mt-lg-2 mb-3">
    <div class="card-header fs-5 fw-bold">{{ props.chat.displayName }}</div>
    <div class="card-body overflow-auto flex-grow-1" ref="bodyRef">
      <div class="text-center" v-if="!waitNewMessages">
        <button class="btn btn-outline-primary mb-2" v-if="hasMoreMessages" @click="requestMoreMessages"><i class="bi bi-box-arrow-in-down me-1"></i>Загрузить
          ещё
        </button>
      </div>
      <LoadingSpinner v-else text="Загрузка сообщений"/>
      <Message :user="user" :message="message" :is-small-device="isSmallDevice" v-for="message in messages"
               :key="message.id"
               ref="msgRef"/>
    </div>
    <div class="card-footer text-body-secondary p-2" v-if="showMessageArea">
      <div class="row m-auto">
        <div class="col-12 col-md-8">
          <textarea id="message-box"
                    class="form-control"
                    :rows="isSmallDevice ? 3 : 7"
                    ref="textAreaElementRef"
                    v-model="messageText"
                    @keydown.ctrl.enter="handleSendMessage"
                    @keydown.meta.enter="handleSendMessage"
          ></textarea>
        </div>
        <div class="col-12 col-md-4 d-flex flex-md-column align-items-stretch flex-row gap-2 mt-2 mt-md-0">
          <FileSelector v-if="showFileSelection" ref="filesSelectorRef"/>
          <button id="send-btn" class="btn btn-primary btn-sm w-100" @click="handleSendMessage" ref="buttonElementRef">
            Отправить
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>