import axios from 'axios';
import { Category } from 'models/Category';
import { Post } from 'models/Post';
import { Role } from 'models/Role';
import { Thread } from 'models/Thread';
import { Tokens } from 'models/Tokens';
import { User } from 'models/User';
import UserService from './UserService';

const AuthService = {
  async login(username: string, password: string): Promise<void> {
    const bodyFormData = new FormData();
    bodyFormData.append('username', username);
    bodyFormData.append('password', password);
    return axios
      .post<Tokens>('/login', bodyFormData)
      .then(response => {
        localStorage.setItem('tokens', JSON.stringify(response.data));
        return response.data;
      })
      .then(async _data => {
        const user = await UserService.getUserAccount();

        if (user !== undefined)
          localStorage.setItem('user', JSON.stringify(user));
      });
  },

  logout() {
    localStorage.removeItem('tokens');
    localStorage.removeItem('user');
  },

  getCurrentUserTokens(): Tokens | undefined {
    const tokens = localStorage.getItem('tokens');
    if (tokens !== null) {
      return JSON.parse(tokens);
    }
    return undefined;
  },

  getCurrentUser(): User | undefined {
    const user = localStorage.getItem('user');
    if (user !== null && user !== undefined) {
      return JSON.parse(user);
    }
    return undefined;
  },

  getCurrentUserRole(): Role | undefined {
    const user = localStorage.getItem('user');
    if (user !== null && user !== undefined) {
      const u: User = JSON.parse(user);
      return u.role;
    }
    return undefined;
  },

  isCurrentUserAtLeastModerator(): boolean {
    const role = this.getCurrentUserRole();
    return role?.name === 'Moderator' || role?.name === 'Admin';
  },

  isCurrentUserAdmin(): boolean {
    const role = this.getCurrentUserRole();
    return role?.name === 'Admin';
  },

  canUserCreateCategory(): boolean {
    return this.isCurrentUserAdmin();
  },

  canUserEditCategory(category: Category): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        category.roles.some(r => r.id === this.getCurrentUserRole()?.id))
    );
  },

  canUserDeleteCategory(_category: Category): boolean {
    return this.isCurrentUserAdmin();
  },

  canUserCreateThread(category: Category): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (category.active &&
        category.roles.some(r => r.id === this.getCurrentUserRole()?.id))
    );
  },

  canUserEditThread(thread: Thread): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        thread.category.roles.some(r => r.id === this.getCurrentUserRole()?.id))
    );
  },

  canUserDeleteThread(thread: Thread): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        thread.category.roles.some(r => r.id === this.getCurrentUserRole()?.id))
    );
  },

  canUserCreatePost(thread: Thread): boolean {
    const isThreadActive = thread.active;
    const allowedRoles = thread.category.roles;
    return (
      this.isCurrentUserAdmin() ||
      (isThreadActive &&
        allowedRoles.some(r => r.id === this.getCurrentUserRole()?.id))
    );
  },

  canUserEditPost(post: Post): boolean {
    const user = this.getCurrentUser();
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        post.thread.category.roles.some(
          r => r.id === this.getCurrentUserRole()?.id,
        )) ||
      user?.id === post.user.id
    );
  },

  canUserDeletePost(post: Post): boolean {
    return (
      this.isCurrentUserAdmin() ||
      (this.isCurrentUserAtLeastModerator() &&
        post.thread.category.roles.some(
          r => r.id === this.getCurrentUserRole()?.id,
        ))
    );
  },
};

export default AuthService;
