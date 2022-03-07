import { Page } from 'models/Page';
import { Post } from 'models/Post';
import { Thread } from 'models/Thread';
import api from './Api';

const ThreadService = {
  async getAllThreads(): Promise<Thread[]> {
    const value = await api.get<Thread[]>('/threads');
    return value.data;
  },

  async getThreadById(id: number): Promise<Thread> {
    const value = await api.get<Thread>(`/threads/${id}`);
    return value.data;
  },

  async getPostsByThreadId(id: number, page: number): Promise<Page<Post>> {
    const value = await api.get<Page<Post>>(
      `/threads/${id}/posts?page=${page}`,
    );
    return value.data;
  },

  async updateThread(thread: Thread): Promise<Thread> {
    const value = await api.put<Thread>(`/threads/${thread.id}`, thread, {
      // headers: authHeader(),
    });

    return value.data;
  },

  async createThread(thread: Thread): Promise<Thread> {
    const value = await api.post<Thread>(`/threads`, thread, {
      // headers: authHeader(),
    });

    return value.data;
  },

  async deleteThread(id: number): Promise<void> {
    await api.delete(`/threads/${id}`, {
      // headers: authHeader(),
    });
  },
};

export default ThreadService;
