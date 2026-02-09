import { createRouter, createWebHistory } from 'vue-router'

import ChangeNotesView from '../views/ChangeNotesView.vue'
import ReleaseNotesView from '../views/ReleaseNotesView.vue'

export const routeNames = {
  releaseNotes: '/',
  changeNotes: '/change-notes',
}

const routes = [
  { path: routeNames.releaseNotes, component: ReleaseNotesView },
  { path: routeNames.changeNotes, component: ChangeNotesView },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})