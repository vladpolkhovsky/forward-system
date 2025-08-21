<script setup lang="ts">
import {Modal} from "bootstrap"
import {computed, onMounted, ref, useId} from "vue";
import LoadingSpinner from "@/components/elements/LoadingSpinner.vue";
import {AttachmentsService} from "@/core/AttachmentsService.ts";

enum FileStatus {
  NOT_LOADED = 'Не загружен',
  LOADING = 'Загружается',
  LOADED = 'Загружен'
}

interface FileDto {
  id: number,
  sysId: string,
  fileName: string,
  file: File
  status: FileStatus
}

const modalId = ref(useId())
const fileAddInputRef = ref<InstanceType<typeof Node>>();
const buttonRef = ref<InstanceType<typeof HTMLButtonElement>>();
const files = ref<FileDto[]>([])
const readyFiles = computed(() => files.value.filter(file => file.status == FileStatus.LOADED && file.id))

const baseId = useId();
let cId = 0;

function getId(): string {
  cId++;
  return baseId + "-" + cId;
}

let modal: Modal = null;

onMounted(() => {
  modal = new Modal('#' + modalId.value);
});

function handleFileAdded(event: Event) {
  const input = event.target as HTMLInputElement;
  for (let file of input.files) {
    files.value.push({
      file: file,
      sysId: getId(),
      fileName: file.name,
      id: null,
      status: FileStatus.NOT_LOADED
    });
  }
  console.log(files.value);
  input.value = null;
}

function handleDeleteFile(sysId: string) {
  console.log(files.value);
  files.value = files.value.filter(t => t.sysId != sysId);
  console.log(files.value);
}

async function startLoading() {
  for (let file of files.value) {
    if (file.status != FileStatus.LOADED) {
      file.status = FileStatus.LOADING
    }
  }
  for (let file of files.value) {
    if (file.status == FileStatus.LOADING) {
      let formData = new FormData();
      formData.append("file", file.file, file.fileName);
      AttachmentsService.upload(formData, (id) => {
        file.id = id;
        file.status = FileStatus.LOADED
      });
    }
  }
}

const clearFiles = () => {
  files.value = [];
}

const setAttribute = (name: string, value: string) => {
  buttonRef?.value?.setAttribute(name, value);
}

const removeAttribute = (name: string) => {
  buttonRef?.value?.removeAttribute(name);
}

defineExpose({
  readyFiles,
  clearFiles,
  setAttribute,
  removeAttribute
})

</script>

<template>
  <button type="button" class="btn btn-outline-primary btn-sm w-100 position-relative" @click="modal.show()" ref="buttonRef">
    Файлы
    <span class="position-absolute top-0 start-100 translate-middle badge rounded-pill bg-danger"
          v-if="files.filter(value => value.status == FileStatus.LOADED).length">
      {{ files.filter(value => value.status == FileStatus.LOADED).length }}
    <span class="visually-hidden">files added</span>
  </span>
  </button>

  <!--  Modal -->
  <div class="modal modal-lg fade" tabindex="-1" :id="modalId" data-bs-backdrop="static">
    <div class="modal-dialog">
      <div class="modal-content">
        <div class="modal-header">
          <h5 class="modal-title">Добавить файлы</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"
                  @click="modal.hide()"></button>
        </div>
        <div class="modal-body">
          <div class="alert alert-warning mb-3" role="alert" v-if="files.length == 0">
            Файлы не добавлены.
          </div>
          <div class="mb-3" v-if="files.length > 0">
            <div class="input-group input-group-sm mb-2" v-for="file in files">
              <input type="text" class="form-control" placeholder="Название файла"
                     :disabled="file.status != FileStatus.NOT_LOADED"
                     v-model="file.fileName">
              <span class="input-group-text" v-if="file.status == FileStatus.LOADING">
                <LoadingSpinner/></span>
              <span :class="[ 'input-group-text', {
                'text-bg-primary': file.status == FileStatus.LOADING,
                'text-bg-danger': file.status == FileStatus.NOT_LOADED,
                'text-bg-success': file.status == FileStatus.LOADED
              }]">{{ file.status }}</span>
              <button class="btn btn-outline-danger btn-sm" type="button" @click="handleDeleteFile(file.sysId)">Удалить
              </button>
            </div>
          </div>
          <div class="d-flex gap-5 justify-content-between align-items-sm-baseline">
            <div class="w-50">
              <label class="form-label">Добавит файлы</label>
              <input class="form-control form-control-sm" multiple type="file" @change="handleFileAdded"
                     ref="fileAddInputRef">
            </div>
            <div class="w-50" v-if="files.filter(value => value.status == FileStatus.NOT_LOADED).length">
              <label class="form-label">Отредактируйте названия файлов и загрузите их</label>
              <button class="btn btn-primary" type="button" @click="startLoading()">Загрузить выбранные</button>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" @click="modal.hide()">Закрыть
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>

</style>