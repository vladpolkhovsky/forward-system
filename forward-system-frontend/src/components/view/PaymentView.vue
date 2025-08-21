<script setup lang="ts">

import {PaymentStatusService} from "@/core/PaymentStatusService.ts";
import {computed, onMounted, ref, watch} from "vue";
import type {OrderPaymentDto} from "@/core/dto/OrderPaymentDto.ts";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";
import {OrderPaymentStatusEnum, paymentStatusToRusName} from "@/core/enum/OrderPaymentStatusEnum.ts";

interface Props {
  authorId?: number | undefined,
  twoOnRow?: boolean | undefined
}

const props = withDefaults(defineProps<Props>(), {
  authorId: undefined,
  twoOnRow: false
})

const payments = ref<OrderPaymentDto[]>([])
const loading = ref(true);
const propsAuthorId = computed(() => props.authorId)

interface GridLine {
  order: AuthorOrdersDto,
  items: OrderPaymentDto[]
}

const grid = ref<GridLine[]>();

watch(payments, (newValue) => {
  grid.value = [];
  newValue.forEach(item => {
    let found = false;
    grid.value.forEach(line => {
      if (line.order.orderId == item.order.orderId) {
        line.items.push(item);
        found = true;
      }
    });
    if (!found) {
      grid.value.push({
        order: item.order,
        items: [item]
      });
    }
  });
  console.log(grid.value);
});

const refresh = () => {
  loading.value = true;
  if (props.authorId) {
    PaymentStatusService.fetchPaymentStatusByUserId(props.authorId, fetched => {
      payments.value = fetched
      loading.value = false;
    });
    return;
  }
  PaymentStatusService.fetchPaymentStatus(fetched => {
    payments.value = fetched
    loading.value = false;
  });
}

defineExpose({
  refresh
})

onMounted(() => {
  refresh();
})

watch(propsAuthorId, () => {
  refresh();
})

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-else>
    <div class="row">
      <h5 class="m-0 p-0 ps-3">Статусы оплаты заказов</h5>
    </div>
    <div class="row mt-1 gy-2 gx-2" v-if="grid?.length > 0">
      <div :class="['col-12', { 'col-xl-6': props.twoOnRow }]" v-for="line in grid">
        <div class="card">
          <h5 class="card-header">Заказ: {{ line.order.orderTechNumber }}</h5>
          <div class="card-body m-0 p-0">
            <table class="table table-bordered m-0 p-0">
              <thead>
              <tr>
                <th scope="col" class="text-center align-content-center">Дата</th>
                <th scope="col" class="text-center align-content-center">Статус</th>
                <th scope="col" class="text-center align-content-center">Размер выплаты</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="item in line.items">
                <td class="text-center align-content-center">{{ item.createdAt }}</td>
                <td class="text-center align-content-center">
                <span :class="['badge', {
                    'text-bg-secondary': (item.status == OrderPaymentStatusEnum.NO_PAYMENT),
                    'text-bg-danger': (item.status == OrderPaymentStatusEnum.REFUND),
                    'text-bg-warning': (item.status == OrderPaymentStatusEnum.PARTITIONAL_PAYMENT),
                    'text-bg-success': (item.status == OrderPaymentStatusEnum.FULL_PAYMENT)
                  }]">{{ paymentStatusToRusName(item.status) }}</span>
                </td>
                <td class="text-center align-content-center" v-if="item.paymentValue">{{ item.paymentValue }}</td>
                <td class="text-center align-content-center" v-else><i>Нет оплаты</i></td>
              </tr>
              <tr>
                <td class="text-end align-content-center" colspan="2">Сумма:</td>
                <td class="text-center align-content-center text-bg-info">
                  {{
                    line.items.map(value => value.paymentValue ?? 0).reduce(((previousValue, currentValue) => previousValue + currentValue))
                  }}
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    <div class="row mt-1" v-else>
      <p class="m-0 ms-1 mt-2">Нет данных о статусах оплаты.</p>
    </div>
  </div>
</template>

<style scoped>

</style>