import { Category } from './Category';
import { User } from './User';

export interface Thread {
  id: number;
  title: string;
  createDate: Date;
  active: boolean;
  pinned: boolean;
  category: Category;
  user: User;
}
