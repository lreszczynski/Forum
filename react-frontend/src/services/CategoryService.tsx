import { Category } from '../models/Category';

const get = (): Category[] => [
  { id: 1, name: 'name', description: 'description' },
  { id: 2, name: 'name2', description: 'description2' },
  { id: 3, name: 'name3', description: 'description3' },
  { id: 4, name: 'name4', description: 'description4' },
];

export default get;
