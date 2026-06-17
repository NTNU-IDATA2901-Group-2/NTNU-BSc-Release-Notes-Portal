<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button } from './ui/button';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter, DialogDescription } from './ui/dialog';

const props = defineProps<{
  open: boolean;
  titleKey: string;
  descriptionKey: string;
  mode: DialogPromptMode;
  onConfirm?: () => void;
  onCancel?: () => void;
}>()

export type DialogPromptMode = 'delete' | 'confirm';

const emit = defineEmits<{
  'update:open': [value: boolean];
}>()

const handleClose = () => {
  if (props.onCancel) {
    props.onCancel();
  }
  emit('update:open', false);
}

const handleConfirm = () => {
  if (props.onConfirm) {
    props.onConfirm();
  }
  emit('update:open', false);
}

const { t } = useI18n();

</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>{{ t(props.titleKey) }}</DialogTitle>
        <DialogDescription>
          {{ t(props.descriptionKey) }}
        </DialogDescription>
      </DialogHeader>
      <slot/>
      <DialogFooter>
        <Button variant="outline" @click="handleClose">{{ t('button.cancel') }}</Button>
        <Button variant="destructive" @click="handleConfirm">{{ t(`button.${props.mode}`) }}</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>
</template>