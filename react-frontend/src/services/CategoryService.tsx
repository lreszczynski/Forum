import { Category } from 'models/Category';
import { CategoryWithThreads } from 'models/CategoryWithThreads';

const CategoryService = {
  getAllCategories() {
    return fetch('/categories').then(res => res.json() as Promise<Category[]>);
  },

  getCategoryByIdWithThreads(id: number) {
    return fetch(`/categories/${id}/threads`).then(
      res => res.json() as Promise<CategoryWithThreads>,
    );
  },

  getCategoryById(id: number) {
    return fetch(`/categories/${id}`).then(
      res => res.json() as Promise<Category>,
    );
  },
};

export default CategoryService;
