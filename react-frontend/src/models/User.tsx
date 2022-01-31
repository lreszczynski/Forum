import { Role } from './Role';

export interface User {
  id: number;
  username: string;
  banned: boolean;
  role: Role;
}
