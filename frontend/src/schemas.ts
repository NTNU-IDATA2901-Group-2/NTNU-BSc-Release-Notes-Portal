import { z } from 'zod'
import { testingNeedValues } from './utils/types'

// Error messages are i18n keys, translated where they are surfaced (e.g. toasted in ReleaseNoteEdit).
export const ChangeImpactSchema = z.object({
  featureId: z.number({
    required_error: 'validation.changeImpact.feature',
    invalid_type_error: 'validation.changeImpact.feature',
  }),
  whatIsChanged: z.string().min(1, { message: 'validation.changeImpact.whatIsChanged' }),
  whatShouldBeTested: z.string().min(1, { message: 'validation.changeImpact.whatShouldBeTested' }),
  testingNeed: z.enum(testingNeedValues, {
    required_error: 'validation.changeImpact.testingNeed',
    invalid_type_error: 'validation.changeImpact.testingNeed',
  }),
})

export const EditReleaseNoteSchema = z.object({
  tag: z.string().optional(),
  summary: z.string().optional(),
  changeNoteIds: z.array(z.number()).optional(),
  productId: z.number().optional(),
  published: z.boolean(),
  releaseTimeline: z.object({
    previewAvailableFrom: z.string().optional(),
    recommendedTestPhaseFrom: z.string().optional(),
    recommendedTestPhaseTo: z.string().optional(),
    plannedProductionDeployment: z.string().optional(),
  }).optional(),
  knownLimitations: z.array(z.string()).optional(),
  changeImpacts: z.array(ChangeImpactSchema).optional(),
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
  changeNoteDirectory: z.string().min(1, { message: "Required" }),
  pat: z.string().optional(),
})

export const UpdatePromptsSchema = z.object({
  prompts: z.array(z.object({
    id: z.number(),
    name: z.string(),
    prompt: z.string(),
  }))
})

export const DiffReleaseNotesSchema = z.object({
  productId: z.number(),
  releaseNoteOneId: z.number(),
  releaseNoteTwoId: z.number(),
})
