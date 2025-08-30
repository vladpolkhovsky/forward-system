<script setup lang="ts">
import {QueryParamService} from "@/core/QueryParamService.ts";
import {computed, onMounted, ref, useTemplateRef, watch} from "vue";
import type {OrderShortDto} from "@/core/dto/OrderShortDto.ts";
import {OrderService} from "@/core/OrderService.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";
import UserSelector from "@/components/elements/UserSelector.vue";
import {AuthorityEnum} from "@/core/enum/AuthorityEnum.ts";
import type {UserDto} from "@/core/dto/UserDto.ts";
import {AuthorService} from "@/core/AuthorService.ts";
import type {AuthorDto} from "@/core/dto/AuthorDto.ts";
import type {DisciplineDto} from "@/core/dto/DisciplineDto.ts";
import DistributionBlock from "@/components/view/DistributionBlock.vue";
import Accordion from "@/components/elements/Accordion.vue";
import AccordionItem from "@/components/elements/AccordionItem.vue";
import {ParticipantTypeEnum} from "@/core/enum/ParticipantTypeEnum.ts";
import type {OrderParticipantDto} from "@/core/dto/OrderParticipantDto.ts";
import QueueDistributionView from "@/components/elements/QueueDistrubutionView.vue";
import type QueueDistrubutionEdit from "@/components/elements/QueueDistrubutionEdit.vue";
import QueueDistributionEdit from "@/components/elements/QueueDistrubutionEdit.vue";
import type {DistributionPerson} from "@/core/dto/DistributionPerson.ts";
import {DistributionService} from "@/core/DistributionService.ts";
import type {DistributionRequest} from "@/core/dto/DistributionRequest.ts";
import type {DistributionLogDto} from "@/core/dto/DistributionLogDto.ts";

const orderId = parseInt(QueryParamService.getParam("orderId"));

const loading = ref(true);
const loadingAuthors = ref(true);
const loadingAutomaticDistribution = ref(true);
const useQueueDistribution = ref(false);
const distributionText = ref("");

const distributionExcellent = ref<InstanceType<typeof DistributionBlock>>();
const distributionGood = ref<InstanceType<typeof DistributionBlock>>();
const distributionMaybe = ref<InstanceType<typeof DistributionBlock>>();
const distributionNotWrite = ref<InstanceType<typeof DistributionBlock>>();

const distributionGroupsRef = computed<InstanceType<typeof DistributionBlock>[]>(() => {
  return [
    distributionExcellent.value, distributionGood.value, distributionMaybe.value, distributionNotWrite.value,
  ]
});

const queueDistributionEditRef = useTemplateRef<InstanceType<typeof QueueDistrubutionEdit>>("queueDistributionEditRef");

const distributionPersons = computed<DistributionPerson[]>(() => {
  let order = 0;
  return distributionGroupsRef?.value?.map(t => t)
      ?.flatMap(t => t?.distributionPersons ?? [])?.filter(t => t)
      ?.map(t => {
        console.log(t);
        t.order = order++;
        return t;
      }) ?? [];
});

watch(distributionPersons, (newVal) => {
  console.log(newVal);
})

const selectedCatcher = ref<UserDto>();
const distributionLogs = ref<DistributionLogDto[]>();
const authors = ref<AuthorDto[]>();

const excellent = computed(() => getExcellent(authors.value, order.value.disciplineId));
const good = computed(() => getGood(authors.value, order.value.disciplineId));
const maybe = computed(() => getMaybe(authors.value, order.value.disciplineId));
const notWrite = computed(() => getNotWrite(authors.value, order.value.disciplineId));

const order = ref<OrderShortDto>();

onMounted(() => {
  refreshOrder();
  refreshAuthors();
  refreshDistributionLogs();
});

function refreshDistributionLogs() {
  loadingAutomaticDistribution.value = true;
  DistributionService.getDistributionLogs(orderId, logs => {
    distributionLogs.value = logs;
    loadingAutomaticDistribution.value = false;
  });
}

function refreshOrder() {
  loading.value = true;
  OrderService.fetchOrder(orderId, orderDto => {
    order.value = orderDto;
    loading.value = false;
  });
}

function refreshAuthors() {
  loadingAuthors.value = true;
  AuthorService.fetchAuthors(authorsDto => {
    authors.value = authorsDto;
    loadingAuthors.value = false;
  });
}

function handleCatcher(catcher: UserDto) {
  selectedCatcher.value = catcher;
}

function hasDiscipline(sourceDisciplines: DisciplineDto[], targetDisciplineId: number): boolean {
  return sourceDisciplines.filter(t => t.id == targetDisciplineId).length > 0;
}

function getExcellent(authors: AuthorDto[], targetDisciplineId: number): AuthorDto[] {
  return authors.filter(t => hasDiscipline(t.disciplines.excellent, targetDisciplineId))
}

function getGood(authors: AuthorDto[], targetDisciplineId: number): AuthorDto[] {
  return authors.filter(t => hasDiscipline(t.disciplines.good, targetDisciplineId))
}

function getMaybe(authors: AuthorDto[], targetDisciplineId: number): AuthorDto[] {
  return authors.filter(t => hasDiscipline(t.disciplines.maybe, targetDisciplineId))
}

function getNotWrite(authors: AuthorDto[], targetDisciplineId: number): AuthorDto[] {
  return authors.filter(t => !(hasDiscipline(t.disciplines.excellent, targetDisciplineId)
      || hasDiscipline(t.disciplines.good, targetDisciplineId)
      || hasDiscipline(t.disciplines.maybe, targetDisciplineId)));
}

function getCatcher(): OrderParticipantDto {
  let catchers = order.value.participants.filter(t => t.type == ParticipantTypeEnum.CATCHER);
  if (catchers.length > 0) {
    return catchers[0];
  }
  return undefined;
}

function handleUncheckAuthor(userId: number) {
  distributionGroupsRef.value.forEach(ref => {
    ref.clearSelectionById(userId);
  });
}

function handleSetNewCatcher() {
  const catcherId = selectedCatcher.value.id;
  const orderId = order.value.id;

  DistributionService.sendSetNewCatcher(orderId, catcherId, () => {
    refreshOrder();
  });
}

function handleSendDistribution() {
  let persons: DistributionPerson[] = [];

  if (useQueueDistribution.value) {
    persons.push(...queueDistributionEditRef.value.sorted)
  } else {
    persons.push(...distributionPersons.value)
  }

  const request = {
    text: distributionText.value,
    isQueueDistribution: useQueueDistribution.value,
    persons: persons,
    queueDistributionWaitMinutes: queueDistributionEditRef.value?.waitMinutes ?? null
  } as DistributionRequest;

  DistributionService.sendDistributionRequest(order.value.id, request, () => {
    refreshOrder();
    refreshDistributionLogs();
  });

  distributionGroupsRef.value?.forEach(el => {
    persons.forEach(p => {
      el.clearSelectionById(p.userId);
    });
  });

  distributionText.value = "";
}

function handleRemoveAuthorFromParticipants(orderId: number, authorId: number) {
  DistributionService.removeAuthorFromOrderParticipants(orderId, authorId, () => {
    for (const el of distributionGroupsRef.value) {
      el.clearSelectionById(authorId);
    }
    order.value.participants = order.value.participants.filter(t => t.user.id != authorId);
  });
}

const isStatusNotCreatedAndDistribution = computed(() => {
  return order.value.orderStatus != OrderStatusEnum.CREATED &&
      order.value.orderStatus != OrderStatusEnum.DISTRIBUTION
});
</script>


<template>
  <LoadingSpinner v-if="loading" text="Загрузка информации о заказе для распределения."/>
  <div class="container-fluid shadow-sm p-3 mb-3 bg-body rounded" v-else>
    <div class="row" v-if="isStatusNotCreatedAndDistribution">
      <div class="alert alert-danger text-center" role="alert">
        Статус заказа <strong>{{ order.orderStatusRus }}</strong>. Невозможно предложить заказ другим
        авторам.
      </div>
    </div>
    <div class="row mt-3">
      <h5 class="m-0 p-0 text-center h4">Распределение для заказа №{{ order.techNumber }} (Статус:
        {{ order.orderStatusRus }})</h5>
    </div>
    <div class="row mt-3 mt-md-0">
      <div class="col-12 mt-3 col-md-6 col-lg-2">
        <UserSelector :authority="AuthorityEnum.MANAGER"
                      @select="handleCatcher"
                      tittle="Выбор кетчера"
                      :default-selected-user-id="getCatcher()?.user?.id"/>
        <div class="input-group mt-3" v-if="selectedCatcher && getCatcher()?.user?.id != selectedCatcher?.id">
          <span class="input-group-text">{{ selectedCatcher.username }}</span>
          <button class="btn btn-outline-primary"
                  @click="handleSetNewCatcher"
                  type="button"
                  :disabled="isStatusNotCreatedAndDistribution">Назначить кетчером
          </button>
        </div>
      </div>
      <div class="col-12 mt-3 col-md-6 col-lg-4">
        <LoadingSpinner v-if="loadingAuthors" text="Загрузка авторов"/>
        <div v-else class="card p-2">
          <Accordion :name="'Группы авторов по наличию дисциплины: ' + order.disciplineName">
            <AccordionItem :name="'Напишет на 5 (' + excellent.length + ' авторов)'" :open="true">
              <DistributionBlock tittle="Список авторов" :users="excellent"
                                 :disabled="isStatusNotCreatedAndDistribution"
                                 ref="distributionExcellent"
                                 :participants="order.participants" color="success"/>
            </AccordionItem>
            <AccordionItem :name="'Напишет на 4-5 (' + good.length + ' авторов)'" :open="false">
              <DistributionBlock tittle="Список авторов" :users="good"
                                 :disabled="isStatusNotCreatedAndDistribution"
                                 ref="distributionGood"
                                 :participants="order.participants" color="warning"/>
            </AccordionItem>
            <AccordionItem :name="'Может рассмотреть (' + maybe.length + ' авторов)'" :open="false">
              <DistributionBlock tittle="Список авторов" :users="maybe"
                                 :disabled="isStatusNotCreatedAndDistribution"
                                 ref="distributionMaybe"
                                 :participants="order.participants" color="danger"/>
            </AccordionItem>
            <AccordionItem :name="'Не имеют привязки (' + notWrite.length + ' авторов)'" :open="false">
              <DistributionBlock tittle="Список авторов" :users="notWrite"
                                 :disabled="isStatusNotCreatedAndDistribution"
                                 ref="distributionNotWrite"
                                 :participants="order.participants" color="secondary"/>
            </AccordionItem>
          </Accordion>
        </div>
      </div>
      <div class="col-12 mt-3 col-md-12 col-lg-6">
        <div class="row">
          <div class="col-12 mt-3 mt-md-0 col-md-6">
            <LoadingSpinner v-if="loading" text="Загрузка текущего сотава группы"/>
            <div class="card">
              <div class="card-body p-2">
                <h6 class="card-title">Пользовательский состав заказа.</h6>
                <ul class="list-group list-group-flush">
                  <li class="list-group-item d-flex justify-content-between align-items-start"
                      v-for="participant in order.participants" :key="participant.id">
                    <div class="ms-2 me-auto">
                      <div class="fw-bold">{{ participant.user.username }}</div>
                      <p class="m-0 mt-1" v-if="participant.fee">Цена для автора: {{ participant.fee }}</p>
                      <p class="m-0 mt-1">{{ participant.typeRusName }}</p>
                      <button class="m-0 mt-1 btn btn-sm btn-danger p-1"
                              :disabled="isStatusNotCreatedAndDistribution"
                              v-if="participant.type == ParticipantTypeEnum.AUTHOR || participant.type == ParticipantTypeEnum.DECLINE_AUTHOR"
                              @click="handleRemoveAuthorFromParticipants(order.id, participant.user.id)">
                        <i class="bi bi-person-x me-1"></i> Удалить автора из заказ
                      </button>
                    </div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          <div class="col-12 mt-3 mt-md-0 col-md-6">
            <div class="card">
              <div class="card-body p-2">
                <h6 class="card-title">Текущие выделенные пользователи</h6>
                <ul class="list-group list-group-flush">
                  <li class="list-group-item"
                      v-for="person in distributionPersons">
                    <div class="fw-bold"><i class="me-3 bi bi-person-dash btn btn-sm btn-outline-danger"
                                            @click="handleUncheckAuthor(person.userId)"></i>
                      {{ person.author.username }}
                    </div>
                    <div v-if="person.customFee">Цена для автора: {{ person.customFee }}</div>
                  </li>
                </ul>
              </div>
            </div>
          </div>
          <form name="submit distribution" @submit.prevent="handleSendDistribution">
            <div class="col-12 mt-3">
              <LoadingSpinner v-if="loadingAutomaticDistribution"
                              text="Загрузка информации об автоматическом распределении"/>
              <template v-else>
                <QueueDistributionView :log-items="distributionLogs"
                                       :disabled="isStatusNotCreatedAndDistribution"
                                       v-if="distributionLogs.length > 0"
                                       @update="refreshDistributionLogs"/>
                <div class="form-check form-switch mt-3" v-if="!isStatusNotCreatedAndDistribution">
                  <input class="form-check-input" type="checkbox" role="switch" v-model="useQueueDistribution">
                  <label class="form-check-label" for="switchCheckChecked">Использовать автоматическое
                    распределение</label>
                </div>
                <template v-if="useQueueDistribution">
                  <QueueDistributionEdit ref="queueDistributionEditRef" :persons="distributionPersons"/>
                </template>
              </template>
            </div>
            <div class="col-12 mt-3" v-if="!isStatusNotCreatedAndDistribution">
              <div class="mt-3">
                <label class="form-label">Текст, который будет отправлен авторам</label>
                <textarea class="form-control" rows="3" v-model="distributionText"
                          placeholder="Введите текст, который будет отправлен авторам. Можно оставить пустым"></textarea>
              </div>
              <button class="btn btn-sm w-100 m-auto btn-primary mt-2" type="submit">Отправить</button>
              <div class="form-text text-danger fw-bold mt-1" v-if="useQueueDistribution">
                *Будет использовано автоматическое распределение.
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>