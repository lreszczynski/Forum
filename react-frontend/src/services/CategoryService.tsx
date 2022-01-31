import axios from 'axios';
import { Category } from 'models/Category';
import { Page } from 'models/Page';
import { ThreadAndPostStats } from 'models/ThreadAndPostStats';
import authHeader from './AuthHeader';

const CategoryService = {
  async getAllCategories(): Promise<Category[]> {
    const value = await axios.get<Category[]>('/categories');
    return value.data;
  },

  async getThreadsByCategoryId(
    categoryId: number,
    page: number,
  ): Promise<Page<ThreadAndPostStats>> {
    const value = await axios.get<Page<ThreadAndPostStats>>(
      `/categories/${categoryId}/threads?page=${page}`,
    );
    return value.data;
  },

  async getPinnedThreadsByCategoryId(
    categoryId: number,
  ): Promise<ThreadAndPostStats[]> {
    const value = await axios.get<ThreadAndPostStats[]>(
      `/categories/${categoryId}/pinned-threads`,
    );
    return value.data;
  },

  async getCategoryById(id: number): Promise<Category> {
    const value = await axios.get<Category>(`/categories/${id}`);
    return value.data;
  },

  async updateCategory(category: Category): Promise<Category> {
    const value = await axios.put<Category>(
      `/categories/${category.id}`,
      category,
      {
        headers: authHeader(),
      },
    );

    return value.data;
  },
};

export default CategoryService;
