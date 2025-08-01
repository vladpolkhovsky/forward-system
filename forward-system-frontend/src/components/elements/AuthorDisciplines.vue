<script setup lang="ts">
import {onMounted, ref} from "vue";
import type {AuthorDisciplinesDto} from "@/core/dto/AuthorDisciplinesDto.ts";

interface Props {
  userId: number
}

const props = defineProps<Props>();
const loading = ref(true);
const disciplines = ref<AuthorDisciplinesDto>(null);

onMounted(() => {
  fetch(`/api/author/get-author-disciplines/${props.userId}`)
      .then(value => value.json())
      .then(value => {
        disciplines.value = value as AuthorDisciplinesDto;
        loading.value = false;
      })
})
</script>

<template>
  <div class="container" v-if="loading">
    <div class="d-flex">
      <div class="spinner-border m-auto" role="status">
        <span class="visually-hidden">Loading...</span>
      </div>
    </div>
  </div>
  <div class="mb-3" v-else>
    <p class="h4">Дисциплины пользователя</p>
    <div class="row">
      <div class="col-12 col-md-4">
        <div class="card mb-4 text-bg-success">
          <div class="card-body">
            <p class="fs-5 fw-bold text-center">Напишу на 5</p>
            <ul class="list-group list-group-flush">
              <li class="list-group-item text-bg-success fw-medium" v-for="discipline in disciplines.excellent">{{ discipline }}</li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card mb-4 text-bg-primary">
          <div class="card-body">
            <p class="fs-5 fw-bold text-center">Напишу на 4-5</p>
            <ul class="list-group list-group-flush">
              <li class="list-group-item text-bg-primary fw-medium" v-for="discipline in disciplines.good">{{ discipline }}</li>
            </ul>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="card mb-4 text-bg-secondary">
          <div class="card-body">
            <p class="fs-5 fw-bold text-center">Могу рассмотреть</p>
            <ul class="list-group list-group-flush">
              <li class="list-group-item text-bg-secondary fw-medium" v-for="discipline in disciplines.maybe">{{ discipline }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>