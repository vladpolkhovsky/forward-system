<script setup lang="ts">
import type {DistributionPerson} from "@/core/dto/DistributionPerson.ts";
import {onMounted, ref, watch} from "vue";

interface Props {
  persons: DistributionPerson[]
}

const props = defineProps<Props>();
const sorted = ref<DistributionPerson[]>(props.persons);
const waitMinutes = ref<number>();

onMounted(() => {
  refreshOrder();
});

watch(() => props.persons, () => {
  refreshOrder();
});

function refreshOrder() {
  console.log('before', sorted.value)

  let max = sorted.value.length > 0 ? Math.max(...sorted.value.map(t => t.order ?? 0)) : 0;
  const oldSorted = sorted.value;

  sorted.value = props.persons;
  sorted.value.forEach(t => {
    t.order = oldSorted.filter(e => e.userId == t.userId)[0]?.order ?? ++max;
  });

  sorted.value = sorted.value.sort((a, b) => a.order - b.order);

  console.log('after', sorted.value)
}

function handleUp(index: number) {
  let tmp = sorted.value[index - 1].order;
  sorted.value[index - 1].order = sorted.value[index].order;
  sorted.value[index].order = tmp;
  refreshOrder();
}

function handleDown(index: number) {
  let tmp = sorted.value[index + 1].order;
  sorted.value[index + 1].order = sorted.value[index].order;
  sorted.value[index].order = tmp;
  refreshOrder();
}

defineExpose({
  waitMinutes,
  sorted
})

</script>

<template>
  <div class="card mt-3">
    <div class="card-body p-2">
      <h6 class="card-title">Изменение порядка автоматического распределения</h6>
      <ol class="list-group list-group-numbered">
        <li class="list-group-item d-flex flex-row justify-content-between align-items-center"
            v-for="(item, index) in sorted">
          <span class="fw-bold h6">{{ item.author.username }}</span>
          <span class="d-flex flex-row gap-1">
            <i class="btn btn-sm p-0 btn-outline-primary bi bi-arrow-up" @click="handleUp(index)"></i>
            <i class="btn btn-sm p-0 btn-outline-primary bi bi-arrow-down" @click="handleDown(index)"></i>
          </span>
        </li>
      </ol>
      <div class="input-group input-group-sm mt-3">
        <span class="input-group-text">Ожидание ответа (мин)</span>
        <input type="number" required class="form-control" min="5" max="1440"
               v-model="waitMinutes"
               placeholder="1 час = 60 мин, пишем 60. 2ч = 120 и тд.">
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>