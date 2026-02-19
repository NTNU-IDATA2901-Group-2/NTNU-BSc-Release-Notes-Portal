import { z } from 'zod'

export const EditReleaseNoteSchema = z.object({
  tag: z.string().min(1),
  summary: z.string().optional(),
  changeNotes: z.array(z.number()).optional(),
  published: z.boolean(),
})

export const EditChangeNoteSchema = z.object({
  reference: z.string().optional(),
  description: z.string().optional(),
  developerNotes: z.string().optional(),
  upgradeNotes: z.string().optional(),
  changeSource: z.string().optional(),
  productId: z.number().optional(),
  scopeId: z.number().optional(),
  featureId: z.number().optional(),
  customerId: z.number().optional(),
})