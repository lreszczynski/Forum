import axios from 'axios';
import { Page } from 'models/Page';
import { Post } from 'models/Post';
import { Thread } from 'models/Thread';

const ThreadService = {
  async getAllThreads(): Promise<Thread[]> {
    const value = await axios.get<Thread[]>('/threads');
    return value.data;
  },

  async getThreadById(id: number): Promise<Thread> {
    const value = await axios.get<Thread>(`/threads/${id}`);
    return value.data;
  },

  async getPostsByThreadId(id: number, page: number): Promise<Page<Post>> {
    const value = await axios.get<Page<Post>>(
      `/threads/${id}/posts?page=${page}`,
    );
    return value.data;
  },
};

export default ThreadService;
