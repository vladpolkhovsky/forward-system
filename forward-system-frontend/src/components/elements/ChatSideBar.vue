<script setup lang="ts">

import type {UserDto} from "@/core/dto/UserDto.ts";
import type {ChatShortDto} from "@/core/dto/ChatShortDto.ts";
import {onMounted, ref, watch} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {ChatSidebarService} from "@/core/ChatSidebarService.ts";
import type {OrderChatInformation} from "@/core/dto/OrderChatInformation.ts";
import {hasAuthorityManager} from "@/core/enum/AuthorityEnum.ts";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import AccordionItem from "@/components/elements/AccordionItem.vue";
import Accordion from "@/components/elements/Accordion.vue";
import type {OrderReviewDto} from "@/core/dto/OrderReviewDto.ts";
import {ChatTypeEnum} from "@/core/enum/ChatTypeEnum.ts";
import type {ForwardOrderChatInformation} from "@/core/dto/ForwardOrderChatInformation.ts";
import InformationText from "@/components/elements/InformationText.vue";

interface Props {
  user: UserDto,
  chat: ChatShortDto
}

const props = defineProps<Props>();
const orderChatInformation = ref<OrderChatInformation>();
const forwardOrderChatInformation = ref<ForwardOrderChatInformation>();
const orderReviews = ref<OrderReviewDto[]>([]);

const loading = ref(true);
const orderChatInfoLoading = ref(true);
const orderReviewsInfoLoading = ref(true);
const forwardOrderInfoLoading = ref(true);

onMounted(() => {
  renew();
});

watch(() => props.chat, () => {
  renew();
});

const renew = () => {
  loading.value = true;

  orderChatInfoLoading.value = true;
  orderReviewsInfoLoading.value = true;

  orderReviews.value = null;
  orderChatInformation.value = null;
  forwardOrderChatInformation.value = null;

  if (props.chat.orderId) {
    ChatSidebarService.loadChatOrderInformation(props.chat.orderId, orderChatInfo => {
      if (props.chat.orderId == orderChatInfo.id) {
        orderChatInformation.value = orderChatInfo;
        orderChatInfoLoading.value = false;
      }
    });

    ChatSidebarService.loadOrderReviews(props.chat.orderId, reviews => {
      orderReviews.value = reviews.filter(t => t.orderId == props.chat.orderId);
      orderReviewsInfoLoading.value = false;
    });

    if (props.chat.type == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT || props.chat.type == ChatTypeEnum.FORWARD_ORDER_CHAT) {
      forwardOrderInfoLoading.value = true;

      ChatSidebarService.loadForwardOrderChatInfo(props.chat.orderId, forwardOrderInfo => {
        forwardOrderChatInformation.value = forwardOrderInfo;
        forwardOrderInfoLoading.value = false;
      });
    }
  }

  loading.value = false;
}

function handleForwardOrderDeleteUsers(forwardOrderId: number) {
  if (confirm("Заменить код и удалить участников?")) {
    ChatSidebarService.deleteForwardOrderUsers(forwardOrderId, () => {
      renew();
    });
  }
}

</script>

<template>
  <div class="overflow-auto flex-grow-1 mt-2 mt-lg-2 mb-3 montserrat" v-if="!loading">
    <figure>
      <blockquote class="blockquote">
        <p>
          <i class="bi bi-chat-left-dots me-1"></i>
          Чат: {{ chat.displayName }}
          <i class="ms-1 btn btn-sm btn-outline-primary bi bi-arrow-clockwise"
             @click="renew"></i></p>
      </blockquote>
      <figcaption class="blockquote-footer">
        Идентификатор {{ chat.id }}
      </figcaption>
    </figure>

    <InformationText v-if="chat.type == ChatTypeEnum.SPECIAL_CHAT" :expand="true" icon="bi bi-info-circle" :mt="3"
                     tittle="Специальный чат" :show-icon="true">
      <p class="card-text mb-0 fs-7">
        Этот чат создан администратором для <strong>узконаправленного общения</strong> и решения специальных задач.
      </p>
    </InformationText>

    <InformationText v-else-if="chat.type == ChatTypeEnum.REQUEST_ORDER_CHAT" :expand="true" icon="bi bi-info-circle"
                     :mt="3" tittle="Чат для новых заказов" :show-icon="true">
      <p class="card-text fs-7">
        Здесь вы будете получать и обсуждать <strong>условия новых заказов</strong>. Система автоматически пришлёт
        уведомление, когда для вас появится подходящее предложение.
      </p>
      <p class="card-text mb-0 fs-7">
        <strong>Важно:</strong> общение в этом чате ведётся исключительно с вашим менеджером.
      </p>
    </InformationText>

    <InformationText v-else-if="chat.type == ChatTypeEnum.ADMIN_TALK_CHAT" :expand="true" icon="bi bi-info-circle"
                     :mt="3" tittle="Чат с администрацией" :show-icon="true">
      <p class="card-text fs-7">
        Здесь вы будете получать и обсуждать <strong>условия новых заказов</strong>. Система автоматически пришлёт
        уведомление, когда для вас появится подходящее предложение.
      </p>
      <p class="card-text fs-7">
        Направляйте сюда все ваши вопросы, связанные с работой платформы. Здесь вы можете получить консультацию и
        поддержку от администрации.
      </p>
      <p class="card-text mb-0 fs-7">
        Кроме того, в этот чат будут приходить <strong>системные уведомления</strong> о платежах и статусах оплаты.
      </p>
    </InformationText>

    <InformationText v-else-if="chat.type == ChatTypeEnum.ORDER_CHAT" :expand="true" icon="bi bi-info-circle"
                     :mt="3" tittle="Чат по выполнению заказа" :show-icon="true">
      <p class="card-text fs-7">
        Это рабочее пространство для обсуждения деталей и процесса выполнения текущего заказа.
      </p>
      <p class="card-text mb-0 fs-7">
        Здесь вы можете задавать вопросы менеджеру, а он, в свою очередь, будет контролировать ход работы и
        интересоваться прогрессом.
      </p>
    </InformationText>

    <InformationText v-else-if="chat.type == ChatTypeEnum.FORWARD_ORDER_CHAT" :expand="true" icon="bi bi-info-circle"
                     :mt="3" tittle="Прямой чат с заказчиком" :show-icon="true">
      <p class="card-text fs-7">
        Этот чат предназначен для <strong>прямого общения с заказчиком</strong> по всем вопросам, связанным с
        выполнением его заказа.
      </p>
      <p class="card-text mb-0 fs-7">
        <strong>Внимание:</strong> соблюдайте правила безопасности. Не передавайте личные данные и ведите всю
        коммуникацию строго в рамках этого чата.
      </p>
    </InformationText>

    <InformationText v-else-if="chat.type == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT" :expand="true"
                     icon="bi bi-info-circle"
                     :mt="3" tittle="Прямой заказ (Чат с администрацией)" :show-icon="true">
      <p class="card-text fs-7">
        Этот чат предназначен для <strong>прямого общения с заказчиком</strong> по всем вопросам, связанным с
        выполнением его заказа.
      </p>
      <p class="card-text mb-0 fs-7">
        Этот чат предназначен для коммуникации с заказчиком по любым вопросам, связанным с его прямым заказом, при
        участии и поддержке администрации.
      </p>
    </InformationText>

    <template v-if="chat.orderId">
      <LoadingSpinner v-if="orderChatInfoLoading" :margin-top="true"
                      text="Загружаем информацию о заказе"/>
      <div class="card mt-3" v-if="orderChatInformation">
        <div class="card-body p-2">
          <h5 class="card-title">
            <i class="bi bi-info-square me-1"></i>ТЗ №{{ orderChatInformation.techNumber + ' ' }}
            <OrderStatusIcon
                :name="orderChatInformation.status" :rus-name="orderChatInformation.statusRusName"/>
          </h5>
          <h6 class="card-subtitle mb-2 text-body-secondary" v-if="orderChatInformation.subject">
            Тема: {{ orderChatInformation.subject }}</h6>
          <div class="input-group mb-1" v-if="hasAuthorityManager(user.authorities)">
            <span class="input-group-text">Название</span>
            <textarea type="text" class="form-control form-control-sm disabled" disabled
                      :value="orderChatInformation.name"></textarea>
          </div>
          <div class="input-group mb-1">
            <span class="input-group-text">Дисциплина</span>
            <input type="text" class="form-control form-control-sm disabled" disabled
                   :value="orderChatInformation.disciplineName">
          </div>
          <div class="input-group mb-1" v-if="orderChatInformation.intermediateDeadline">
            <span class="input-group-text">Промежуточный с. д.</span>
            <input type="text" class="form-control form-control-sm disabled" disabled
                   :value="orderChatInformation.intermediateDeadline">
          </div>
          <div class="mb-1" v-for="item in orderChatInformation.additionalDates ?? []">
            <label class="form-label mb-0">{{ item.text }}</label>
            <input type="text" class="form-control form-control-sm disabled" disabled :value="item.time">
          </div>
          <div class="input-group mb-1" v-if="orderChatInformation.deadline">
            <span class="input-group-text">Окончательный срок</span>
            <input type="text" class="form-control form-control-sm disabled" disabled
                   :value="orderChatInformation.deadline">
          </div>

          <Accordion name="Детали">
            <AccordionItem name="Доп. информация" :open="false">
              <div class="input-group mb-1">
                <span class="input-group-text">Оригинальность</span>
                <input type="text" class="form-control form-control-sm disabled" disabled
                       :value="orderChatInformation.originality">
              </div>
              <div class="input-group mb-1">
                <span class="input-group-text">Система проверки</span>
                <input type="text" class="form-control form-control-sm disabled" disabled
                       :value="orderChatInformation.verificationSystem">
              </div>
              <div class="input-group mb-1">
                <span class="input-group-text">Автор</span>
                <input type="text" class="form-control form-control-sm disabled" disabled
                       :value="orderChatInformation.authorUsername">
              </div>
              <div class="input-group mb-1">
                <span class="input-group-text">Менеджер</span>
                <input type="text" class="form-control form-control-sm disabled" disabled
                       :value="orderChatInformation.managerUsername">
              </div>
            </AccordionItem>
            <AccordionItem name="Действия с заказом" :open="false" v-if="hasAuthorityManager(user.authorities)">
              <div class="input-group mb-1">
                <a class="btn btn-sm btn-primary" :href="`/change-order-status/${orderChatInformation.id}`"
                   target="_blank">Изменить
                  статус заказа</a>
              </div>
            </AccordionItem>
          </Accordion>

          <a class="btn btn-sm btn-outline-dark mt-3 p-1 w-100"
             target="_blank"
             :href="'/order-info/' + chat.orderId">Карточка заказа</a>
        </div>
      </div>

      <InformationText v-if="chat.orderId && hasAuthorityManager(user.authorities) && (chat.type == ChatTypeEnum.FORWARD_ORDER_CHAT || chat.type == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT)"
                       :expand="true" icon="bi-telegram"
                       :mt="3" tittle="Информация о прямом заказе" :show-icon="true">
        <LoadingSpinner v-if="forwardOrderInfoLoading" :margin-top="true"
                        text="Загружаем информацию о прямом заказе"/>
        <template class="card mt-3" v-if="forwardOrderChatInformation">
          <p class="m-0">Кол-во участников чата в телеграмме:
            <span class="h5">{{ forwardOrderChatInformation.botCount }}</span>
          </p>
          <p class="m-0">Код для присоединения к чату:
            <kbd class="ms-2 d-inline-block">/join {{ forwardOrderChatInformation.code }}</kbd>
          </p>
          <button id="delete-all-from-chat" type="submit" class="btn btn-sm btn-danger mt-2"
                  @click="handleForwardOrderDeleteUsers(forwardOrderChatInformation.forwardOrderId)">Удалить всех и
            заменить код.
          </button>
          <div class="form-text" id="delete-all-from-chat-help-text">При нажатии удалит всех участников
            чата. Изменится код для вступления в чат. Полезно если к чату присоединился не тот человек.
          </div>
        </template>
      </InformationText>

      <LoadingSpinner v-if="orderReviewsInfoLoading" :margin-top="true"
                      text="Загружаем информацию о проверках"/>
      <div class="card mt-3" v-if="orderChatInformation">
        <div class="card-body p-2">
          <h5 class="card-title">
            <i class="bi bi-files me-1"></i>Проверки работы
          </h5>
          <Accordion
              :name="'Проверно ' + orderReviews.filter(t => t.isApproved).length +
              ' / Не проверено ' + orderReviews.filter(t => !t.isApproved).length + ' / Всего ' + orderReviews.length">
            <template v-if="orderReviews && orderReviews.length > 0">
              <AccordionItem
                  :name="'Запрос от ' + review.createdAt + ' (' + (review.isApproved ? 'Проверено' : 'Не проверено') + ')'"
                  :open="index == 0"
                  v-for="(review, index) in orderReviews">
                <ul class="list-group list-group-flush">
                  <li class="list-group-item p-1" v-if="review.requestText">
                    <figure class="m-0 p-0">
                      <blockquote class="blockquote">
                        <pre class="mb-1 fs-7 line-break montserrat">{{ review.requestText }}</pre>
                      </blockquote>
                      <figcaption class="blockquote-footer mb-0">Сообщение для эксперта</figcaption>
                    </figure>
                  </li>
                  <li class="list-group-item p-1" v-if="review.isApproved">
                    <figure class="m-0 p-0">
                      <blockquote class="blockquote">
                        <pre class="mb-1 fs-7 line-break montserrat">{{ review.resultText }}</pre>
                        <p class="m-0 mt-2 mb-2 fs-7 line-break montserrat">Оценка <strong>{{
                            review.resultMark
                          }}</strong></p>
                      </blockquote>
                      <figcaption class="blockquote-footer mb-0">Сообщение от эксперта</figcaption>
                    </figure>
                  </li>
                  <li class="list-group-item p-1">
                    <div class="fs-7">
                      <label class="form-label mb-0">Файл для проверки:</label>
                      <a class="d-block" target="_blank"
                         :href="'/load-file/' + review.requestFile.id">{{ review.requestFile.name }}</a>
                    </div>
                  </li>
                  <li class="list-group-item p-1" v-if="review.resultFile">
                    <div class="fs-7">
                      <label class="form-label mb-0">Реузльтат проверки:</label>
                      <a class="d-block" target="_blank"
                         :href="'/load-file/' + review.resultFile.id"> {{ review.resultFile.name }}</a>
                    </div>
                  </li>
                  <li class="list-group-item p-1">
                    <a class="btn btn-sm btn-primary d-block m-2" target="_blank"
                       :href="'/expert-review-order-view/' + review.orderId + '/' + review.id">
                      Открыть карточку запроса ({{ (review.isApproved ? 'Проверено' : 'Не проверено') }})</a>
                  </li>
                </ul>
              </AccordionItem>
            </template>
            <div v-else>
              <i>Нет запросов на проверку</i>
            </div>
          </Accordion>
        </div>
      </div>
    </template>

  </div>

  <LoadingSpinner v-else text="Загружаем информацию" :margin-top="true"/>
</template>

<style scoped>

.fs-7 {
  font-size: 0.85rem !important;
}

.line-break {
  line-break: normal;
  white-space: pre-wrap;
}

.montserrat {
  font-family: "Montserrat", serif;
  font-optical-sizing: auto;
  font-style: normal;
}
</style>