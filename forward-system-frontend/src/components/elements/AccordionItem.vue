<script setup lang="ts">
import {onMounted, ref, useId, watch} from "vue";
import {Collapse} from "bootstrap";

interface Props {
  name: string;
  open: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits(['update:open']);

const collapseRef = ref<HTMLElement>();
const accordionButtonRef = ref<HTMLButtonElement>();
const collapseInstance = ref<Collapse>();
const collapseId = ref(`collapse-` + useId());

onMounted(() => {
  if (collapseRef.value) {
    collapseInstance.value = new Collapse(collapseRef.value, {
      toggle: false,
    });

    // Слушаем события коллапса для обновления состояния кнопки
    collapseRef.value.addEventListener('show.bs.collapse', () => {
      if (accordionButtonRef.value) {
        accordionButtonRef.value.classList.remove('collapsed');
        accordionButtonRef.value.setAttribute('aria-expanded', 'true');
      }
      emit('update:open', true);
    });

    collapseRef.value.addEventListener('hide.bs.collapse', () => {
      if (accordionButtonRef.value) {
        accordionButtonRef.value.classList.add('collapsed');
        accordionButtonRef.value.setAttribute('aria-expanded', 'false');
      }
      emit('update:open', false);
    });

    props.open ? show() : hide();
  }
});

watch(() => props.open, (newVal) => {
  newVal ? show() : hide();
});

const show = () => {
  collapseInstance.value?.show();
};

const hide = () => {
  collapseInstance.value?.hide();
};

const toggle = () => {
  collapseInstance.value?.toggle();
};
</script>

<template>
  <div class="accordion-item">
    <h2 class="accordion-header">
      <button ref="accordionButtonRef"
              class="accordion-button p-2"
              :class="{ 'collapsed': !open }"
              type="button"
              :aria-expanded="open"
              :aria-controls="collapseId"
              @click="toggle">
        {{ name }}
      </button>
    </h2>
    <div :id="collapseId"
         ref="collapseRef"
         class="accordion-collapse collapse"
         :class="{ 'show': open }">
      <div class="accordion-body p-2">
        <slot/>
      </div>
    </div>
  </div>
</template>