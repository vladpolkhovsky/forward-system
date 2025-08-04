<script setup lang="ts">

import {onMounted, ref} from "vue";
import type {CalendarGroupDto} from "@/core/dto/CalendarGroupDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import dayjs from "dayjs";
import updateLocale from "dayjs/plugin/updateLocale";
import isToday from "dayjs/plugin/isToday";
import type {CalendarGroupParticipantStatusDto} from "@/core/dto/CalendarGroupParticipantStatusDto.ts";

dayjs.extend(updateLocale)
dayjs.extend(isToday);

dayjs.updateLocale('en', {
  weekStart: 1
})

interface DateState {
  date: dayjs.Dayjs,
  activeUserIds: Set<number>
}

const loading = ref(true);
const begin = dayjs().subtract(2, "day");
const groups = ref<CalendarGroupDto[]>(null);
const selectedGroupId = ref<number>(null);
const selectedGroup = ref<CalendarGroupDto>(null);
const calendar = ref<DateState[]>([])

function resetCalendar() {
  calendar.value = [];

  for (let i = 0; i < 9; i++) {
    calendar.value.push({
      date: begin.clone().add(i, "day"),
      activeUserIds: new Set()
    });
  }

  console.log(calendar)
}

function fetchGroups() {
  resetCalendar();

  fetch("/api/calendar/get-groups", {method: "GET"})
      .then(value => value.json())
      .then(value => value as CalendarGroupDto[])
      .then(value => {
        groups.value = value;
        selectedGroupId.value = value[0].id;
        selectedGroup.value = value[0];
        loading.value = false;

        handleChangeSelectedGroup();
      })
}

function handleChangeSelectedGroup() {
  loading.value = true;

  for (let group of groups.value) {
    if (group.id == selectedGroupId.value) {
      selectedGroup.value = group;
    }
  }

  resetCalendar();

  let begin = dayjs().clone().subtract(1, "month").format("DD.MM.YYYY");
  let end = dayjs().clone().add(2, "month").format("DD.MM.YYYY");
  fetch(`/api/calendar/get-group-participant-status/${selectedGroupId.value}` +
      `?begin=${begin}&end=${end}`, {method: "GET"})
      .then(value => value.json())
      .then(value => value as CalendarGroupParticipantStatusDto)
      .then(value => {
        calendar.value.forEach(day => {
          let key = day.date.format("DD.MM.YYYY");
          value.days[key]?.forEach(authorId => {
            day.activeUserIds.add(authorId)
          });
        });

        loading.value = false;
      });
}

onMounted(() => {
  fetchGroups();
})

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div v-else class="container mt-2">
    <div class="card">
      <div class="card-header">
        Расписание
      </div>
      <div class="card-body">
        <div class="mb-2 row">
          <label for="select-calendar-group" class="col-md-2 col-form-label">Группа: </label>
          <div class="col-md-10">
            <select id="select-calendar-group" v-model="selectedGroupId" class="form-select"
                    @change="handleChangeSelectedGroup">
              <option :value="group.id" v-for="group in groups">{{ group.name }}</option>
            </select>
          </div>
        </div>
        <div class="row mt-2">
          <div class="col-12">
            <table class="table table-bordered">
              <thead>
              <tr>
                <th scope="col">Автор</th>
                <th scope="col" v-for="date in calendar"
                    :class="{ 'table-warning': (date.date.day() == 6 || date.date.day() == 0), 'table-success': date.date.isToday() }">
                  <p class="m-0 mb-1">{{ date.date.locale("ru-RU").format("D MMM") }}</p>
                  <p class="m-0 mb-1">{{ date.date.locale("ru-RU").format("ddd") }}</p>
                  <p class="m-0 mb-1 fw-bold" v-if="date.date.isToday()">Сегодня</p>
                </th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="member in selectedGroup.participants">
                <th scope="row">{{ member.username }}</th>
                <td v-for="day in calendar"
                    :class="['text-center', 'align-content-center', { 'bg-success-subtle' : day.activeUserIds.has(member.id)}]">
                  <input class="form-check-input" type="checkbox" disabled :checked="day.activeUserIds.has(member.id)">
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>