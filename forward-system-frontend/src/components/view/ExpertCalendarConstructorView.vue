<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {CalendarGroupDto} from "@/core/dto/CalendarGroupDto.ts";
import ExpertCalendarGridConstructor from "@/components/elements/ExpertCalendarGridConstructor.vue";
import AuthorSelector from "@/components/elements/AuthorSelector.vue";

interface CalendarGroup {
  id: number
  name: String,
  selectedAuthorIds: Set<number>
}

const groupsLoading = ref(true)
const newGroupName = ref("")

const groups = ref<CalendarGroupDto[]>()
const authors = ref<AuthorShortDto[]>()

const selectedGroup = ref<CalendarGroup>(null);
const selectedAuthor = ref<AuthorShortDto>(null);

const calendarConstructorRef = ref<InstanceType<typeof ExpertCalendarGridConstructor>>();
watch(selectedGroup, (value, oldValue) => {
  if (value !== oldValue) {
    calendarConstructorRef.value.reset();
  }
})

onMounted(async () => {
  init();
})

async function init() {
  const fetchedGroups = await fetch("/api/calendar/get-groups", {method: 'GET'});
  groups.value = (await fetchedGroups.json()) as CalendarGroupDto[];
  groupsLoading.value = false

  if (selectedGroup.value) {
    for (let group of groups.value) {
      if (group.id == selectedGroup.value.id)
        selectedGroup.value = calendarGroupDtoToCalendarGroup(group);
    }
  }
}

function getAuthorById(id: number) {
  for (let author of authors.value) {
    if (author.id == id) {
      return author
    }
  }
  return null;
}

function calendarGroupDtoToCalendarGroup(dto: CalendarGroupDto): CalendarGroup {
  return {
    name: dto.name,
    id: dto.id,
    selectedAuthorIds: new Set(dto.participants.map(value => value.id))
  }
}

async function saveNewGroup() {
  groupsLoading.value = true;

  const headers = new Headers();
  headers.append("Content-Type", "application/json; charset=UTF-8");

  await fetch("/api/calendar/create-group", {
    method: 'POST',
    headers: headers,
    body: JSON.stringify({
      name: newGroupName.value,
    })
  });

  newGroupName.value = "";

  init();
}

async function handelAddToGroup() {
  selectedGroup.value.selectedAuthorIds.add(selectedAuthor.value.id)

  groupsLoading.value = true;

  const headers = new Headers();
  headers.append("Content-Type", "application/json; charset=UTF-8");

  await fetch("/api/calendar/add-to-group", {
    method: 'POST',
    headers: headers,
    body: JSON.stringify({
      groupId: selectedGroup.value.id,
      userId: selectedAuthor.value.id
    }),
  })

  init();
}

async function handelDeleteFromGroup() {
  selectedGroup.value.selectedAuthorIds.add(selectedAuthor.value.id)

  groupsLoading.value = true;

  const headers = new Headers();
  headers.append("Content-Type", "application/json; charset=UTF-8");

  await fetch("/api/calendar/delete-from-group", {
    method: 'POST',
    headers: headers,
    body: JSON.stringify({
      groupId: selectedGroup.value.id,
      userId: selectedAuthor.value.id
    }),
  })

  init();
}

async function handelDeleteGroup() {
  groupsLoading.value = true;

  const headers = new Headers();
  headers.append("Content-Type", "application/json; charset=UTF-8");

  await fetch("/api/calendar/delete-group", {
    method: 'POST',
    headers: headers,
    body: JSON.stringify({
      id: selectedGroup.value.id,
    }),
  })

  selectedGroup.value = null;

  init();
}

</script>

<template>
  <div class="container mb-3">
    <div class="row">
      <h3>Конструктор Расписания</h3>
    </div>
  </div>
  <div class="container mb-3">
    <div class="row">
      <div class="col-12 col-md-4">
        <AuthorSelector @select="author => selectedAuthor = author" @ready="fetchedAuthors => authors = fetchedAuthors"/>
      </div>
      <div class="col-12 col-md-4">
        <div class="container shadow-sm p-3 mb-3 bg-body rounded">
          <div class="row">
            <div class="col-12">
              <p class="fw-bold h4">Создать новую группу</p>
              <input class="form-control mb-2" placeholder="Название" v-model="newGroupName">
              <button :class="[ 'btn', 'btn-success', 'mb-2', { 'disabled': (newGroupName.length == 0) } ]"
                      @click="saveNewGroup">
                Сохранить
              </button>
            </div>
          </div>
        </div>
        <div class="container shadow-sm p-3 mb-3 bg-body rounded">
          <div class="row">
            <div class="col-12">
              <p class="fw-bold h4">Группы</p>
              <LoadingSpinner v-if="groupsLoading"/>
              <select v-else class="form-select" multiple :size="Math.min(10, groups.length)">
                <option v-for="group in groups" :selected="selectedGroup?.id == group.id"
                        @click="selectedGroup = calendarGroupDtoToCalendarGroup(group)">{{
                    group.name
                  }}
                </option>
              </select>
            </div>
          </div>
        </div>
      </div>
      <div class="col-12 col-md-4">
        <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-if="selectedGroup">
          <div class="row">
            <template v-if="selectedAuthor">
              <p class="text-muted fw-semibold h5">Выбран пользователь: <span
                  class="text-dark fw-bold">{{ selectedAuthor.username }}</span></p>
              <div class="d-grid gap-2 d-md-block mb-3">
                <button class="btn btn-sm btn-primary mb-3"
                        v-if="!selectedGroup.selectedAuthorIds.has(selectedAuthor.id)"
                        @click="handelAddToGroup">
                  Добавить в группу
                </button>
                <button class="btn btn-sm btn-danger mb-3"
                        v-if="selectedGroup.selectedAuthorIds.has(selectedAuthor.id)"
                        @click="handelDeleteFromGroup">
                  Удалить из группы
                </button>
              </div>
            </template>
            <p class="text-muted fw-semibold h5">Состав группы: <span
                class="text-dark fw-bold">{{ selectedGroup.name }}</span></p>
            <ol class="ms-3">
              <li v-for="authorInGroupId in selectedGroup.selectedAuthorIds">
                {{ getAuthorById(authorInGroupId).username }}
              </li>
            </ol>
            <div class="d-grid gap-2 d-md-block">
              <button class="btn btn-sm btn-danger mb-3" @click="handelDeleteGroup">Удалить группу</button>
            </div>
          </div>
        </div>
        <div v-else class="container shadow-sm p-3 mb-3 bg-body rounded">
          <p class="fw-semibold h5 text-danger m-0">Группа не выбрана</p>
        </div>
      </div>
    </div>
  </div>
  <ExpertCalendarGridConstructor :group-id="selectedGroup.id" v-if="selectedGroup" ref="calendarConstructorRef"/>
</template>

<style scoped>

</style>