<script setup lang="ts">
import type {AuthorOrdersDto} from "@/core/dto/AuthorOrdersDto.ts";
import {computed, onMounted, ref, watch} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {AuthorOrderService} from "@/core/AuthorOrderService.ts";

interface Props {
  authorId?: number
  size?: number
}

let props = withDefaults(defineProps<Props>(), {
  size: 15
});

const emit = defineEmits<{
  (e: 'select', value: AuthorOrdersDto): void,
  (e: 'ready', value: AuthorOrdersDto[]): void
}>()

const selectorSizes = ref(Array.from(new Set([10, 15, 30, 40, 50, 60, 100, props.size])).sort((a, b) => a - b))
const selectorSize = ref(props.size);
const selectedOrder = ref<AuthorOrdersDto>();
const propsAuthorId = computed(() => props.authorId);

watch(selectedOrder, (newValue) => {
  emit('select', newValue)
});

watch(propsAuthorId, () => {
  refresh();
});

const loading = ref(true);
const orders = ref<AuthorOrdersDto[]>();

const refresh = () => {
  loading.value = true;

  if (propsAuthorId.value) {
    AuthorOrderService.fetchAuthorOrdersById(propsAuthorId.value, fetched => {
      orders.value = fetched;
      loading.value = false;
      emit("ready", fetched);
    })

    selectedOrder.value = undefined
    emit("select", undefined)

    return;
  }

  AuthorOrderService.fetchAuthorOrders(fetched => {
    orders.value = fetched;
    loading.value = false;
    emit("ready", fetched);
  })

  selectedOrder.value = undefined
  emit("select", undefined)
}


defineExpose({
  refresh
});

onMounted(() => {
  refresh();
})

</script>

<template>
  <LoadingSpinner v-if="loading"/>
  <div class="container shadow-sm p-3 mb-3 bg-body rounded" v-else>
    <div class="row">
      <div class="col-12">
        <h4 class="m-0">Заказы</h4>
      </div>
    </div>
    <div class="row mt-2 mb-2">
      <div class="col-12" v-if="orders?.length > 0">
        <select class="form-select" v-model="selectedOrder" :size="selectorSize">
          <option v-for="order in orders" :value="order">{{ order.orderTechNumber }}</option>
        </select>
      </div>
      <div v-else>
        <p class="mt-0 mb-0">Нет заказов.</p>
      </div>
    </div>
    <div class="row">
      <div class="input-group input-group-sm">
        <span class="input-group-text">Кол-во строк</span>
        <select v-model="selectorSize">
          <option v-for="size in selectorSizes" :value="size">{{ size }}</option>
        </select>
        <button class="btn btn-outline-danger bi bi-arrow-clockwise" type="button" @click="refresh"></button>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>