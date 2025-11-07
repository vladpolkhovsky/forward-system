<script setup lang="ts">

import {nextTick, onMounted, ref, watch} from "vue";
import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import {OrderService} from "@/core/OrderService.ts";
import type {OrderFullDto} from "@/core/dto/OrderFullDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import FullOrderCard from "@/components/elements/FullOrderCard.vue";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";
import type {ParticipantType} from "@/core/type/ParticipantType.ts";
import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";

interface OrderSearchModel {
  techNumber: number,
  showClosed: boolean,
  status: OrderStatusType,
  page: number,
  size: number
}

const search = ref<OrderSearchModel>({
  status: null,
  size: 50,
  page: 0,
  techNumber: null,
  showClosed: false
});

const loading = ref(true);
const filterChanged = ref(false);
const loadedPage = ref<PageableDto<OrderFullDto>>();

const useTableView = ref(false);

onMounted(() => {
  callSearch();
});

watch(() => search.value, () => {
  filterChanged.value = true;
}, {deep: true})

watch(() => search.value.showClosed, () => {
  if (search.value.status == OrderStatusEnum.CLOSED) {
    search.value.status = null;
  }
})

function callSearch() {
  loading.value = true;
  filterChanged.value = false;

  OrderService.searchOrders(search.value, (page) => {
    loadedPage.value = page;
    loading.value = false;
  })
}

function handleSearch() {
  callSearch();
}

function selectPage(page: number) {
  search.value.page = page;
  nextTick(() => {
    filterChanged.value = false;
    callSearch();
  })
}


function isDistributionStatus(order: OrderFullDto) {
  return order.orderStatus == OrderStatusEnum.DISTRIBUTION
      || order.orderStatus == OrderStatusEnum.CREATED;
}

function getResponsibleManager(order: OrderFullDto) {
  if (isDistributionStatus(order)) {
    return findUserWithRole(ParticipantTypeEnum.CATCHER, order)?.user?.username ?? order.createdBy.username;
  }
  return findUserWithRole(ParticipantTypeEnum.HOST, order)?.user?.username ?? order.createdBy.username;
}

function findUserWithRole(pt: ParticipantType, order: OrderFullDto): OrderParticipantDto {
  return order.participants?.filter(t => t.type == pt)?.[0] ?? null;
}

</script>

<template>
  <div class="container">
    <div class="row mb-2">
      <div class="col-12 col-md-4">
        <div class="input-group input-group-sm mb-2">
          <span class="input-group-text" id="basic-addon1">Номер заказа</span>
          <input type="number" class="form-control" placeholder="Номер заказа" min="0" max="100000"
                 v-model="search.techNumber">
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="input-group input-group-sm mb-2">
          <label class="input-group-text" for="orderStatus">Стадия заказа</label>
          <select class="form-select" id="orderStatus" v-model="search.status">
            <option selected :value="null">Все стадии</option>
            <option :value="OrderStatusEnum.CREATED">Создан</option>
            <option :value="OrderStatusEnum.WAITING">Ожидание</option>
            <option :value="OrderStatusEnum.DISTRIBUTION">Распределение</option>
            <option :value="OrderStatusEnum.ADMIN_REVIEW">На проверке</option>
            <option :value="OrderStatusEnum.IN_PROGRESS">В работе</option>
            <option :value="OrderStatusEnum.REVIEW">На проверке</option>
            <option :value="OrderStatusEnum.GUARANTEE">На гарантии</option>
            <option :value="OrderStatusEnum.CLOSED" v-if="search.showClosed">Заершён</option>
          </select>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" role="switch" id="showClosed" v-model="search.showClosed">
          <label class="form-check-label" for="showClosed">Показывать завершённые</label>
        </div>
        <div class="input-group input-group-sm mt-2">
          <label class="input-group-text" for="pageSize">Размер страницы</label>
          <select class="form-select" id="pageSize" v-model="search.size">
            <option :value="10">10</option>
            <option :value="25">25</option>
            <option :value="50">50</option>
            <option :value="100">100</option>
          </select>
        </div>
      </div>
    </div>
    <div class="row mb-2" v-if="filterChanged">
      <button class="btn btn-sm btn-primary text-center" @click="handleSearch">Поиск</button>
    </div>
    <div class="row mb-2" v-if="loadedPage">
      <nav class="d-flex gap-3">
        <div class="form-check form-switch">
          <input class="form-check-input" type="checkbox" role="switch" id="table-view" v-model="useTableView">
          <label class="form-check-label" for="table-view">
            Табличный вид
          </label>
        </div>
        <ul class="pagination pagination-sm flex-wrap">
          <li class="page-item disabled">
            <a class="page-link">Всего элементов: {{ loadedPage.totalElements }}</a>
          </li>
          <li class="page-item" v-for="pageIndex in loadedPage.totalPages">
            <a :class="['page-link', { active: (loadedPage.pageable.pageNumber == (pageIndex - 1)) }]"
               aria-current="page"
               @click="selectPage(pageIndex - 1)">{{ pageIndex }}</a>
          </li>
        </ul>
      </nav>
    </div>
    <div class="row mb-2" v-if="loadedPage">

    </div>
    <div class="row mb-2" v-if="loading">
      <LoadingSpinner text="Загружаем заказы..."/>
    </div>
    <template v-else>
      <div class="row mb-2" v-if="useTableView">
        <table class="table table-striped table-bordered text-center">
          <thead>
          <tr>
            <th scope="col">№</th>
            <th scope="col">Статус</th>
            <th scope="col">Дисциплина</th>
            <th scope="col">Стоимость / для автора</th>
            <th scope="col">Срок сдачи</th>
            <th scope="col">Ответсвенный менеджер</th>
          </tr>
          </thead>
          <tbody>
          <tr :key="order.id" v-for="order in loadedPage.content">
            <th scope="row"><a :href="`/view-order/` + order.id" target="_blank">{{ order.techNumber }}</a></th>
            <td><OrderStatusIcon :name="order.orderStatus" :rus-name="order.orderStatusRus"/></td>
            <td>{{ order.disciplineName }}</td>
            <td>{{ order.orderCost }} / {{ order.orderAuthorCost }}</td>
            <td>{{ order.deadline }}</td>
            <td>{{ getResponsibleManager(order) }}</td>
          </tr>
          </tbody>
        </table>
      </div>
      <div class="row mb-2" v-else>
        <div class="col-12">
          <FullOrderCard :order="order" :key="order.id" v-for="order in loadedPage.content"/>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>

</style>