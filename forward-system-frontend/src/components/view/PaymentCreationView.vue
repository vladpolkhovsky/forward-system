<script setup lang="ts">

import AuthorSelector from "@/components/elements/AuthorSelector.vue";
import OrderSelector from "@/components/elements/OrderSelector.vue";
import {ref} from "vue";
import type {AuthorShortDto} from "@/core/dto/AuthorShortDto.ts";
import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";
import type {OrderPaymentStatusType} from "@/core/type/OrderPaymentStatusType.ts";
import {OrderPaymentStatusEnum, paymentStatusToRusName} from "@/core/enum/OrderPaymentStatusEnum.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {PaymentStatusService} from "@/core/PaymentStatusService.ts";
import type {OrderPaymentSaveRequestDto} from "@/core/dto/OrderPaymentDto.ts";
import PaymentView from "@/components/view/PaymentView.vue";

const saveProgress = ref(false);
const selectedAuthor = ref<AuthorShortDto>();
const selectedAuthorOrder = ref<AuthorOrdersDto>();
const paymentViewRef = ref<InstanceType<typeof PaymentView>>();

let keyValue = 0;

interface CreatePaymentEditableRequest {
  key: number,
  author: AuthorShortDto,
  order: AuthorOrdersDto,
  paymentStatus: OrderPaymentStatusType,
  value: number | null
}

const edit = ref<CreatePaymentEditableRequest[]>([]);

function handelCreatePaymentEdit() {
  edit.value.push({
    key: keyValue++,
    order: selectedAuthorOrder.value,
    author: selectedAuthor.value,
    paymentStatus: OrderPaymentStatusEnum.NO_PAYMENT,
    value: null
  });
}

function changeSelectStatus(item: CreatePaymentEditableRequest) {
  if (item.paymentStatus == OrderPaymentStatusEnum.NO_PAYMENT) {
    item.value = null;
  }
}

function handelDelete(item: CreatePaymentEditableRequest) {
  edit.value = edit.value.filter(value => value.key != item.key);
}

function handelSave() {
  saveProgress.value = true;
  PaymentStatusService.saveMany(
      edit.value.map<OrderPaymentSaveRequestDto>(value => {
        return {
          orderId: value.order.orderId,
          userId: value.author.id,
          status: value.paymentStatus,
          paymentValue: value.value
        };
      }),
      () => {
        edit.value = [];
        paymentViewRef.value.refresh();
        saveProgress.value = false;
      });
}

</script>

<template>
  <div class="container mb-3">
    <div class="row">
      <h3>Статусы оплаты</h3>
    </div>
  </div>
  <div class="container mb-2">
    <div class="row">
      <div class="col-12 col-md-3">
        <AuthorSelector @select="value => selectedAuthor = value"/>
      </div>
      <div class="col-12 col-md-3">
        <OrderSelector @select="value => selectedAuthorOrder = value" :author-id="selectedAuthor?.id"/>
      </div>
      <div class="col-12 col-md-6">
        <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-if="selectedAuthor && selectedAuthorOrder">
          <p class="m-0 mb-3">Нажмите ниже, чтобы создать выплату.</p>
          <button class="btn btn-sm btn-primary" @click="handelCreatePaymentEdit">Создать выплату
            {{ selectedAuthor.username }} по заказу {{ selectedAuthorOrder.orderTechNumber }}
          </button>
        </div>
        <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-if="edit.length > 0">
          <form @submit.prevent="handelSave">
            <div class="row">
              <table class="table table-bordered">
                <thead>
                <tr>
                  <th scope="col">ТЗ</th>
                  <th scope="col">Автор</th>
                  <th scope="col">Статус</th>
                  <th scope="col">Размер выплаты</th>
                  <th scope="col">Действия</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in edit">
                  <th scope="row" class="text-center align-content-center">{{ item.order.orderTechNumber }}</th>
                  <td class="text-center align-content-center">{{ item.author.username }}</td>
                  <td class="text-center align-content-center">
                    <select class="form-select form-select-sm" required v-model="item.paymentStatus"
                            @change="changeSelectStatus(item)">
                      <option :value="OrderPaymentStatusEnum.NO_PAYMENT"
                              :selected="item.paymentStatus == OrderPaymentStatusEnum.NO_PAYMENT">
                        {{ paymentStatusToRusName(OrderPaymentStatusEnum.NO_PAYMENT) }}
                      </option>
                      <option :value="OrderPaymentStatusEnum.PARTITIONAL_PAYMENT"
                              :selected="item.paymentStatus == OrderPaymentStatusEnum.PARTITIONAL_PAYMENT">
                        {{ paymentStatusToRusName(OrderPaymentStatusEnum.PARTITIONAL_PAYMENT) }}
                      </option>
                      <option :value="OrderPaymentStatusEnum.FULL_PAYMENT"
                              :selected="item.paymentStatus == OrderPaymentStatusEnum.FULL_PAYMENT">
                        {{ paymentStatusToRusName(OrderPaymentStatusEnum.FULL_PAYMENT) }}
                      </option>
                    </select>
                  </td>
                  <td class="text-center align-content-center">
                    <input class="form-control form-control-sm"
                           v-model="item.value"
                           required
                           placeholder="Нет выплаты"
                           type="number"
                           :disabled="item.paymentStatus == OrderPaymentStatusEnum.NO_PAYMENT">
                  </td>
                  <td class="text-center align-content-center">
                    <button class="btn btn-sm btn-danger bi bi-trash" @click="handelDelete(item)"></button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
            <div class="row mt-3">
              <button class="btn btn-sm btn-success" type="submit" :disabled="saveProgress">
                Сохранить
              </button>
              <LoadingSpinner v-if="saveProgress" class="mt-3"/>
              <span class="text-center mt-3" v-if="saveProgress">Не закрывайте страницу до конца загрузки!</span>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
  <PaymentView :author-id="selectedAuthor.id" v-if="selectedAuthor" :two-on-row="true" ref="paymentViewRef" class="mt-3"/>
</template>

<style scoped>

</style>