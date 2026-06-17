import type { ChangeNote } from "./types"

export function getLabelFromChangeNote(cn: ChangeNote): string | null {
  let label
  if (cn.title) {
    label = cn.title + (cn.reference ? ` (${cn.reference})` : '')
  } else {
    label = cn.reference ? cn.reference : null
  }
  return label
}