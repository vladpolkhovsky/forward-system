<script setup lang="ts">
import {onMounted, ref, watch} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {AuthorityType} from "@/core/type/AuthorityType.ts";
import type {UserDto} from "@/core/dto/UserDto.ts";
import {UserService} from "@/core/UserService.ts";

interface Props {
  tittle?: string,
  authority: AuthorityType,
  defaultSelectedUserId?: number,
  size?: number
}

let props = withDefaults(defineProps<Props>(), {
  size: 15
});

const emit = defineEmits<{
  (e: 'select', value: UserDto): void,
  (e: 'ready', value: UserDto[]): void
}>()

const selectorSizes = ref(Array.from(new Set([10, 15, 30, 40, 50, 60, 100, props.size])).sort((a, b) => a - b))
const selectorSize = ref(props.size);
const selectedUser = ref<UserDto>();

watch(selectedUser, (newValue) => {
  emit('select', newValue)
});

const loading = ref(true);
const users = ref<UserDto[]>();

const refresh = () => {
  loading.value = true;

  UserService.fetchAllUsersWithAuthority(props.authority, fetched => {
    users.value = fetched;
    if (props.defaultSelectedUserId) {
      users.value.forEach(t => {
        if (t.id == props.defaultSelectedUserId) {
          selectedUser.value = t;
        }
      });
    }
    loading.value = false;
    emit("ready", fetched);
  })

  users.value = undefined
  emit("select", undefined)
}

const select = (userId: number) => {
  let trySelectUser = users.value.filter(t => t.id == userId);
  if (trySelectUser.length != 1) {
    console.error('select user with id ' + userId + ' but filtered = ' + trySelectUser);
    return;
  }
  selectedUser.value = trySelectUser[0];
}


const resetSelected = () => {
  selectedUser.value = null;
  refresh();
}

defineExpose({
  select,
  resetSelected,
  refresh
});

onMounted(() => {
  refresh();
})

</script>

<template>
  <LoadingSpinner v-if="loading" text="Загрузка пользователей"/>
  <div class="container p-2 mb-3 bg-body rounded" v-else>
    <div class="row">
      <div class="col-12">
        <h4 class="m-0">{{props.tittle ?? 'Пользователи'}}</h4>
      </div>
    </div>
    <div class="row mt-2 mb-2">
      <div class="col-12">
        <select class="form-select" v-model="selectedUser" :size="selectorSize">
          <option v-for="user in users" :value="user" :selected="selectedUser?.id == user.id">{{ user.username }}</option>
        </select>
      </div>
    </div>
    <div class="row d-flex justify-content-around">
      <div class="input-group input-group-sm">
        <span class="input-group-text">Кол-во строк</span>
        <select v-model="selectorSize">
          <option v-for="size in selectorSizes" :value="size">{{ size }}</option>
        </select>
        <button class="btn btn-outline-danger bi bi-arrow-clockwise" type="button" @click="refresh"></button>
        <button class="btn btn-outline-danger" type="button" @click="resetSelected">Сброс</button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>