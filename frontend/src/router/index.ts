import { createRouter, createWebHistory } from 'vue-router'
import Home from '../pages/Home.vue'
import GuideDetail from '../pages/GuideDetail.vue'
import Editor from '../pages/Editor.vue'
import Login from '../pages/Login.vue'
import Register from '../pages/Register.vue'
import Me from '../pages/Me.vue'
import AdminUsers from '../pages/AdminUsers.vue'
import Feed from '../pages/Feed.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: Home },
    { path: '/feed', component: Feed },
    { path: '/guides/:id', component: GuideDetail },
    { path: '/editor', component: Editor },
    { path: '/login', component: Login },
    { path: '/register', component: Register },
    { path: '/me', component: Me },
    { path: '/admin/users', component: AdminUsers }
  ]
})

