<script setup lang="ts">

import type {UserDto} from "@/core/dto/UserDto.ts";
import type {MessageDto} from "@/core/dto/MessageDto.ts";
import {type LastSeenAtDto, LastSeenAtService} from "@/core/LastSeenAtService.ts";
import {onMounted, ref} from "vue";
import {Tooltip} from "bootstrap";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";
import type {ChatOptionDto} from "@/core/dto/ChatOptionDto.ts";

interface Props {
  user: UserDto,
  message: MessageDto,
  isSmallDevice: boolean
}

const props = defineProps<Props>();
const tooltipRef = ref<InstanceType<typeof HTMLSpanElement>>();

const isMyOwnMessage = props.message.fromUserId == props.user.id;
const isSystemMessage = props.message.isSystemMessage;
const isSimpleOtherMessage = !(isSystemMessage || isMyOwnMessage);

const lastSeenAt = ref<LastSeenAtDto>();

const refreshLastSeenAt = () => {
  let userLastSeenAt = LastSeenAtService.getUserLastSeenAt(props.message.fromUserId);
  if (userLastSeenAt) {
    lastSeenAt.value = userLastSeenAt;
  }
}

defineExpose({
  refreshLastSeenAt
})

onMounted(() => {
  refreshLastSeenAt();

  let tooltip = Tooltip.getOrCreateInstance(tooltipRef.value, {
    placement: 'top',
    trigger: 'hover',
    tittle: 'Сообщение прочитано',
    container: `#message-${props.message.id}`,
    html: true
  });

  const tooltipHtml = props.message.messageReadedByUsernames.join("<br/>")
  tooltip.setContent({'.tooltip-inner': tooltipHtml});
});

function handleOptionClick(option: ChatOptionDto) {
  if (option.isResolved) {
    return;
  }

  if (option.url.startsWith("/api/order/distribution/item")) {
    fetch(`${option.url}&optionId=${option.id}`)
        .then(value => value.json())
        .then(value => {
          if (value['code'] != 200) {
            throw new Error(value['message'])
          }
          option.isResolved = true;
        })
        .catch(err => {
          alert(err['message']);
        });
    return;
  }

  window.open(option.url, '_blank');
}

function getOptionClasses(option: ChatOptionDto): string[] {

  let optionColorDefined = false;
  const classes: string[] = [];

  if (option.isResolved) {
    classes.push("text-decoration-line-through")
  }

  if (option.url.startsWith("/api/order/distribution/item")) {
    if (option.url.includes("action=ACCEPTED")) {
      classes.push("btn-success")
    }
    if (option.url.includes("action=TALK")) {
      classes.push("btn-info")
    }
    if (option.url.includes("action=DECLINE")) {
      classes.push("btn-danger")
    }
    optionColorDefined = true;
  }

  if (!optionColorDefined) {
    classes.push("btn-primary")
  }

  return classes;
}

</script>

<template>
  <div :id="'message-' + message.id" :class="[ 'border rounded-3 p-1 mb-3 position-relative', {
    'ms-auto bg-success-subtle': isMyOwnMessage,
    'me-auto': !isMyOwnMessage,
    'bg-danger-subtle': isSystemMessage,
    'bg-white': isSimpleOtherMessage,
    'w-75': !isSmallDevice,
    'w-100': isSmallDevice,
  }]" :message-id="message.id">
    <p class="fs-6 m-2" :message-from-user-id="message.fromUserId">
      <span class="me-2 fw-bold">{{ message.fromUserUsername }}</span>
      <span class="me-2 badge text-bg-success"
            v-if="message.fromUserOrderParticipantType == ParticipantTypeEnum.MAIN_AUTHOR">Автор</span>
      <span class="me-2 badge text-bg-primary" v-if="message.fromUserOrderParticipantType == ParticipantTypeEnum.HOST">Менеджер</span>
      <span class="me-2 badge text-bg-danger" v-if="message.fromUserIsAdmin">Админ</span>
      <span :class="[ 'me-2 badge', { 'text-bg-secondary': !lastSeenAt.online, 'text-bg-success': lastSeenAt.online }]"
            :online-status-id="lastSeenAt.id"
            v-if="lastSeenAt && lastSeenAt.shouldBeVisible">
        {{ (lastSeenAt.online ? '' : 'Был в сети: ') + lastSeenAt.lastOnlineAt }}
      </span>
    </p>
    <pre class="m-2 fs-7 line-break montserrat" v-if="message.text" v-html="message.text"/>
    <template v-for="opt in message.options">
      <button :class="['btn btn-sm w-100 fs-7 mb-1 montserrat', ...getOptionClasses(opt)]"
              :disabled="opt.isResolved"
              @click="handleOptionClick(opt)">{{ opt.name }}
      </button>
    </template>
    <div class="d-flex justify-content-between m-2 fs-7" v-if="message.attachments.length > 0">
      <div class="d-block">
        <a :href="'/load-file/' + att.id" target="_blank" class="me-2 d-inline-block line-break text-break"
           v-for="att in message.attachments">{{ att.name }}</a>
      </div>
    </div>
    <div class="d-flex justify-content-end fs-7 p-1 me-1">
      <span class="position-relative">{{ message.createdAt }}
        <span ref="tooltipRef"
              class="ms-2 bi bi-check-lg"
              data-bs-toggle="tooltip"
              data-bs-title="Сообщение прочитано"
              v-show="message.messageReadedByUsernames.length > 0">
          <span class="position-absolute top-0 start-100 translate-middle fw-light ms-1"
                v-if="message.messageReadedByUsernames.length > 0">
            {{ message.messageReadedByUsernames.length }}
            <span class="visually-hidden">who read message</span>
          </span>
        </span>
      </span>
    </div>
    <span v-if="message.isNewMessage"
          class="position-absolute top-0 start-100-minus-35px translate-middle badge rounded-pill bg-primary">Новое</span>
  </div>
</template>

<style scoped>
.montserrat {
  font-family: "Montserrat", serif;
  font-optical-sizing: auto;
  font-style: normal;
}

.start-100-minus-35px {
  left: calc(100% - 35px) !important;
}

.line-break {
  line-break: normal;
  white-space: pre-wrap;
}

.fs-7 {
  font-size: 0.85rem !important;
}
</style>