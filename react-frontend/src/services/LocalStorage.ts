import { Role } from 'models/Role';
import { Tokens } from 'models/Tokens';
import { User } from 'models/User';

const LocalStorage = {
  setNewAccessToken(accessToken: string) {
    const tokens = this.getCurrentUserTokens();
    if (tokens !== undefined) {
      tokens.accessToken = accessToken;
    }
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
};

export default LocalStorage;
