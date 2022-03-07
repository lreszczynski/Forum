import { Page } from 'models/Page';
import { CreatePost, Post } from 'models/Post';
import api from './Api';

const PostService = {
  async searchForPostContent(
    content: string,
    page: number,
  ): Promise<Page<Post>> {
    const value = await api.get<Page<Post>>('/posts/search', {
      params: { text: content, page },
    });

    return value.data;
  },

  async updatePost(post: Post): Promise<Post> {
    const value = await api.put<Post>(`/posts/${post.id}`, post, {
      // headers: authHeader(),
    });

    return value.data;
  },

  async deletePost(id: number): Promise<void> {
    await api.delete(`/posts/${id}`, {
      // headers: authHeader(),
    });
  },

  async createPost(post: CreatePost): Promise<Post> {
    const value = await api.post<Post>(`/posts`, post, {
      // headers: authHeader(),
    });

    return value.data;
  },
};

export default PostService;
