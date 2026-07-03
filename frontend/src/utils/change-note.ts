import type { ChangeNote, Customer } from "./types"

/**
 * Returns the distinct customers referenced by the given change notes, preserving
 * first-seen order.
 */
export function uniqueCustomers(changeNotes: ChangeNote[]): Customer[] {
  const customers = new Array<Customer>()
  changeNotes.forEach(change => {
    if (change.customer && !customers.some(c => c.id === change.customer?.id)) {
      customers.push(change.customer)
    }
  })
  return customers
}

export function getLabelFromChangeNote(cn: ChangeNote): string | null {
  let label
  if (cn.title) {
    label = cn.title + (cn.reference ? ` (${cn.reference})` : '')
  } else {
    label = cn.reference ? cn.reference : null
  }
  return label
}