import { Role } from './Role';

export interface User {
  id: number;
  username: String;
  banned: boolean;
  role: Role;
}
