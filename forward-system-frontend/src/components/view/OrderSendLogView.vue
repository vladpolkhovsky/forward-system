<script setup lang="ts">

import UserSelector from "@/components/elements/UserSelector.vue";
import type {UserDto} from "@/core/dto/UserDto.ts";
import type {PageableDto} from "@/core/dto/PageableDto.ts";
import type { DistributionRequestLogDto } from "@/core/dto/DistributionRequestLogDto.ts";
import {onMounted, ref, watch} from "vue";
import {DistributionRequestLogService} from "@/core/DistributionRequestLogService.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";

const selectedAuthor = ref<UserDto>(null);
const selectedManager = ref<UserDto>(null);
const searchOrderLine = ref("")
const logs = ref<PageableDto<DistributionRequestLogDto>>(null);
const cPage = ref(0)
const loading = ref<boolean>(true);

watch([selectedAuthor, selectedManager, searchOrderLine], ([author, manager, order]) => {
  loadPage(manager?.id, author?.id, order, cPage.value);
});

onMounted(() => {
  loadPage(null, null, null, 0)
});

function load(page: number) {
  loadPage(selectedManager?.value?.id, selectedAuthor?.value?.id, searchOrderLine?.value, page);
}

function loadPage(managerId: number, authorId: number, order: string, page: number) {
  loading.value = true;
  cPage.value = page

  DistributionRequestLogService.getDistributionLogs({ authorId: authorId, managerId: managerId, order: order, page: page }, pageable => {
    logs.value = pageable
    loading.value = false;
  })
}

</script>

<template>
  <div class="row mb-3">
    <div class="col-6">
      <UserSelector authority="MANAGER" tittle="Выбор менеджера" @select="user => selectedManager = user"/>
    </div>
    <div class="col-6">
      <UserSelector authority="AUTHOR" tittle="Выбор автора" @select="user => selectedAuthor = user"/>
    </div>
  </div>
  <div class="row mb-3">
    <div class="input-group mb-3">
      <span class="input-group-text">ТЗ</span>
      <input type="number" class="form-control" v-model="searchOrderLine" placeholder="Номер ТЗ">
      <button class="btn btn-outline-secondary bi bi-x-square" type="button" @click="searchOrderLine = ''"></button>
    </div>
  </div>
  <div class="row mb-3" v-if="logs && logs.totalPages > 1">
    <nav>
      <ul class="pagination pagination-sm flex-wrap">
        <li class="page-item">
          <a class="page-link" @click="load(Math.max(0, cPage - 1))" aria-label="Previous">
            <span aria-hidden="true">&laquo;</span>
          </a>
        </li>
        <li :class="['page-item', { 'active': pageIndex - 1 == cPage}]" v-for="pageIndex in logs.totalPages">
          <a class="page-link" @click="load(pageIndex - 1)">{{ pageIndex }}</a>
        </li>
        <li class="page-item">
          <a class="page-link" @click="load(Math.min(logs.totalPages, cPage + 1))" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
          </a>
        </li>
      </ul>
    </nav>
  </div>
  <div class="row mb-3">
    <LoadingSpinner v-if="loading" text="Загружаем данные" />
    <table v-else class="table table-striped table-bordered text-center">
      <thead>
      <tr>
        <th scope="col">Менеджер</th>
        <th scope="col">Автор</th>
        <th scope="col">Заказ</th>
        <th scope="col">Дата</th>
      </tr>
      </thead>
      <tbody>
      <tr v-if="logs.totalElements > 0" v-for="log in logs.content" :key="log.id">
        <td>{{ log.managerUsername }}</td>
        <td>{{ log.authorUsername }}</td>
        <td>{{ log.orderTechNumber }}</td>
        <td>{{ log.createdAt }}</td>
      </tr>
      <tr v-else>
        <td colspan="4"><i>Нет данных...</i></td>
      </tr>
      </tbody>
    </table>
  </div>
</template>

<style scoped>

</style>