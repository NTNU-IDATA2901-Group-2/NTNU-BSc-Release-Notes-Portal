<script setup lang="ts">
import Button from '@/components/ui/button/Button.vue';
import { useRoute } from 'vue-router';
import { ArrowLeft, EllipsisVertical  } from "lucide-vue-next"
import Separator from '@/components/ui/separator/Separator.vue';
import Badge from '@/components/ui/badge/Badge.vue';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import Spinner from '@/components/ui/spinner/Spinner.vue';
import { Pencil, Trash2, Eye, FileDown, Ban, Save } from "lucide-vue-next"
import { computed, ref, watch } from 'vue';
import Input from '@/components/ui/input/Input.vue';
import { Textarea } from '@/components/ui/textarea';
import { useChangeNote } from '@/api/change-note-api';
import TagSelect from '@/components/TagSelect.vue';

  const isEditing = ref(false)

  const route = useRoute();

  const id = route.params.id as string;
  const { isPending, isFetching, isError, data: changeNote } = useChangeNote(id);

  const selectedProduct = ref<number | string>()
  const selectedScope = ref<number | string>()
  const selectedFeature = ref<number | string>()
  const selectedCustomer = ref<number | string>()

  watch(changeNote, (cn) => {
    if (cn) {
      if (cn.product) {selectedProduct.value = cn.product.id} else {selectedProduct.value = 'none'}
      if (cn.scope) {selectedScope.value = cn.scope.id} else {selectedScope.value = 'none'}
      if (cn.feature) {selectedFeature.value = cn.feature.id} else {selectedFeature.value = 'none'}
      if (cn.customer) {selectedCustomer.value = cn.customer.id} else {selectedCustomer.value = 'none'}
    }
  })
</script>

<template>
  <div class="flex flex-col items-center px-4 mb-20">
    <Button variant="outline" class="mb-4 absolute left-4 mt-4 lg:left-10 lg:mt-10" @click="$router.back()"><ArrowLeft />Previous</Button>
    <div class="md:hidden flex w-full mt-4 justify-end gap-2">
      <Button v-if="isEditing" variant="outline" @click="isEditing = false">Cancel <Ban /></Button>
      <Button disabled v-if="isEditing" variant="outline" >Save <Save /></Button>
    </div>
    <Spinner v-if="isPending || isFetching" />
    <h1 v-if="isError">Error retreiving change note</h1>

    <div v-if="!isPending && !isFetching && !isError && changeNote" class="flex flex-col gap-16 flex-1 w-full items-center mt-16 mx-4 lg:w-4xl md:mt-42">
      <div class="flex flex-col gap-4 w-full">
        <div class="flex flex-row items-center justify-between w-full">
          <div class="flex items-center gap-4">
            <h1 v-if="!isEditing" class="text-2xl max-w-60 whitespace-nowrap overflow-hidden">{{ changeNote.reference }}</h1>
            <Input v-if="isEditing" class="w-full" v-model="changeNote.reference"/>
            <Badge v-if="!isEditing" class="h-6" :variant="changeNote.published ? 'success' : 'destructive'">{{ changeNote.published ? 'Published' : 'Private' }}</Badge>
          </div>
          <div class="flex gap-4">
          	<DropdownMenu v-if="!isEditing">
              <DropdownMenuTrigger class="cursor-pointer hover:bg-border/50 rounded-md p-2 transition-colors">
                <EllipsisVertical class="text-text-primary"/>
              </DropdownMenuTrigger>
              <DropdownMenuContent class="mr-6 lg:mr-20 mt-2">
                  <DropdownMenuItem @click="isEditing = !isEditing">
                    <div class="w-full flex gap-2">
                        <p class="text-text-dark-static ml-auto">Edit</p>
                        <Pencil class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">Delete</p>
                        <Trash2 class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">{{ changeNote.published ? 'Publish' : 'Unpublish' }}</p>
                        <Eye class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
                  <DropdownMenuItem disabled>
                    <div class="w-full flex gap-2">
                        <p class="ml-auto text-text-dark-static">Export</p>
                        <FileDown class="text-text-dark-static"/>
                    </div>
                  </DropdownMenuItem>
              </DropdownMenuContent>
	          </DropdownMenu>
            <Button class="hidden md:flex" v-if="isEditing" variant="outline" @click="isEditing = false">Cancel <Ban /></Button>
          	<Button class="hidden md:flex" disabled v-if="isEditing" variant="outline" >Save <Save /></Button>
          </div>
          </div>

          <p v-if="!isEditing" class="">{{ changeNote.description }}</p>
          <Textarea v-if="isEditing" class="w-full" v-model="changeNote.description"/>
          <div v-if="!isEditing" class="flex flex-wrap gap-4">
            <Badge v-if="changeNote.product" class="h-6">{{ changeNote.product.name }}</Badge>
            <Badge v-if="changeNote.scope" class="h-6">{{ changeNote.scope.name }}</Badge>
            <Badge v-if="changeNote.feature" class="h-6">{{ changeNote.feature.name }}</Badge>
            <Badge v-if="changeNote.customer" class="h-6">{{ changeNote.customer.name }}</Badge>
          </div>
          <div v-if="isEditing" class="flex flex-wrap justify-between gap-4">
            <div class="flex flex-col gap-1">
              <h4 class="text-md">Product</h4>
              <TagSelect mode="product" :selected-id="changeNote.product.id" v-model="selectedProduct"/>
            </div>
            <div class="flex flex-col gap-1">
              <h4 class="text-md">Scope</h4>
              <TagSelect mode="scope" :selected-id="changeNote.scope.id" v-model="selectedScope"/>
            </div>
            <div class="flex flex-col gap-1">
              <h4 class="text-md">Feature</h4>
              <TagSelect mode="feature" :selected-id="changeNote.feature.id" v-model="selectedFeature"/>
            </div>
            <div class="flex flex-col gap-1">
              <h4 class="text-md">Customer</h4>
              <TagSelect mode="customer" :selected-id="changeNote.customer.id" v-model="selectedCustomer"/>
            </div>
          </div>
      </div>
      <Separator class="w-full h-2"/>
      <div class="flex flex-col w-full text-xl gap-10">
          <div>
            <h3 class="text-lg">Developer Notes</h3>
            <p v-if="!isEditing" class="text-sm">{{ changeNote.developerNotes }}</p>
            <Textarea v-if="isEditing" class="w-full" v-model="changeNote.developerNotes"/>
          </div>
          <div>
            <h3 class="text-lg">Upgrade Notes</h3>
            <p v-if="!isEditing" class="text-sm">{{ changeNote.upgradeNotes }}</p>
            <Textarea v-if="isEditing" class="w-full" v-model="changeNote.upgradeNotes"/>
          </div>
      </div>
    </div>
  </div>
</template>