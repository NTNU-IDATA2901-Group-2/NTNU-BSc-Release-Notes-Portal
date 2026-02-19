import { ref, watch } from "vue";

const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)")

const theme = ref<"light" | "dark">(
  localStorage.getItem("theme") as "light" | "dark" ??
  (mediaQuery.matches ? "dark" : "light")
)

export function useTheme() {
  watch(
    theme,
    (value) => {
      document.documentElement.classList.toggle("dark", value === "dark")
      localStorage.setItem("theme", value)
    },
    { immediate: true }
  )

  return { theme }
}