import axios from 'axios';
import { Category } from 'models/Category';
import { Role } from 'models/Role';
import authHeader from './AuthHeader';

const RoleService = {
  async getAllRoles(): Promise<Role[]> {
    const value = await axios.get<Role[]>('/roles', {
      headers: authHeader(),
    });
    return value.data;
  },

  async getRoleById(id: number): Promise<Role> {
    const value = await axios.get<Category>(`/roles/${id}`);
    return value.data;
  },
};

export default RoleService;
