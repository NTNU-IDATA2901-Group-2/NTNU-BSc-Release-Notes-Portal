import { z } from 'zod'

export const EditReleaseNoteSchema = z.object({
  tag: z.string().optional(),
  summary: z.string().optional(),
  changeNoteIds: z.array(z.number()).optional(),
  published: z.boolean(),
  releaseTimeline: z.object({
    previewAvailableFrom: z.string().optional(),
    recommendedTestPhaseFrom: z.string().optional(),
    recommendedTestPhaseTo: z.string().optional(),
    plannedProductionDeployment: z.string().optional(),
  }).optional(),
  knownLimitations: z.array(z.string()).optional()
})

export const EditChangeNoteSchema = z.object({
  title: z.string().optional(),
  reference: z.string().optional(),
  description: z.string().optional(),
  developerNotes: z.string().optional(),
  upgradeNotes: z.string().optional(),
  productId: z.number().optional(),
  scopeId: z.number().optional(),
  featureId: z.number().optional(),
  customerId: z.number().optional(),
  viewableByEveryone: z.boolean().optional(),
})

export const CreateGitRepositorySchema = z.object({
  name: z.string().min(1, { message: "Required" }),
  url: z.string().min(1, { message: "Required" }),
})

export const UpdatePromptsSchema = z.object({
  prompts: z.array(z.object({
    id: z.number(),
    name: z.string(),
    prompt: z.string(),
  }))
})