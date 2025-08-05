<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import {AuthorService} from "@/core/AuthorService.ts";
import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";

interface Props {
  size?: number
}

let props = withDefaults(defineProps<Props>(), {
  size: 15
});

const emit = defineEmits<{
  (e: 'select', value: AuthorShortDto): void,
  (e: 'ready'): void
}>()

const selectorSizes = ref(Array.from(new Set([10, 15, 30, 40, 50, 60, 100, props.size])).sort((a, b) => a - b))
const selectorSize = ref(props.size);
const selectedAuthor = ref<AuthorShortDto>();

watch(selectedAuthor, (newValue) => {
  emit('select', newValue)
});

const loading = ref(true);
const authors = ref<AuthorShortDto[]>();

const refresh = () => {
  loading.value = true;

  AuthorService.fetchAuthors(fetched => {
    authors.value = fetched;
    loading.value = false;
    emit("ready");
  })
}

defineExpose({
  refresh
});

onMounted(() => {
  refresh();
})

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div class="container shadow-sm p-3 mb-5 bg-body rounded" v-else>
    <div class="row">
      <div class="col-12">
        <h4 class="m-0">Авторы</h4>
      </div>
    </div>
    <div class="row mt-2 mb-2">
      <div class="col-12">
        <select class="form-select" v-model="selectedAuthor" :size="selectorSize">
          <option v-for="author in authors" :value="author">{{ author.username }}</option>
        </select>
      </div>
    </div>
    <div class="row">
      <div class="input-group input-group-sm">
        <span class="input-group-text">Кол-во строк</span>
        <select v-model="selectorSize">
          <option v-for="size in selectorSizes" :value="size">{{ size }}</option>
        </select>
        <button class="btn btn-outline-danger bi bi-arrow-clockwise" type="button" @click="refresh"></button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>