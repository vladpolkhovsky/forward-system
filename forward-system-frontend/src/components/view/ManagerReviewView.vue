<script setup lang="ts">

import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {ref} from "vue";
import type {V3SearchOrderReviewDto} from "@/core/dto/V3SearchOrderReviewDto.ts";
import OrderStatusIcon from "@/components/elements/OrderStatusIcon.vue";
import Accordion from "@/components/elements/Accordion.vue";
import AccordionItem from "@/components/elements/AccordionItem.vue";
import {ReviewService} from "@/core/ReviewService.ts";

const notYetLoaded = ref(true);
const loading = ref(true);
const techNumberSearch = ref("");

const maxPageCount = ref(0);
const cPage = ref(0);
const loadedReviews = ref<V3SearchOrderReviewDto[]>();

function handleSearchClick(page: number = 0) {
  if (page <= 0) {
    page = 0;
  }

  if (page >= maxPageCount.value) {
    page = maxPageCount.value - 1;
  }

  cPage.value = page;

  loading.value = true;

  ReviewService.searchReviewsByTechNumberLike(techNumberSearch.value, page, page => {
    cPage.value = page.pageable.pageNumber;
    maxPageCount.value = page.totalPages;
    loadedReviews.value = page.content;

    loading.value = false;
    notYetLoaded.value = false;
  });
}

</script>

<template>
  <div class="row">
    <div class="input-group flex-nowrap">
      <span class="input-group-text" id="addon-wrapping"><i class="bi bi-search"></i></span>
      <input type="number" class="form-control" placeholder="Поиск по ТЗ" v-model="techNumberSearch"/>
      <button class="btn btn-outline-secondary" type="button" id="button-addon2" @click="handleSearchClick()">Поиск
      </button>
    </div>
  </div>

  <LoadingSpinner text="Загрузка информации о проверках" :margin-top="true" v-if="!notYetLoaded && loading"/>
  <div class="row mt-3" v-if="maxPageCount > 1">
    <nav>
      <ul class="pagination">
        <li class="page-item">
          <a class="page-link" @click="handleSearchClick(cPage - 1)" aria-label="Previous">
            <span aria-hidden="true">&laquo;</span>
          </a>
        </li>
        <li :class="['page-item', { 'active': pageIndex - 1 == cPage}]" v-for="pageIndex in maxPageCount">
          <a class="page-link" @click="handleSearchClick(pageIndex - 1)">{{
              pageIndex
            }}</a>
        </li>
        <li class="page-item">
          <a class="page-link" @click="handleSearchClick(cPage + 1)" aria-label="Next">
            <span aria-hidden="true">&raquo;</span>
          </a>
        </li>
      </ul>
    </nav>
  </div>

  <div class="row" v-if="!loading">
    <div class="card mt-2" v-for="review in loadedReviews">
      <div class="card-body">
        <h5 class="card-title">ТЗ №<span class="fw-bold me-3">{{ review.orderTechNumber }}</span>
          <OrderStatusIcon :name="review.status" :rus-name="review.statusRusName"/>
        </h5>
        <h6 class="card-subtitle mb-2 text-body-secondary">Всего {{ review.reviews.length }} запросов на
          проверку.</h6>
        <template v-if="review.reviews.length > 0">
          <div class="container">
            <div class="row">
              <div class="col-12">
                <Accordion name="Проверки ТЗ">
                  <AccordionItem
                      :name="`Запрос от ${cReview.createdAt} (${cReview.isApproved ? 'Проверено' : 'Не проверено'})`"
                      :open="index == 0" v-for="(cReview, index) in review.reviews">
                    <div class="input-group input-group-sm  mb-2" v-if="cReview.resultMark">
                      <span class="input-group-text">Оценка эксперта</span>
                      <input type="text" class="form-control" readonly :value="cReview.resultMark">
                    </div>
                    <div class="input-group input-group-sm mb-2" v-if="cReview.resultDate">
                      <span class="input-group-text">Дата проверки</span>
                      <input type="text" class="form-control" readonly :value="cReview.resultDate">
                    </div>
                    <div class="input-group input-group-sm mb-2" v-if="cReview.createdAt">
                      <span class="input-group-text">Дата создания запроса</span>
                      <input type="text" class="form-control" readonly :value="cReview.createdAt">
                    </div>
                    <a :href="`/expert-review-order-view/${cReview.orderId}/${cReview.id}`">Карточка проверки</a>
                  </AccordionItem>
                </Accordion>
              </div>
            </div>
          </div>
        </template>
        <p class="card-text fst-italic" v-else>По данному заказу ещё нет проверок</p>
      </div>
    </div>
  </div>

  <div v-if="notYetLoaded"
       class="row d-flex flex-column display-6 align-items-center mt-3 justify-content-center text-center">
    <i class="bi bi-book"></i>
    <span>Введите номер ТЗ для поиска рецензий</span>
  </div>
</template>

<style scoped>

</style>