<script setup lang="ts">
import Pill from "@/components/elements/Pill.vue";
import {OrderStatusEnum} from "@/core/enum/OrderStatusEnum.ts";

const props = defineProps({
  name: String,
  rusName: String
})
</script>

<template>
  <Pill color="secondary" :text="props.rusName" v-if="props.name == OrderStatusEnum.CREATED"/>
  <Pill color="secondary" :text="props.rusName" v-if="props.name == OrderStatusEnum.WAITING"/>
  <Pill color="secondary" :text="props.rusName" v-if="props.name == OrderStatusEnum.DISTRIBUTION"/>
  <Pill color="secondary" :text="props.rusName" v-if="props.name == OrderStatusEnum.ADMIN_REVIEW"/>
  <Pill color="primary" :text="props.rusName" v-if="props.name == OrderStatusEnum.IN_PROGRESS"/>
  <Pill color="primary" :text="props.rusName" v-if="props.name == OrderStatusEnum.REVIEW"/>
  <Pill color="danger" :text="props.rusName" v-if="props.name == OrderStatusEnum.FINALIZATION"/>
  <Pill color="success" :text="props.rusName" v-if="props.name == OrderStatusEnum.GUARANTEE"/>
  <Pill color="info" :text="props.rusName" v-if="props.name == OrderStatusEnum.CLOSED"/>
</template>

<style scoped>

</style>