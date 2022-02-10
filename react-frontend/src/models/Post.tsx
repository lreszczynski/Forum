import { Thread } from './Thread';
import { User } from './User';

export interface Post {
  id: number;
  content: string;
  createDate: Date;
  user: User;
  thread: Thread;
}

export interface CreatePost {
  content: string;
  user: User;
}
