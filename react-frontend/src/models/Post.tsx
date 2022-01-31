import { User } from './User';

export interface Post {
  id: number;
  content: string;
  createDate: Date;
  user: User;
}
