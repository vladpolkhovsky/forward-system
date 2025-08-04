<script setup lang="ts">

import {onMounted, ref} from "vue";
import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import AdditionalDatesDisplay from "@/components/elements/AdditionalDatesDisplay.vue";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";

const loading = ref(true)
const filterType = ref("all")
const dataTable = ref<AuthorOrdersDto[]>([])
const filteredTable = ref<AuthorOrdersDto[]>([])

function onFilterChange() {
  if (filterType.value == "all") {
    filteredTable.value = dataTable.value
  } else {
    filteredTable.value = dataTable.value.filter(value => value.orderStatus == filterType.value)
  }
}

onMounted(() => {
  fetch("api/author/get-author-orders", {method: "GET"})
      .then(value => value.json())
      .then(json => {
        dataTable.value = json as AuthorOrdersDto[];
        filteredTable.value = dataTable.value
        loading.value = false;
      })
})

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div v-else class="container">
    <select class="form-select mb-2 mt-2" aria-label="Фильтр по статусам" v-model="filterType" @change="onFilterChange">
      <option selected value="all">Фильтр по статусам</option>
      <option :value="OrderStatusEnum.IN_PROGRESS">В работе</option>
      <option :value="OrderStatusEnum.REVIEW">На проверке</option>
      <option :value="OrderStatusEnum.FINALIZATION">Доработка</option>
      <option :value="OrderStatusEnum.GUARANTEE">На гарантии</option>
      <option :value="OrderStatusEnum.CLOSED">Завершен</option>
    </select>
    <table class="table table-bordered table-striped">
      <thead>
      <tr>
        <th scope="col" class="text-center align-content-center">ТЗ</th>
        <th scope="col" class="text-center align-content-center">Стадия</th>
        <th scope="col" class="text-center align-content-center">Тема</th>
        <th scope="col" class="text-center align-content-center">Оригинальность</th>
        <th scope="col" class="text-center align-content-center">Промежуточный срок сдачи</th>
        <th scope="col" class="text-center align-content-center">Дополнительные сроки сдачи</th>
        <th scope="col" class="text-center align-content-center">Статус оплаты</th>
        <th scope="col" class="text-center align-content-center">Окончательный срок сдачи</th>
      </tr>
      </thead>
      <tbody>
      <tr v-for="item in filteredTable">
        <th scope="row" class="text-center align-content-center"><a
            :href="`/order-info/` + item.orderId">{{ item.orderTechNumber }}</a></th>
        <td class="text-center align-content-center">
          <OrderStatusIcon :name="item.orderStatus" :rus-name="item.orderStatusRus"/>
        </td>
        <td class="text-center align-content-center">{{ item.subject }}</td>
        <td class="text-center align-content-center">{{ item.originality + '%' }}</td>
        <td class="text-center align-content-center">{{ item.intermediateDeadline }}</td>
        <td class="align-content-center">
          <AdditionalDatesDisplay :dates="item.additionalDates"/>
        </td>
        <td class="text-center align-content-center"><span class="text-nowrap">{{ item.paymentStatus }}</span></td>
        <td class="text-center align-content-center">{{ item.deadline }}</td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>
</style>
