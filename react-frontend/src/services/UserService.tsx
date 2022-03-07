import { User } from 'models/User';
import api from './Api';

const UserService = {
  async getUserAccount(): Promise<User | undefined> {
    const value = await api.get<User>('/users/account', {
      // headers: authHeader(),
    });
    return value.data;
  },
};
export default UserService;
