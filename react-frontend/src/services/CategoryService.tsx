import { Category } from 'models/Category';
import { Thread } from 'models/Thread';

export function getAllCategories() {
  return fetch('/categories').then(res => res.json() as Promise<Category[]>);
}

export function getAllThreadsByCategoryId(id: number) {
  return fetch(`/categories/${id}/threads`).then(
    res => res.json() as Promise<Thread[]>,
  );
}
