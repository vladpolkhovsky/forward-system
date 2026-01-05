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
import InformationTextPlain from "@/components/elements/InformationTextPlain.vue";
import type {ReviewCreateRequestDto} from "@/core/dto/ReviewCreateRequestDto.ts";
import {AttachmentsService} from "@/core/AttachmentsService.ts";
import {ReviewService} from "@/core/ReviewService.ts";
import {OrderService} from "@/core/OrderService.ts";
import type {OrderShortDto} from "@/core/dto/OrderShortDto.ts";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";
import type {ChatFileAttachmentDto} from "@/core/dto/ChatFileAttachmentDto.ts";
import {ChatService} from "@/core/ChatService.ts";

interface Props {
  user: UserDto,
  chat: ChatShortDto
}

const props = defineProps<Props>();
const orderChatInformation = ref<OrderChatInformation>();
const forwardOrderChatInformation = ref<ForwardOrderChatInformation>();
const fetchedOrder = ref<OrderShortDto>();
const orderReviews = ref<OrderReviewDto[]>([]);
const chatFiles = ref<ChatFileAttachmentDto[]>([]);

const loading = ref(true);
const orderChatInfoLoading = ref(true);
const orderLoading = ref(true);
const orderReviewsInfoLoading = ref(true);
const loadingChatDocuments = ref(true);
const forwardOrderInfoLoading = ref(true);
const createReviewRequest = ref<ReviewCreateRequestDto>({});

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
  orderLoading.value = true;
  loadingChatDocuments.value = true;

  orderReviews.value = null;
  orderChatInformation.value = null;
  forwardOrderChatInformation.value = null;

  createReviewRequest.value.fileId = null;
  createReviewRequest.value.orderId = null;
  createReviewRequest.value.chatId = null;
  createReviewRequest.value.requestText = null;

  ChatService.fetchChatFiles(props.chat.id, page => {
    chatFiles.value = page.content;
    loadingChatDocuments.value = false;
  });

  if (props.chat.orderId) {
    OrderService.fetchOrder(props.chat.orderId, order => {
      fetchedOrder.value = order;
      orderLoading.value = false;
    });

    ChatSidebarService.loadChatOrderInformation(props.chat.orderId, orderChatInfo => {
      if (props.chat.orderId == orderChatInfo.id) {
        orderChatInformation.value = orderChatInfo;
        orderChatInfoLoading.value = false;

        createReviewRequest.value.chatId = props.chat.id;
        createReviewRequest.value.orderId = orderChatInfo.id;
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

function handleForwardOrderSubmitFileStatusChange(forwardOrderId: number) {
  if (confirm("Изменить возможность автора отправлять файлы?")) {
    ChatSidebarService.changeForwardOrderFileSubmissionStatus(forwardOrderId, () => {
      renew();
    });
  }
}

function handleForwardOrderPaidStatusChange(forwardOrderId: number) {
  if (confirm("Изменить статус оплаты заказа?")) {
    ChatSidebarService.changeForwardOrderPaidStatus(forwardOrderId, () => {
      renew();
    });
  }
}

function handleReviewFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.item(0) ?? null;

  if (!file) {
    return
  }

  let formData = new FormData();
  formData.append("file", file, "Проверка " + file.name);

  AttachmentsService.upload(formData, (id) => {
    createReviewRequest.value.fileId = id;
  });
}

function handleSubmitCreateReviewRequest() {
  ReviewService.createReviewRequest(createReviewRequest.value, () => {
    renew();
  });
}

function handleDeleteReview(reviewId: number) {
  if (confirm("Удалить запрос на проверку?")) {
    ReviewService.deleteReviewRequest(reviewId, props.chat.id, () => {
      renew();
    });
  }
}

function getExpertName() {
  const experts = fetchedOrder.value.participants
      .filter(t => t.type == ParticipantTypeEnum.EXPERT)
      .map(t => t.user.username);
  return experts.length > 0 ? experts.join("; ") : "<Не назначен>";
}

function handleDeleteExpertFromOrder() {
  OrderService.deleteExpertFromOrder(fetchedOrder.value.id, () => {
    renew();
  });
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
            <AccordionItem name="Экспертный отдел" :open="false"
                           v-if="hasAuthorityManager(user.authorities) && orderChatInformation.expertGroupId">
              <LoadingSpinner text="Загружаем информацию о заказе" v-if="orderLoading"/>
              <template v-else>
                <div class="input-group mb-1">
                  <span class="input-group-text">Экспертная группа</span>
                  <input type="text" class="form-control form-control-sm disabled" disabled
                         :value="orderChatInformation.expertGroupName">
                </div>
                <div class="input-group mb-1">
                  <span class="input-group-text">Назначенный эксперт</span>
                  <input type="text" class="form-control form-control-sm disabled" disabled
                         :value="getExpertName()">
                </div>
                <div class="input-group mb-0">
                  <button class="btn btn-sm btn-danger" @click="handleDeleteExpertFromOrder">Удалить эксперта из
                    заказа
                  </button>
                </div>
              </template>
            </AccordionItem>
            <AccordionItem name="Действия с заказом" :open="false" v-if="hasAuthorityManager(user.authorities)">
              <div class="input-group mb-1">
                <a class="btn btn-sm btn-primary" :href="`/update-order/${orderChatInformation.id}`"
                   target="_blank">Изменить карточку заказа</a>
              </div>
              <div class="input-group mb-1">
                <a class="btn btn-sm btn-primary" :href="`/change-order-status/${orderChatInformation.id}`"
                   target="_blank">Изменить
                  статус заказа</a>
              </div>
              <div class="input-group mb-0">
                <a class="btn btn-sm btn-success" :href="`/expert-review-order/${orderChatInformation.id}`"
                   target="_blank">Отправить на проверку</a>
              </div>
            </AccordionItem>
          </Accordion>

          <a class="btn btn-sm btn-outline-dark mt-3 p-1 w-100"
             target="_blank"
             :href="'/order-info/' + chat.orderId">Карточка заказа</a>
        </div>
      </div>

      <InformationText v-if="chat.orderId && (chat.type == ChatTypeEnum.FORWARD_ORDER_CHAT
          || chat.type == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT)" :expand="true"
                       icon="bi-patch-exclamation" :mt="3" tittle="Статус прямого заказа" :show-icon="true">

        <LoadingSpinner v-if="forwardOrderInfoLoading" :margin-top="true"
                        text="Загружаем информацию о прямом заказе"/>
        <div class="p-2 d-flex gap-2 justify-content-around align-items-center flex-wrap"
             v-if="forwardOrderChatInformation">
            <span class="badge text-bg-success fs-7"
                  v-if="forwardOrderChatInformation.isOrderPaid">Заказ оплачен</span>
          <span class="badge text-bg-danger fs-7" v-else>Заказ не оплачен</span>
          <span class="badge text-bg-success fs-7"
                v-if="forwardOrderChatInformation.isAuthorCanSubmitFiles">Файлы разрешены</span>
          <span class="badge text-bg-danger fs-7" v-else>Файлы запрещены</span>
        </div>
      </InformationText>

      <InformationTextPlain
          v-if="chat.orderId && hasAuthorityManager(user.authorities)
            && (chat.type == ChatTypeEnum.FORWARD_ORDER_CHAT || chat.type == ChatTypeEnum.FORWARD_ORDER_ADMIN_CHAT)"
          icon="bi-telegram" :mt="3" tittle="Информация о прямом заказе" :show-icon="true">

        <LoadingSpinner v-if="forwardOrderInfoLoading" :margin-top="true"
                        text="Загружаем информацию о прямом заказе"/>

        <Accordion v-if="forwardOrderChatInformation">
          <AccordionItem name="Участники" :open="true">
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
          </AccordionItem>
          <AccordionItem name="Действия с прямым заказом" :open="false">
            <button id="change-file-paid-status"
                    @click="handleForwardOrderPaidStatusChange(forwardOrderChatInformation.forwardOrderId)"
                    :class="[ 'form-control btn btn-sm', {
              'btn-danger' : forwardOrderChatInformation.isOrderPaid,
              'btn-success' : !forwardOrderChatInformation.isOrderPaid
            }]">{{
                !forwardOrderChatInformation.isOrderPaid ?
                    'Пометить заказ как оплаченный' : 'Пометить заказ как не оплаченный'
              }}
            </button>

            <button id="change-file-submit-status"
                    @click="handleForwardOrderSubmitFileStatusChange(forwardOrderChatInformation.forwardOrderId)"
                    :class="[ 'form-control btn btn-sm mt-2', {
              'btn-danger' : forwardOrderChatInformation.isAuthorCanSubmitFiles,
              'btn-success' : !forwardOrderChatInformation.isAuthorCanSubmitFiles
            }]">{{
                !forwardOrderChatInformation.isAuthorCanSubmitFiles ?
                    'Разрешить отправку файлов' : 'Запретить отправлять файлы'
              }}
            </button>
          </AccordionItem>
        </Accordion>
      </InformationTextPlain>

      <LoadingSpinner v-if="orderChatInfoLoading" :margin-top="true"
                      text="Загружаем информацию группе проверки"/>
      <div class="card mt-3" v-if="orderChatInformation">
        <div class="card-body p-2">
          <h5 class="card-title m-0">
            <i class="bi bi-journal-arrow-up me-1"></i> Создать запрос на проверку
          </h5>
        </div>
        <form action="/api/review/create-by-order-group" enctype="multipart/form-data" method="post"
              class="p-2"
              @submit.prevent="handleSubmitCreateReviewRequest"
              v-if="orderChatInformation.expertGroupId">
          <div class="form-floating mb-2">
            <textarea class="form-control" id="request-note" name="request-note" rows="5" required
                      v-model="createReviewRequest.requestText"
                      style="min-height: 100px" minlength="20"></textarea>
            <label for="request-note">Описание для эксперта</label>
            <div class="form-text" id="request-note-help-text">Минимум 20 символов</div>
          </div>
          <div class="mb-2">
            <label for="request-file" class="form-label">Файл на проверку</label>
            <input type="file" required class="form-control" id="request-file" name="request-file"
                   @change="handleReviewFileChange">
            <div class="form-text" id="request-file-help-text">Прикрепите файл для проверки экспертом</div>
          </div>
          <button type="submit" class="btn btn-primary btn-sm d-block"
                  :disabled="!(createReviewRequest.fileId && createReviewRequest.orderId && createReviewRequest.chatId)"
          >Отправить запрос на проверку
          </button>
        </form>
        <div v-else class="alert alert-warning m-2" role="alert">
          Данному заказу не назначена группа экспертов. Создать запрос на проверку невозможно.
        </div>
      </div>

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
                    <a class="btn btn-sm btn-danger d-block m-2" target="_blank"
                       v-if="hasAuthorityManager(user.authorities) && !review.isApproved"
                       @click="handleDeleteReview(review.id)">
                      Удалить запрос на проверку</a>
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

    <div class="mt-3">
      <Accordion>
        <AccordionItem name="Приложенные в чат документы" :open="false">
          <LoadingSpinner v-if="loadingChatDocuments" text="Загружаем файлы"/>
          <template v-else>
            <div class="card" v-if="chatFiles.length == 0">
              <div class="card-body p-2">
                Чат не содержит файлов.
              </div>
            </div>
            <div class="card mb-2" v-for="file in chatFiles" :key="file.attachment.id" v-if="chatFiles.length > 0">
              <div class="card-body p-2">
                <h5 class="card-title fs-7 m-0"><a target="_blank" :href="'/load-file/' + file.attachment.id">{{
                    file.attachment.name
                  }}</a></h5>
                <p class="card-text m-0 fs-7">{{ file.createdAt }} / {{ file.user.username }}</p>
              </div>
            </div>
          </template>
        </AccordionItem>
      </Accordion>
    </div>
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