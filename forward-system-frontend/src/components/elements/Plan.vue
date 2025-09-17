<script setup lang="ts">

import type {UserPlanViewDto} from "@/core/dto/UserPlanViewDto.ts";

interface Props {
  plan: UserPlanViewDto,
  showDate?: boolean
}

defineProps<Props>()

</script>

<template>
  <div class="d-flex p-1 flex-column gap-2">
    <div v-if="plan.targetSum" class="d-flex align-items-center">
      <div class="progress text-end h6 fw-bold m-0 ms-2 me-2 w-100 border border-success" role="progressbar"
           style="height: 15px">
        <div class="progress-bar text-dark overflow-visible text-end pe-2 ps-2 bg-warning"
             :style="{ 'width': Math.min(100, Math.min(100, Math.ceil(plan.orderSumPercent * 100))) + '%' }">
          {{ new Intl.NumberFormat("ru-RU").format(plan.orderSum) }} руб. | {{ Math.ceil(plan.orderSumPercent * 100 - 0.5) }}%
        </div>
      </div>
      <span class="badge rounded-pill bg-primary">
      {{ new Intl.NumberFormat("ru-RU").format(plan.targetSum) }} рублей
    </span>
    </div>
    <div v-if="plan.targetCount" class="d-flex align-items-center">
      <div class="progress text-end h6 fw-bold m-0 ms-2 me-2 w-100 border border-success" role="progressbar"
           style="height: 15px">
        <div class="progress-bar text-dark overflow-visible text-end pe-2 ps-2 bg-warning"
             :style="{ 'width': Math.min(100, Math.min(100, Math.ceil(plan.orderCountSumPercent * 100))) + '%' }">
          {{ new Intl.NumberFormat("ru-RU").format(plan.orderCountSum) }} шт. | {{ Math.ceil(plan.orderCountSumPercent * 100 - 0.5) }}%
        </div>
      </div>
      <span class="badge rounded-pill bg-primary">
      {{ new Intl.NumberFormat("ru-RU").format(plan.targetCount) }} заказов
    </span>
    </div>
  </div>
</template>

<style scoped>

</style>