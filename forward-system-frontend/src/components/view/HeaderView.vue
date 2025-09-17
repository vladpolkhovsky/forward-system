<script setup lang="ts">

import {computed, onMounted, ref} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {UserService} from "@/core/UserService";
import type {UserDto} from "@/core/dto/UserDto";
import type {UserPlanViewDto} from "@/core/dto/UserPlanViewDto";
import {PlanViewService} from "@/core/PlanViewService";
import {AuthorityEnum, hasAuthority} from "@/core/enum/AuthorityEnum";
import Plan from "@/components/elements/Plan.vue";
import {ChatService} from "@/core/ChatService";
import {ChatTypeEnum} from "@/core/enum/ChatTypeEnum";

const handleResize = () => {
  windowWidth.value = window.outerWidth;
  windowHeight.value = window.outerWidth;
};

const windowWidth = ref(window.innerWidth);
const windowHeight = ref(window.innerHeight);
const loadingMessagesCount = ref(true);
const loadingUser = ref(true);
const userHasPlan = ref(false);
const loadingUserPlan = ref(true);

const user = ref<UserDto>();
const userPlan = ref<UserPlanViewDto>();

const isSmallDevice = computed(() => windowWidth.value <= 992 || windowHeight.value < 720);

const isNotSaveDomain = window.location.host !== 'writer-link.by';

const newMessageCountMainChat = ref(0);
const newMessageCountForwardChat = ref(0);

onMounted(() => {
  window.addEventListener('resize', handleResize);
  UserService.fetchUserData(true, null, userDto => {
    user.value = userDto

    if (hasAuthority(userDto.authorities, AuthorityEnum.MANAGER)) {
      userHasPlan.value = true;

      PlanViewService.getActivePlan(userDto.id, planDto => {
        userPlan.value = planDto;
        loadingUserPlan.value = false;
      });
    }

    loadingUser.value = false;
  });

  ChatService.fetchNewMessageCount(newMessageDto => {
    newMessageCountMainChat.value = Object.entries(newMessageDto)
        .map(value => value[1]).reduce((previousValue, currentValue) => previousValue + currentValue, 0);
    newMessageCountForwardChat.value = Object.entries(newMessageDto)
        .filter(value => value[0] == ChatTypeEnum.FORWARD_ORDER_CHAT || value[0] == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT)
        .map(value => value[1]).reduce((previousValue, currentValue) => previousValue + currentValue, 0);
    loadingMessagesCount.value = false;
  });
});

</script>

<template>
  <div class="container">
    <div class="p-2 alert alert-danger mt-2 mb-2" role="alert" v-if="isNotSaveDomain">
      <p class="ms-2 me-2 mt-1 mb-1 text-center ">Вы используете старую ссылку. У сайта появился защищённый домен.
        <br/>
        Ссылка на новый домен: <a target="_self" href="https://writer-link.by/main">writer-link.by</a>!</p>
    </div>
    <header
        :class="['mt-3 mb-3 border-bottom border-2', { 'justify-content-center align-items-center flex-column gap-3 d-flex': isSmallDevice, 'row': !isSmallDevice}]"
        style="min-height: 95px">
      <ul class="nav col-12 col-lg-8 mb-2 justify-content-center justify-content-lg-start mb-md-0 align-items-center ">
        <li class="me-3">
          <a class="nav-link px-2 link-primary fw-medium text-dark" href="/main"><i class="bi bi-list me-2"></i>Меню</a>
        </li>
        <li class="me-3">
          <a class="nav-link px-2 text-dark position-relative"
             href="/new-messenger-v3?tab=all#center">
            <span class="fw-bold link-dark"><i class="bi bi-chat-left-dots me-2"></i>Чаты</span>
            <div class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-dark"
                 v-if="loadingMessagesCount">
              <LoadingSpinner :small="true"/>
            </div>
            <span v-else class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-dark" v-if="newMessageCountMainChat > 0">
              {{ newMessageCountMainChat }}
            </span>
          </a>
        </li>
        <li class="me-3">
          <a class="nav-link px-2 link-dark fw-medium text-dark position-relative"
             href="/forward/main#main-view">
            <i class="bi bi-telegram me-2"></i>Прямые заказы
            <div class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-dark"
                 v-if="loadingMessagesCount">
              <LoadingSpinner :small="true"/>
            </div>
            <span v-else class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-dark" v-if="newMessageCountForwardChat > 0">
              {{ newMessageCountForwardChat }}
            </span>
          </a>
        </li>
        <li class="me-3">
          <a class="nav-link px-2 link-primary fw-medium text-dark" href="/calendar"><i
              class="bi bi-calendar-check me-2"></i>Календарь</a>
        </li>
        <li>
          <a class="nav-link px-2 link-primary fw-medium text-dark" href="/push-subscription"><i
              class="bi bi-bell me-2"></i>Уведомления</a>
        </li>
      </ul>
      <div class="col-12 col-lg-4 d-flex justify-content-center">
        <LoadingSpinner text="Загружаем пользователя" v-if="loadingUser"/>
        <div class="d-flex flex-fill gap-2" v-else>
          <div class="d-flex flex-fill gap-2 flex-column justify-content-center">
            <div class="text-center">
              <a class="btn fw-medium text-dark" href="/info"><i class="bi bi-person-vcard me-2"></i>{{ user.username }}</a>
            </div>
            <div v-if="userHasPlan && ((loadingUserPlan && userPlan == null) || userPlan)">
              <LoadingSpinner text="Загружаем план" v-if="loadingUserPlan"/>
              <Plan v-else :plan="userPlan"/>
            </div>
          </div>
          <div class="d-flex align-items-center justify-content-around">
            <a href="/logout" class="btn btn-sm btn-danger m-3">Выйти<i class="ms-2 bi bi-box-arrow-right"></i></a>
          </div>
        </div>
      </div>
    </header>
  </div>
</template>

<style scoped>

</style>