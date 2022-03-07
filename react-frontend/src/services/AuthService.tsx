import { Category } from 'models/Category';
import { Post } from 'models/Post';
import { Thread } from 'models/Thread';
import { Tokens } from 'models/Tokens';
import { User } from 'models/User';
import { UserRegistration } from 'models/UserRegistration';
import api from './Api';
import LocalStorage from './LocalStorage';

const AuthService = {
  async login(username: string, password: string): Promise<void> {
    const bodyFormData = new FormData();
    bodyFormData.append('username', username);
    bodyFormData.append('password', password);
    return api
      .post<Tokens>('/login', bodyFormData)
      .then(response => {
        localStorage.setItem('tokens', JSON.stringify(response.data));
        return response.data;
      })
      .then(async _data => {
        const user = await api
          .get<User>('/users/account', {
            // headers: authHeader(),
          })
          .then(res => res.data);

        if (user !== undefined)
          localStorage.setItem('user', JSON.stringify(user));
      });
  },

  async register(user: UserRegistration): Promise<void> {
    const value = await api.post<void>(`/users/register`, user, {});

    return value.data;
  },

  logout() {
    localStorage.removeItem('tokens');
    localStorage.removeItem('user');
  },

  isCurrentUserAtLeastModerator(): boolean {
    const role = LocalStorage.getCurrentUserRole();
    return role?.name === 'Moderator' || role?.name === 'Admin';
  },

  isCurrentUserAdmin(): boolean {
    const role = LocalStorage.getCurrentUserRole();
    return role?.name === 'Admin';
  },

  canUserCreateCategory(): boolean {
    return this.isCurrentUserAdmin();
  },

  canUserEditCategory(category: Category): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        ))
    );
  },

  canUserDeleteCategory(_category: Category): boolean {
    return this.isCurrentUserAdmin();
  },

  canUserCreateThread(category: Category): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (category.active &&
        category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        ))
    );
  },

  canUserEditThread(thread: Thread): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        thread.category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        ))
    );
  },

  canUserDeleteThread(thread: Thread): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        thread.category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        ))
    );
  },

  canUserCreatePost(thread: Thread): boolean {
    const isThreadActive = thread.active;
    const allowedRoles = thread.category.roles;
    return (
      this.isCurrentUserAdmin() ||
      (isThreadActive &&
        allowedRoles.some(r => r.id === LocalStorage.getCurrentUserRole()?.id))
    );
  },

  canUserEditPost(post: Post): boolean {
    const user = LocalStorage.getCurrentUser();
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        post.thread.category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        )) ||
      user?.id === post.user.id
    );
  },

  canUserDeletePost(post: Post): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        post.thread.category.roles.some(
          r => r.id === LocalStorage.getCurrentUserRole()?.id,
        ))
    );
  },
};

export default AuthService;
