<script setup lang="ts">
import 'dayjs/locale/ru'
import dayjs from 'dayjs'
import updateLocale from 'dayjs/plugin/updateLocale'
import {onMounted, ref, watch} from "vue";
import type {CalendarGroupDto} from "@/core/dto/CalendarGroupDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {CalendarGroupParticipantStatusDto} from "@/core/dto/CalendarGroupParticipantStatusDto.ts";
import type {CalendarGroupParticipantStatusSwitchDto} from "@/core/dto/CalendarGroupParticipantStatusSwitchDto.ts";

dayjs.extend(updateLocale)

dayjs.updateLocale('en', {
  weekStart: 1
})

interface Props {
  groupId: number
}

const props = defineProps<Props>()

const reset = () => {
  init(props.groupId);
}

defineExpose({
  reset
})

interface DateState {
  date: dayjs.Dayjs,
  activeUserIds: Set<number>
  loading: boolean
}

const begin = dayjs().startOf("week");
const dateGrid = ref<DateState[][]>([]);

const group = ref<CalendarGroupDto>(null);
const loading = ref(true);

onMounted(() => {
  init(props.groupId);
})

watch(props, (newValue) => {
  console.log("watch", newValue, props)
  init(newValue.groupId);
})

function init(groupId: number) {
  fetchGroup(groupId);
}

function fetchGroup(groupId: number) {
  loading.value = true;

  fetch(`/api/calendar/get-group/${groupId}`, {method: "GET"})
      .then(value => value.json())
      .then(value => value as CalendarGroupDto)
      .then(value => {
        if (props.groupId == value.id) {
          group.value = value;
          createCalendarTables();
          fetchParticipantStatus();
          loading.value = false;
        }
      });
}

function createCalendarTables() {
  dateGrid.value = [];

  for (let i = 0; i < 6; i++) {
    const line: DateState[] = [];

    // Создаём новую дату на основе begin + i недель БЕЗ мутации
    const weekStart = begin.clone().add(i, "week");

    for (let j = 0; j < 7; j++) {
      // Для каждого дня клонируем начало недели и добавляем дни
      line.push({
        date: weekStart.clone().add(j, "day"),
        activeUserIds: new Set<number>(),
        loading: true
      });
    }

    dateGrid.value.push(line);
  }
}

function fetchParticipantStatus() {
  let begin = dayjs().subtract(1, "month").format("DD.MM.YYYY");
  let end = dayjs().add(2, "month").format("DD.MM.YYYY");
  fetch(`/api/calendar/get-group-participant-status/${props.groupId}` +
      `?begin=${begin}&end=${end}`, {method: "GET"})
      .then(value => value.json())
      .then(value => value as CalendarGroupParticipantStatusDto)
      .then(value => {
        dateGrid.value.forEach(line => {
          line.forEach(day => {
            let key = day.date.format("DD.MM.YYYY");
            value.days[key]?.forEach(authorId => {
              day.activeUserIds.add(authorId)
            });
            day.loading = false;
          });
        });
      });
}

function switchParticipantStatus(day: DateState, userId: number) {
  let date = day.date.format("DD.MM.YYYY");
  fetch(`/api/calendar/get-group-participant-status/switch/${props.groupId}/${userId}?date=${date}`, {method: "POST"})
      .then(value => value.json())
      .then(value => value as CalendarGroupParticipantStatusSwitchDto)
      .then(value => {
        if (value.working) {
          day.activeUserIds.add(value.userId);
        } else {
          day.activeUserIds.delete(value.userId);
        }
        day.loading = false;
      });
}

function switchState(day: DateState, userId: number) {
  day.loading = true;
  switchParticipantStatus(day, userId)
}
</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-else>
    <p class="fw-bold fs-5">Расписание группы {{ group.name }}</p>
    <div class="row">
      <div class="col-12 col-xl-6 mb-3" v-for="(weekLine, index) in dateGrid" :key="index">
        <!-- Форматируем вывод для читаемости -->
        <p class="fs-5 mb-3">Неделя {{ weekLine[0].date.locale("ru-RU").format("D MMM") }} - {{
            weekLine[weekLine.length - 1].date.locale("ru-RU").format("D MMM YYYY")
          }}</p>
        <table class="table table-bordered">
          <thead>
          <tr>
            <th scope="col">Автор</th>
            <th scope="col" v-for="date in weekLine"
                :class="{ 'table-warning': (date.date.day() == 6 || date.date.day() == 0) }">
              <p class="m-0 mb-1">{{ date.date.locale("ru-RU").format("D MMM") }}</p>
              <p class="m-0 mb-1">{{ date.date.locale("ru-RU").format("ddd") }}</p>
            </th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="member in group.participants">
            <th scope="row">{{ member.username }}</th>
            <td v-for="day in weekLine"
                :class="['text-center', 'align-content-center', { 'bg-success-subtle' : day.activeUserIds.has(member.id)}]">
              <LoadingSpinner v-if="day.loading"/>
              <input v-else class="form-check-input" type="checkbox" :checked="day.activeUserIds.has(member.id)"
                     @change="switchState(day, member.id)">
            </td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>