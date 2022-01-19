import { Thread } from 'models/Thread';
import { ThreadWithPosts } from 'models/ThreadWithPosts';

const ThreadService = {
  getAllThreads() {
    return fetch('/threads').then(res => res.json() as Promise<Thread[]>);
  },

  getThreadWithPosts(id: number) {
    return fetch(`/threads/${id}/posts`).then(
      res => res.json() as Promise<ThreadWithPosts>,
    );
  },
};

export default ThreadService;
