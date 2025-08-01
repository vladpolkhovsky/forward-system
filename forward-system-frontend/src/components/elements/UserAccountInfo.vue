<script setup lang="ts">
import type {UserDto} from "@/core/dto/UserDto.ts";
import {authorityToRusName} from "@/core/enum/AuthorityEnum.ts";

interface Props {
  user: UserDto
}

let props = defineProps<Props>();
</script>

<template>
  <div class="row mb-3">
    <p class="h4">Инофрмация о пользователе</p>
    <div class="card mb-4">
      <div class="card-body">
        <div class="row">
          <div class="col-12 col-md-6 col-lg-4">
            <div class="row mb-2">
              <label for="lastName" class="col-12 col-lg-6 col-form-label text-lg-end">Фамиллия</label>
              <div class="col-12 col-lg-6">
                <input id="lastName" class="form-control" type="text" :value="props.user.lastName" readonly>
              </div>
            </div>
            <div class="row mb-2">
              <label for="firstName" class="col-12 col-lg-6 col-form-label text-lg-end">Имя</label>
              <div class="col-12 col-lg-6">
                <input id="firstName" class="form-control" type="text" :value="props.user.firstName" readonly>
              </div>
            </div>
            <div class="row mb-2">
              <label for="username" class="col-12 col-lg-6 col-form-label text-lg-end">Логин</label>
              <div class="col-12 col-lg-6">
                <input id="username" class="form-control" type="text" :value="props.user.username" readonly>
              </div>
            </div>
            <div class="row mb-2">
              <label for="id" class="col-12 col-lg-6 col-form-label text-lg-end">Идентификатор</label>
              <div class="col-12 col-lg-6">
                <input id="id" class="form-control" type="text" :value="props.user.id" readonly>
              </div>
            </div>
          </div>
          <div class="col-12 col-md-6 col-lg-3">
            <p class="fw-bold mb-1">Роли</p>
            <ul class="list-group list-group-flush">
              <li class="list-group-item" v-for="item in props.user.authorities">{{ authorityToRusName(item) }}</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>