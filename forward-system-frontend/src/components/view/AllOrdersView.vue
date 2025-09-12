<script setup lang="ts">

import {nextTick, onMounted, ref, watch} from "vue";
import type {OrderStatusType} from "@/core/type/OrderStatusType.ts";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import {OrderService} from "@/core/OrderService.ts";
import type {OrderFullDto} from "@/core/dto/OrderFullDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import FullOrderCard from "@/components/elements/FullOrderCard.vue";

interface OrderSearchModel {
  techNumber: number,
  showClosed: boolean,
  status: OrderStatusType,
  page: number,
  size: number
}

const search = ref<OrderSearchModel>({
  status: null,
  size: 25,
  page: 0,
  techNumber: null,
  showClosed: false
});

const loading = ref(true);
const filterChanged = ref(false);
const loadedPage = ref<PageableDto<OrderFullDto>>();

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
      <nav>
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
    <div class="row mb-2" v-if="loading">
      <LoadingSpinner text="Загружаем заказы..."/>
    </div>
    <div class="row mb-2" v-else>
      <div class="col-12">
        <FullOrderCard :order="order" :key="order.id" v-for="order in loadedPage.content"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>