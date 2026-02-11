import { createRouter, createWebHistory } from 'vue-router'

import ChangeNotesView from '../views/ChangeNotesView.vue'
import ReleaseNotesView from '../views/ReleaseNotesView.vue'
import ReleaseNoteView from '@/views/ReleaseNoteView.vue'
import ChangeNoteView from '@/views/ChangeNoteView.vue'

export const routeNames = {
  releaseNotes: '/',
  changeNotes: '/change-notes',
  releaseNote: '/release-note/:id',
  changeNote: '/change-note/:id',
}

const routes = [
  { path: routeNames.releaseNotes, component: ReleaseNotesView },
  { path: routeNames.changeNotes, component: ChangeNotesView },
  { path: routeNames.releaseNote, component: ReleaseNoteView },
  { path: routeNames.changeNote, component: ChangeNoteView },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})