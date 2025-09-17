<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import AdditionalDatesDisplay from "@/components/elements/AdditionalDatesDisplay.vue";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {ManagerOrdersDto} from "@/core/dto/ManagerOrdersDto.ts";
import {UserService} from "@/core/UserService.ts";
import type {UserDto} from "@/core/dto/UserDto.ts";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";

const loading = ref(true)
const showClosed = ref(false)
const user = ref<UserDto>()
const filterType = ref("all")
const dataTable = ref<ManagerOrdersDto[]>([])
const filteredTable = ref<ManagerOrdersDto[]>([])

function onFilterChange() {
  if (filterType.value == "all") {
    filteredTable.value = dataTable.value
  } else {
    filteredTable.value = dataTable.value.filter(value => value.orderStatus == filterType.value)
  }
}

function update() {
  UserService.fetchUserData(true, null, userDto => {

    user.value = userDto;

    fetch(`api/manager/get-manager-orders?showClosed=${showClosed.value}`, {method: "GET"})
        .then(value => value.json())
        .then(json => {
          dataTable.value = json as ManagerOrdersDto[];
          filteredTable.value = dataTable.value
          loading.value = false;
          onFilterChange();
        });
  });
}

watch(() => showClosed.value, () => {
  update();
});

onMounted(() => {
  update();
})

function getUserOrderPosition(order: ManagerOrdersDto) {
  return order?.participants?.filter(t => t.user.id == user.value.id)?.[0]?.typeRusName ?? "<Не указано>"
}

function isOrderCatcherAndInDistribution(order: ManagerOrdersDto) {
  return order?.participants?.filter(t => t.user.id == user.value.id)?.[0]?.type == ParticipantTypeEnum.CATCHER
      && (order.orderStatus == OrderStatusEnum.DISTRIBUTION || order.orderStatus == OrderStatusEnum.CREATED)
}

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div v-else class="container">
    <div class="my-auto">
      <div class="form-check form-switch">
        <input class="form-check-input" type="checkbox" role="switch" id="showClosed" v-model="showClosed">
        <label class="form-check-label" for="showClosed">Показывать завершённые</label>
      </div>
      <select class="form-select mb-2 mt-2" aria-label="Фильтр по статусам" v-model="filterType"
              @change="onFilterChange">
        <option selected value="all">Все статусы</option>
        <option :value="OrderStatusEnum.DISTRIBUTION">На распределении</option>
        <option :value="OrderStatusEnum.IN_PROGRESS">В работе</option>
        <option :value="OrderStatusEnum.REVIEW">На проверке</option>
        <option :value="OrderStatusEnum.FINALIZATION">Доработка</option>
        <option :value="OrderStatusEnum.GUARANTEE">На гарантии</option>
        <option :value="OrderStatusEnum.CLOSED" v-if="showClosed">Завершен</option>
      </select>
    </div>
    <table class="table table-bordered table-striped">
      <thead>
      <tr>
        <th scope="col" class="text-center align-content-center">ТЗ</th>
        <th scope="col" class="text-center align-content-center">Роль</th>
        <th scope="col" class="text-center align-content-center">Стадия</th>
        <th scope="col" class="text-center align-content-center">Тема</th>
        <th scope="col" class="text-center align-content-center">Промежуточный срок сдачи</th>
        <th scope="col" class="text-center align-content-center">Дополнительные сроки сдачи</th>
        <th scope="col" class="text-center align-content-center">Окончательный срок сдачи</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in filteredTable">
        <th scope="row" class="text-center align-content-center"><a
            :href="`/view-order/` + item.orderId">{{ item.orderTechNumber }}</a></th>
        <th scope="row" class="text-center align-content-center">
          {{ getUserOrderPosition(item) }}
          <a class="badge text-bg-primary mt-2" :href="'/order/distribution?orderId=' + item.orderId"
             v-if="isOrderCatcherAndInDistribution(item)">Перейти к распрделению</a>
        </th>
        <td class="text-center align-content-center">
          <OrderStatusIcon :name="item.orderStatus" :rus-name="item.orderStatusRus"/>
        </td>
        <td class="text-center align-content-center">{{ item.subject }}</td>
        <td class="text-center align-content-center">{{ item.intermediateDeadline }}</td>
        <td class="align-content-center">
          <AdditionalDatesDisplay :dates="item.additionalDates"/>
        </td>
        <td class="text-center align-content-center">{{ item.deadline }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
</style>
