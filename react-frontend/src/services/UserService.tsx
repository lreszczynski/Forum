import axios from 'axios';
import { User } from 'models/User';
import authHeader from './AuthHeader';

const UserService = {
  async getUserAccount(): Promise<User | undefined> {
    const value = await axios.get<User>('/users/account', {
      headers: authHeader(),
    });
    return value.data;
  },
};
export default UserService;
