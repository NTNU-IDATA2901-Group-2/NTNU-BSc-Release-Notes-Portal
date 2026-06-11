import { computed, ref, watch, type Ref, type WritableComputedRef } from 'vue'
import type { Router } from 'vue-router'
import { CalendarDate, type DateValue } from '@internationalized/date'

export type SearchParams = Record<string, string>

interface UseSearchParamsOptions {
  /** Keys read from the URL but kept out of the reactive params (e.g. not sent to the API). */
  exclude?: string[]
  /** Extra entries merged into the URL only (never into params). Re-evaluated reactively. */
  extraQuery?: () => SearchParams
}

/** Parses a `yyyy-MM-dd` (or any Date-parseable) query value into a CalendarDate. */
export function parseParamDate(value?: string): CalendarDate | undefined {
  if (!value) return undefined
  const jsDate = new Date(value)
  return new CalendarDate(jsDate.getFullYear(), jsDate.getMonth() + 1, jsDate.getDate())
}

/**
 * Centralizes all URL <-> filter-state logic for a list view.
 *
 * Owns a reactive `params` object (seeded from the current URL and kept in sync
 * with it via `router.replace`) and hands back typed writable computeds that
 * filter components bind to with `v-model`. Filter components themselves stay
 * free of any URL / query-key knowledge.
 */
export function useSearchParams(router: Router, options: UseSearchParamsOptions = {}) {
  const exclude = new Set(options.exclude ?? [])
  const fromUrl = Object.fromEntries(new URL(globalThis.location.href).searchParams.entries())
  const params = ref(
    Object.fromEntries(Object.entries(fromUrl).filter(([key]) => !exclude.has(key))),
  ) as Ref<SearchParams>

  watch(
    [params, () => options.extraQuery?.() ?? {}],
    () => {
      router.replace({ query: { ...params.value, ...(options.extraQuery?.() ?? {}) } })
    },
    { deep: true },
  )

  function setKey(key: string, value: string | undefined) {
    if (value === undefined || value === '') {
      const next = { ...params.value }
      delete next[key]
      params.value = next
    } else {
      params.value = { ...params.value, [key]: value }
    }
  }

  /** A single string value, e.g. `published`. */
  function single(key: string): WritableComputedRef<string | undefined> {
    return computed({
      get: () => params.value[key],
      set: (value) => setKey(key, value),
    })
  }

  /** A comma-separated list, e.g. `productIds`. */
  function csv(key: string): WritableComputedRef<string[]> {
    return computed({
      get: () => params.value[key]?.split(',').filter(Boolean) ?? [],
      set: (values) => setKey(key, values.length ? values.join(',') : undefined),
    })
  }

  /** A `yyyy-MM-dd` date, e.g. `fromDate`. */
  function date(key: string): WritableComputedRef<DateValue | undefined> {
    return computed({
      get: () => parseParamDate(params.value[key]),
      set: (value) => setKey(key, value ? value.toString() : undefined),
    })
  }

  /** A boolean expressed as the presence of a specific value, e.g. `hasReleaseNote=false`. */
  function match(key: string, value: string): WritableComputedRef<boolean> {
    return computed({
      get: () => params.value[key] === value,
      set: (on) => setKey(key, on ? value : undefined),
    })
  }

  function clear() {
    params.value = {}
  }

  return { params, single, csv, date, match, setKey, clear }
}
