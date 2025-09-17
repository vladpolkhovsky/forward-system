<script setup lang="ts">

import {onMounted, ref} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {PlanViewService} from "@/core/PlanViewService.ts";
import type {UserPlanViewDto} from "@/core/dto/UserPlanViewDto.ts";
import Accordion from "@/components/elements/Accordion.vue";
import AccordionItem from "@/components/elements/AccordionItem.vue";
import Plan from "@/components/elements/Plan.vue";

const loading = ref(true);
const groupedByUserPlans = ref<UserPlans[]>();

interface UserPlans {
  userId: number,
  username: string,
  activePlan: UserPlanViewDto,
  plans: UserPlanViewDto[]
}

onMounted(() => {
  PlanViewService.listPlans(json => {

    const byUserPlans: UserPlans[] = [];
    json.forEach(plan => {
      const filtered = byUserPlans.filter(value => value.userId == plan.userId);

      let byUser = filtered[0];
      if (filtered.length == 0) {
        byUser = {
          userId: plan.userId,
          username: plan.username,
          activePlan: null,
          plans: []
        }
        byUserPlans.push(byUser);
      }

      if (plan.isPlanActive) {
        byUser.activePlan = plan;
      }

      byUser.plans.push(plan);
    });

    console.log(byUserPlans);

    groupedByUserPlans.value = byUserPlans;
    loading.value = false;
  });
})

function confirmDeleteOrder(event: SubmitEvent) {
  if (confirm("Удалить план?")) {
    return true;
  }
  event.preventDefault();
}

</script>

<template>
  <div class="container">
    <LoadingSpinner text="Загружаем планы" v-if="loading"/>
    <div class="row" v-else>
      <Accordion :name="byUser.username" v-for="byUser in groupedByUserPlans" :key="byUser.username">
        <AccordionItem
            :name="'Текущий активный план (c ' + byUser.activePlan.planStart + ' по ' + byUser.activePlan.planEnd + ')'"
            v-if="byUser.activePlan" :open="true">
          <Plan :plan="byUser.activePlan"/>
        </AccordionItem>
        <AccordionItem :name="'План от ' + plan.planStart + ' по ' + plan.planEnd"
                       v-for="plan in byUser.plans" :key="plan.planId"
                       :open="false">
          <Plan :plan="plan"/>
          <form method="post" class="p-0 mt-2" :action="'/delete-plan/' + plan.planId"
                @submit="confirmDeleteOrder($event as SubmitEvent)">
            <button type="submit" class="btn btn-sm btn-danger mb-2 w-100">
              <i class="bi bi-x-circle me-2"></i> Удалить
            </button>
          </form>
        </AccordionItem>
      </Accordion>
    </div>
  </div>
</template>

<style scoped>

</style>