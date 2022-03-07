import { Category } from 'models/Category';
import { Page } from 'models/Page';
import { ThreadAndPostStats } from 'models/ThreadAndPostStats';
import api from './Api';

const CategoryService = {
  async getAllCategories(): Promise<Category[]> {
    const value = await api.get<Category[]>('/categories');
    return value.data;
  },

  async getThreadsByCategoryId(
    categoryId: number,
    page: number,
  ): Promise<Page<ThreadAndPostStats>> {
    const value = await api.get<Page<ThreadAndPostStats>>(
      `/categories/${categoryId}/threads?page=${page}`,
    );
    return value.data;
  },

  async getPinnedThreadsByCategoryId(
    categoryId: number,
  ): Promise<ThreadAndPostStats[]> {
    const value = await api.get<ThreadAndPostStats[]>(
      `/categories/${categoryId}/pinned-threads`,
    );
    return value.data;
  },

  async getCategoryById(id: number): Promise<Category> {
    const value = await api.get<Category>(`/categories/${id}`);
    return value.data;
  },

  async updateCategory(category: Category): Promise<Category> {
    const value = await api.put<Category>(
      `/categories/${category.id}`,
      category,
      {
        // headers: authHeader(),
      },
    );

    return value.data;
  },

  async createCategory(category: Category): Promise<Category> {
    const value = await api.post<Category>(`/categories`, category, {
      // headers: authHeader(),
    });

    return value.data;
  },

  async deleteCategory(id: number): Promise<void> {
    await api.delete(`/categories/${id}`, {
      // headers: authHeader(),
    });
  },
};

export default CategoryService;
