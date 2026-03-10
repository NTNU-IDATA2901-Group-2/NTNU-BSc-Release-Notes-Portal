import { z } from 'zod'

export const EditReleaseNoteSchema = z.object({
  tag: z.string().optional(),
  summary: z.string().optional(),
  changeNoteIds: z.array(z.number()).optional(),
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

export const CreateGitRepositorySchema = z.object({
  name: z.string().min(1, { message: "Required" }),
  url: z.string().min(1, { message: "Required" }),
})