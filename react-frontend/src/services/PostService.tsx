import axios from 'axios';
import { Post } from 'models/Post';

const PostService = {
  async getAllPosts(): Promise<Post[]> {
    const value = await axios.get<Post[]>('/posts');
    return value.data;
  },
};

export default PostService;
