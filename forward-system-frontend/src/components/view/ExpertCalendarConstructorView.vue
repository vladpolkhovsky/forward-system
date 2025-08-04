<script setup lang="ts">

import {onMounted, ref, watch} from "vue";
import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {CalendarGroupDto} from "@/core/dto/CalendarGroupDto.ts";
import ExpertCalendarGridConstructor from "@/components/elements/ExpertCalendarGridConstructor.vue";
import ExpertCalendarView from "@/components/view/ExpertCalendarView.vue";

interface CalendarGroup {
  id: number
  name: String,
  selectedAuthorIds: Set<number>
}

const authorsLoading = ref(true)
const groupsLoading = ref(true)
const newGroupName = ref("")

const authors = ref<AuthorShortDto[]>(null)
const groups = ref<CalendarGroupDto[]>(null)

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
  const fetchedAuthors = await fetch("/api/author/get-authors", {method: 'GET'});
  authors.value = (await fetchedAuthors.json()) as AuthorShortDto[];
  authorsLoading.value = false

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
  authorsLoading.value = true;

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
  authorsLoading.value = true;

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
  authorsLoading.value = true;

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
  authorsLoading.value = true;

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
        <p class="fw-bold h4">Авторы</p>
        <LoadingSpinner v-if="authorsLoading"/>
        <select v-else class="form-select" multiple size="15">
          <option v-for="author in authors" :selected="selectedAuthor?.id == author.id"
                  @click="selectedAuthor = author">{{
              (selectedGroup?.selectedAuthorIds.has(author.id) ? '(В группе) ' : '') + author.username
            }}
          </option>
        </select>
      </div>
      <div class="col-12 col-md-4">
        <p class="fw-bold h4">Создать новую группу</p>
        <input class="form-control mb-2" placeholder="Название" v-model="newGroupName">
        <button :class="[ 'btn', 'btn-success', 'mb-2', { 'disabled': (newGroupName.length == 0) } ]"
                @click="saveNewGroup">
          Сохранить
        </button>
        <hr/>
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
      <div class="col-12 col-md-4">
        <div v-if="selectedGroup" class="row">
          <div v-if="selectedAuthor">
            <p class="text-muted fw-semibold h5">Выбран пользователь: <span class="text-dark fw-bold">{{ selectedAuthor.username }}</span></p>
            <div class="d-grid gap-2 d-md-block">
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
            <hr class="mt-2 mb-2"/>
          </div>
          <p class="text-muted fw-semibold h5">Состав группы: <span class="text-dark fw-bold">{{selectedGroup.name}}</span></p>
          <ol class="ms-3">
            <li v-for="authorInGroupId in selectedGroup.selectedAuthorIds">
              {{ getAuthorById(authorInGroupId).username }}
            </li>
          </ol>
          <div class="d-grid gap-2 d-md-block">
            <button class="btn btn-sm btn-danger mb-3" @click="handelDeleteGroup">Удалить группу</button>
          </div>
        </div>
        <div v-else>
          Группа не выбрана
        </div>
      </div>
    </div>
  </div>
  <ExpertCalendarGridConstructor :group-id="selectedGroup.id" v-if="selectedGroup" ref="calendarConstructorRef"/>
  <ExpertCalendarView />
</template>

<style scoped>

</style>