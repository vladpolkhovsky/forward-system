<script setup lang="ts">

import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";
import {computed, onMounted, ref, watch} from "vue";
import type {AuthorDto} from "@/core/dto/AuthorDto.ts";
import type {DistributionPerson} from "@/core/dto/DistributionPerson.ts";

interface Props {
  tittle: string,
  disabled: boolean,
  users: AuthorDto[],
  participants: OrderParticipantDto[],
  color: string
}

const props = defineProps<Props>();

interface UserSelectionStatus {
  userId: number,
  isDistributed: boolean,
  isLimitExceeded: boolean,
  isDisabled: boolean,
  isChecked: boolean,
  customFee?: number
}

const model = ref<UserSelectionStatus[]>([]);
const author = new Map<number, AuthorDto>([])

watch(() => props.users, (newValue) => {
  console.log(props.tittle, newValue);
  updateUserSelectionStatus();
});

watch(() => props.participants, (newValue) => {
  console.log(props.tittle, newValue)
  updateUserSelectionStatus();
});

onMounted(() => {
  console.log(props);
  updateUserSelectionStatus();
});

function isParticipant(userId: number): boolean {
  return props.participants?.filter(t => t.user.id == userId)?.length > 0;
}

function isLimitExceeded(user: AuthorDto): boolean {
  return user.activeOrders.length >= user.maxOrdersCount;
}

function updateUserSelectionStatus() {
  author.clear();

  props.users?.forEach(user => {
    author.set(user.id, user);
  });

  model.value = props.users?.map(user => {
    return {
      userId: user.id,
      isDistributed: isParticipant(user.id),
      isLimitExceeded: isLimitExceeded(user),
      isDisabled: isParticipant(user.id) || isLimitExceeded(user),
      isChecked: false
    }
  }) ?? [];
}

function getOrderItmes(userId: number) {
  return props.users?.filter(t => t.id == userId)?.[0]?.activeOrders ?? []
}

const clearSelectionAll = () => {
  updateUserSelectionStatus();
}

const clearSelectionById = (id: number) => {
  model.value?.filter(t => t.userId == id)?.forEach(t => {
    const user = author.get(t.userId);
    t.isChecked = false
    t.isDistributed = isParticipant(user.id);
    t.isLimitExceeded = isLimitExceeded(user);
    t.isDisabled = isParticipant(user.id) || isLimitExceeded(user);
  });
}

const distributionPersons = computed<DistributionPerson[]>(() => {
  return model.value
      ?.filter(t => t.isChecked)
      ?.map(t => {
        return {
          userId: t.userId,
          customFee: t.customFee,
          author: author.get(t.userId),
          order: -1
        }
      });
});

defineExpose({
  clearSelectionAll,
  clearSelectionById,
  distributionPersons
});

</script>

<template>
  <div :class="['card', 'bg-' + color, 'bg-gradient']" style="--bs-bg-opacity: .3;">
    <div class="card-body p-2">
      <h5 class="card-title">{{ props.tittle }}</h5>
      <template v-for="item in model">
        <div class="input-group input-group-sm mt-2">
          <div :class="['input-group-text', 'bg-' + color]" style="--bs-bg-opacity: .3;">
            <input class="form-check-input mt-0 col-2" type="checkbox"
                   v-model="item.isChecked"
                   :disabled="item.isDisabled || disabled">
          </div>
          <span
              :class="['input-group-text col-5', 'bg-' + color, { 'text-decoration-line-through': item.isDisabled }]"
              style="--bs-bg-opacity: .3;" :id="item.userId.toString()">{{ author.get(item.userId)?.username }}</span>
          <input type="number" :class="['input-group-text form-control col-5 text-start', 'bg-' + color]"
                 style="--bs-bg-opacity: .3;"
                 v-model="item.customFee"
                 placeholder="Стоимость для автора"
                 :disabled="item.isDisabled || disabled">
        </div>
        <div class="form-text text-danger fw-bold mb-2" v-if="item.isDistributed">
          Зпрос уже отправлен автору.
        </div>
        <div class="form-text text-danger fw-bold mb-2 d-flex flex-column gap-2" v-if="item.isLimitExceeded">
          <div class="d-flex gap-2">
            <a class="fs-4 ms-2 bi bi-info-circle text-danger"></a>
            <p class="m-1 mb-2">Превышено максимальное число одновременных заказов</p>
          </div>
          <div class="d-flex gap-2">
            <a v-for="order in getOrderItmes(item.userId)" class="d-inline-block me-2" target="_blank" :href="'/view-order/' + order.orderId">
              <i class="bi bi-journal-text"></i> {{ '№ ' + order.orderTechNumber + ' | ' + order.orderStatusRus }}
            </a>
          </div>
          <span >

          </span>
        </div>
      </template>
    </div>
  </div>
</template>

<style scoped>

</style>