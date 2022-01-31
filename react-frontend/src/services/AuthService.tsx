import axios from 'axios';
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
};

export default AuthService;
