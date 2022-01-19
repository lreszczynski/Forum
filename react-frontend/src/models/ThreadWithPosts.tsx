import { Category } from './Category';
import { Post } from './Post';
import { User } from './User';

export interface ThreadWithPosts {
  id: number;
  title: String;
  active: boolean;
  category: Category;
  user: User;
  posts: Set<Post>;
}
