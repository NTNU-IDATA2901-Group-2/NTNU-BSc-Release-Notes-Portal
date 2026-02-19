<script setup lang="ts">
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

</script>

<template>
  <Dialog :open="open" @update:open="handleClose">
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Are you sure you want to delete this change note?</DialogTitle>
        <DialogDescription>
          This action cannot be undone.
        </DialogDescription>
      </DialogHeader>
      <DialogFooter>
        <Button variant="outline" @click="handleClose">Cancel</Button>
        <Button variant="destructive" @click="handleConfirm">Delete</Button>
      </DialogFooter>
    </DialogContent>
  </Dialog>


</template>