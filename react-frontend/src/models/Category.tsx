import { Role } from './Role';

export interface Category {
  id: number;
  name: string;
  description: string;
  active: boolean;
  roles: Role[];
}
