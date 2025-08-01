<script setup lang="ts">
import {onMounted, ref} from "vue";
import type {UserDto} from "@/core/dto/UserDto.ts";
import UserAccountInfo from "@/components/elements/UserAccountInfo.vue";
import AuthorDisciplines from "@/components/elements/AuthorDisciplines.vue";
import {AuthorityEnum, hasAuthority} from "@/core/enum/AuthorityEnum.ts";
import AuthorRating from "@/components/elements/AuthorRating.vue";
import AuthorMedals from "@/components/elements/AuthorMedals.vue";

interface Props {
  fetchAutomatically: boolean,
  userId: number | null
}

const props = withDefaults(defineProps<Props>(), {
  fetchAutomatically: () => true,
  userId: () => null as any
});

const loading = ref(true);
const userData = ref<UserDto>(null);

onMounted(() => {
  const fetchUrl = props.fetchAutomatically ? '/api/user/info' : `/api/user/info/${props.userId}`;
  fetch(fetchUrl, {method: "GET"})
      .then(value => value.json())
      .then(value => {
        userData.value = value as UserDto;
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
  <div v-else class="container">
    <div class="row">
      <div class="col-12">
        <UserAccountInfo :user="userData"/>
        <AuthorDisciplines :user-id="userData.id" v-if="hasAuthority(userData.authorities, AuthorityEnum.AUTHOR)"/>
        <AuthorRating :user-id="userData.id" v-if="hasAuthority(userData.authorities, AuthorityEnum.AUTHOR)"/>
        <AuthorMedals :user-id="userData.id" v-if="hasAuthority(userData.authorities, AuthorityEnum.AUTHOR)"/>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>