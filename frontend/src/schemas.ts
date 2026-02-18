import { z } from 'zod'

export const EditReleaseNoteSchema = z.object({
  id: z.number(),
  tag: z.string().min(1),
  summary: z.string().optional(),
  changeNotes: z.array(z.number()).optional(),
})

export const EditChangeNoteSchema = z.object({
  id: z.number(),
  description: z.string().optional(),
  productId: z.number().optional(),
  scopeId: z.number().optional(),
  featureId: z.number().optional(),
  customerId: z.number().optional(),
  developerNotes: z.string().optional(),
  upgradeNotes: z.string().optional(),
  changeSource: z.string().optional(),
})
