import { Post } from './Post';
import { Thread } from './Thread';

export interface ThreadAndPostStats {
  thread: Thread;
  lastPost: Post;
  postsCount: number;
}
