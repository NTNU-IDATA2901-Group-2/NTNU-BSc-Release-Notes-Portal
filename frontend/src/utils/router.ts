import { createRouter, createWebHistory } from 'vue-router'

import ChangeNotesView from '../views/ChangeNotesView.vue'
import ReleaseNotesView from '../views/ReleaseNotesView.vue'
import ReleaseNoteView from '@/views/ReleaseNoteView.vue'
import ChangeNoteView from '@/views/ChangeNoteView.vue'
import SignInView from '@/views/SignInView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

export const routeNames = {
  releaseNotes: '/',
  changeNotes: '/change-notes',
  releaseNote: '/release-notes/:id',
  changeNote: '/change-notes/:id',
  signIn: '/sign-in',
  notFound: '/:pathMatch(.*)*',
}

const routes = [
  { path: routeNames.releaseNotes, component: ReleaseNotesView },
  { path: routeNames.changeNotes, component: ChangeNotesView },
  { path: routeNames.releaseNote, component: ReleaseNoteView },
  { path: routeNames.changeNote, component: ChangeNoteView },
  { path: routeNames.signIn, component: SignInView },
  { path: routeNames.notFound, component: NotFoundView },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})