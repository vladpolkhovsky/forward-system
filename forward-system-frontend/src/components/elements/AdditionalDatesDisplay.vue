<script setup lang="ts">
import type {AdditionalDate} from "@/core/dto/AuthorOrdersDto.ts";

interface Props {
  dates: AdditionalDate[]
}

let props = defineProps<Props>();
</script>

<template>
  <i v-if="props.dates.length == 0">Нет дополнительных сроков сдачи</i>
  <div v-else v-for="item in props.dates">
    <p style="font-size: 0.85rem; word-break: break-all" class="m-0"><strong>{{item.time}}</strong>: {{ item.text }}</p>
  </div>
</template>

<style scoped>

</style>