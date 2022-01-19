import { User } from './User';

export interface Post {
  id: number;
  content: String;
  createDate: Date;
  user: User;
}
