<script setup lang="ts">
import type {DistributionLogDto} from "@/core/dto/DistributionLogDto.ts";
import Accordion from "@/components/elements/Accordion.vue";
import AccordionItem from "@/components/elements/AccordionItem.vue";
import {DistributionService} from "@/core/DistributionService.ts";

interface Props {
  logItems: DistributionLogDto[],
  disabled: boolean
}

defineProps<Props>();

const emit = defineEmits(['update']);

function handleSkipClick(itemId: number) {
  DistributionService.sendSkip(itemId, () => {
    emit('update');
  });
}

function handleDistributionStop(distributionId: number) {
  DistributionService.sendStopDistribution(distributionId, () => {
    emit('update');
  })
}

</script>

<template>
  <div class="card mt-3">
    <div class="card-body p-2">
      <h6 class="card-title">Автоматические распределения</h6>
      <Accordion>
        <AccordionItem :name="`Распределение от ${logItem.createdAt} (${logItem.statusTypeRus})`"
                       :open="index == logItems.length - 1"
                       v-for="(logItem, index) in logItems">
          <ol class="list-group list-group-numbered">
            <li class="list-group-item d-flex justify-content-between align-items-start" v-for="item in logItem.items">
              <div class="ms-2 me-auto">
                <span class="fw-bold">{{ item.user.username }}</span>
                <div class="mt-1" v-if="item.waitStart">Отправлено в {{ item.waitStart }}</div>
                <div class="mt-1" v-if="item.waitUntil">Ожидаем до {{ item.waitUntil }}</div>
                <button class="btn btn-sm btn-outline-danger mt-2 p-1"
                        @click="handleSkipClick(item.id)"
                        :disabled="disabled"
                        type="button"
                        v-if="item.statusType == 'TALK' || item.statusType == 'IN_PROGRESS'">Пропустить
                </button>
              </div>
              <span class="badge text-bg-primary rounded-pill">{{ item.statusTypeRus }}</span>
            </li>
          </ol>
          <button class="btn btn-sm btn-danger mt-1 p-1"
                  v-if="logItem.statusType == 'IN_PROGRESS'"
                  :disabled="disabled"
                  type="button"
                  @click="handleDistributionStop(logItem.id)">
            <i class="bi bi-sign-stop me-1"></i> Завершить принудительно</button>
        </AccordionItem>
      </Accordion>
    </div>
  </div>
</template>

<style scoped>

</style>