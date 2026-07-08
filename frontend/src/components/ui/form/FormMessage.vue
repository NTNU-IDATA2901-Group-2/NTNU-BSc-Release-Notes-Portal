<script lang="ts" setup>
import type { HTMLAttributes } from "vue"
import { ErrorMessage } from "vee-validate"
import { toValue } from "vue"
import { useI18n } from "vue-i18n"
import { cn } from "@/utils/utils"
import { useFormField } from "./useFormField"

const props = defineProps<{
  class?: HTMLAttributes["class"]
}>()

const { name, formMessageId } = useFormField()

// Schema error messages are i18n keys (see schemas.ts); non-key messages pass through unchanged.
const { t, te } = useI18n()
</script>

<template>
  <ErrorMessage
    :id="formMessageId"
    v-slot="{ message }"
    data-slot="form-message"
    as="p"
    :name="toValue(name)"
    :class="cn('text-destructive text-sm', props.class)"
  >
    {{ message && te(message) ? t(message) : message }}
  </ErrorMessage>
</template>
