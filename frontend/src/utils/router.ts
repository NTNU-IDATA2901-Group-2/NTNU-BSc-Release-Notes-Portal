import { createRouter, createWebHistory } from 'vue-router'

import ChangeNotesView from '../views/ChangeNotesView.vue'
import ReleaseNotesView from '../views/ReleaseNotesView.vue'
import ReleaseNoteView from '@/views/ReleaseNoteView.vue'
import ChangeNoteView from '@/views/ChangeNoteView.vue'
import SignInView from '@/views/SignInView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import keycloak from './keycloak'
import GitRepositoriesView from '@/views/GitRepositoriesView.vue'
import EditPromptsView from '@/views/EditPromptsView.vue'

export const routeNames = {
  releaseNotes: '/',
  changeNotes: '/change-notes',
  releaseNote: '/release-notes/:id',
  changeNote: '/change-notes/:id',
  signIn: '/sign-in',
  notFound: '/:pathMatch(.*)*',
  gitRepositories: '/git-repositories',
  editPrompts: '/edit-prompts',
}

const routes = [
  { path: routeNames.releaseNotes, component: ReleaseNotesView, meta: { requiresAuth: true } },
  { path: routeNames.changeNotes, component: ChangeNotesView, meta: { requiresAuth: true } },
  { path: routeNames.releaseNote, component: ReleaseNoteView, meta: { requiresAuth: true } },
  { path: routeNames.changeNote, component: ChangeNoteView, meta: { requiresAuth: true } },
  { path: routeNames.signIn, component: SignInView },
  { path: routeNames.notFound, component: NotFoundView },
  { path: routeNames.gitRepositories, component: GitRepositoriesView, meta: { requiresAuth: true } },
  { path: routeNames.editPrompts, component: EditPromptsView, meta: { requiresAuth: true } },
]

export const router = createRouter({
  history: createWebHistory(),
  routes,
})


router.beforeEach(async (to, _from, next) => {
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (keycloak.authenticated) {
      next();
    } else {
      console.warn("User is not authenticated. Redirecting to sign-in page.");
      next(routeNames.signIn);
    }
  } else {
    next();
  }
});