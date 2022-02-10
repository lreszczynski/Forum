import axios from 'axios';
import { Page } from 'models/Page';
import { Post } from 'models/Post';
import { Thread } from 'models/Thread';
import authHeader from './AuthHeader';

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

  async updateThread(thread: Thread): Promise<Thread> {
    const value = await axios.put<Thread>(`/threads/${thread.id}`, thread, {
      headers: authHeader(),
    });

    return value.data;
  },

  async createThread(thread: Thread): Promise<Thread> {
    const value = await axios.post<Thread>(`/threads`, thread, {
      headers: authHeader(),
    });

    return value.data;
  },

  async deleteThread(id: number): Promise<void> {
    await axios.delete(`/threads/${id}`, {
      headers: authHeader(),
    });
  },
};

export default ThreadService;
