import { Post } from 'models/Post';

const PostService = {
  getAllPosts() {
    return fetch('/posts').then(res => res.json() as Promise<Post[]>);
  },
};

export default PostService;
