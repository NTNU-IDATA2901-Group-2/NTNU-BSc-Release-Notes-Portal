import { onBeforeUnmount, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { toast } from 'vue-sonner'

/**
 * Copies text to the clipboard and tracks which item was most recently copied,
 * so a transient "copied" state can be shown. The marker resets after `resetMs`.
 *
 * @param resetMs how long, in milliseconds, the copied marker stays set.
 */
export function useCopyToClipboard(resetMs = 5000) {
  const { t } = useI18n()
  const copiedKey = ref<string | null>(null)
  let resetTimeout: ReturnType<typeof setTimeout> | null = null

  const clearResetTimeout = () => {
    if (resetTimeout) {
      clearTimeout(resetTimeout)
      resetTimeout = null
    }
  }

  const copy = (text: string | null | undefined, key: string) => {
    navigator.clipboard.writeText(text ?? '')
      .then(() => {
        copiedKey.value = key
        clearResetTimeout()
        resetTimeout = setTimeout(() => {
          copiedKey.value = null
          resetTimeout = null
        }, resetMs)
        toast.success(t('toast.copySuccess'))
      })
      .catch((error) => {
        console.error('Error copying to clipboard:', error)
        toast.error(t('toast.copyError'))
      })
  }

  onBeforeUnmount(() => {
    copiedKey.value = null
    clearResetTimeout()
  })

  return { copiedKey, copy }
}
