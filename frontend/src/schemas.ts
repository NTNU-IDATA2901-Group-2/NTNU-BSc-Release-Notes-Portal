import { z } from 'zod'

export const EditReleaseNoteSchema = z.object({
  id: z.number(),
  tag: z.string().min(1),
  summary: z.string().optional(),
  changeNotes: z.array(z.number()).optional(),
})