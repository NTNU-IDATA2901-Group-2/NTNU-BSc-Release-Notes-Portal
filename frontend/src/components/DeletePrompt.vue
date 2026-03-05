<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from './ui/dialog';

const props = defineProps<{
  open: boolean;
  onConfirm: () => void;
}>()

const emit = defineEmits<{
  'update:open': [value: boolean];
}>()

const handleClose = () => {
  emit('update:open', false);
}

const handleConfirm = () => {
  props.onConfirm();
  emit('update:open', false);
}

const { t } = useI18n();

</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{ t('deletePrompt.title') }}</DialogTitle>
        <DialogDescription>
          {{ t('deletePrompt.description') }}
        </DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button variant="outline" @click="handleClose">Cancel</Button>
        <Button variant="destructive" @click="handleConfirm">Delete</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>


</template>