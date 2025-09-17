<script setup lang="ts">

import type {OrderFullDto} from "@/core/dto/OrderFullDto.ts";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import type {ParticipantType} from "@/core/type/ParticipantType.ts";
import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";
import {Dropdown} from 'bootstrap'
import {onUpdated, ref, useId} from "vue";

interface Props {
  order: OrderFullDto
}

const props = defineProps<Props>();
const dropdownId = ref(useId());

const dropdownRef = ref<HTMLButtonElement | null>(null);

onUpdated(() => {
  if (dropdownRef.value) {
    new Dropdown('#' + dropdownId.value)
  }
})

function isDistributionStatus() {
  return props.order.orderStatus == OrderStatusEnum.DISTRIBUTION
      || props.order.orderStatus == OrderStatusEnum.CREATED;
}

function getResponsibleManager() {
  if (isDistributionStatus()) {
    return findUserWithRole(ParticipantTypeEnum.CATCHER)?.user?.username ?? props.order.createdBy.username;
  }
  return findUserWithRole(ParticipantTypeEnum.HOST)?.user?.username ?? props.order.createdBy.username;
}

function findUserWithRole(pt: ParticipantType): OrderParticipantDto {
  return props.order.participants?.filter(t => t.type == pt)?.[0] ?? null;
}

function confirmDeleteOrder(event: SubmitEvent) {
  if (confirm("Удалить ТЗ " + props.order.techNumber)) {
    return true;
  }
  event.preventDefault();
}

</script>

<template>
  <div class="card mb-2">
    <h6 class="card-header p-1 ps-2">
      <a :href="`/view-order/` + order.id" target="_blank">Заказ / ТЗ № {{ order.techNumber }} <span class="ms-3">
          <OrderStatusIcon :name="order.orderStatus" :rus-name="order.orderStatusRus"/>
        </span>
      </a>
    </h6>
    <div class="card-body p-2">
      <div class="container">
        <div class="row">
          <div class="col-12 col-md-8">
            <dl class="row mb-2">
              <dt class="col-md-6">Дисциплина</dt>
              <dd class="col-md-6">{{ order.disciplineName }}</dd>

              <dt class="col-md-6">Стоимость заказа / для автора</dt>
              <dd class="col-md-6">{{ order.orderCost }} / {{ order.orderAuthorCost }}</dd>

              <dt class="col-md-6">Срок сдачи</dt>
              <dd class="col-md-6">{{ order.deadline }}</dd>

              <dt class="col-md-6">Ответсвенный менеджер</dt>
              <dd class="col-md-6">{{ getResponsibleManager() }}</dd>

              <dt class="col-md-6" v-if="order.distributionDays != null">Дней на распределении</dt>
              <dd class="col-md-6" v-if="order.distributionDays != null">{{ order.distributionDays }}</dd>
            </dl>
          </div>
          <div class="col-12 col-md-4">
            <div class="row">
              <div class="col-12 mb-2">

                <div class="row">
                  <a class="btn btn-sm btn-primary mb-2" target="_blank" :href="'/update-order/' + order.id"><i
                      class="bi bi-pencil-square me-2"></i>
                    Открыть для изменения
                  </a>
                </div>

                <div class="row">
                  <a class="btn btn-sm btn-primary mb-2" target="_blank"
                     :href="'/order/distribution?orderId=' + order.id"
                     v-if="isDistributionStatus()">
                    <i class="bi bi-people me-2"></i> Распределение
                  </a>
                </div>

                <div class="row">
                  <a type="button" class="btn btn-sm btn-primary position-relative mb-2"
                     target="_blank"
                     :href="'/new-messenger-v3?tab=orders&chatId=' + order.orderChatId"
                     v-if="order.orderChatId && !isDistributionStatus()">
                    <i class="bi bi-chat-dots me-2"></i> Чат заказа
                    <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
                          v-if="order.orderChatIdNewMessages > 0">
                      {{ order.orderChatIdNewMessages }}
                    </span>
                  </a>
                </div>

                <div class="row">
                  <a class="btn btn-sm btn-primary mb-2" :href="'/change-order-status/' + order.id"
                     target="_blank"><i class="bi bi-app-indicator me-2"></i> Изменить статус</a>
                </div>

                <div class="row">
                  <form method="post" class="p-0" :action="'/delete-order/' + order.id"
                        @submit="confirmDeleteOrder($event as SubmitEvent)">
                    <button type="submit" class="btn btn-sm btn-danger mb-2 w-100">
                      <i class="bi bi-x-circle me-2"></i> Удалить
                    </button>
                  </form>
                </div>

              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="card-footer text-body-secondary p-1 ps-2">
      Создан: {{ order.createdAt }} / {{ order.createdBy.username }}
    </div>
  </div>
</template>

<style scoped>

</style>