import { Thread } from './Thread';

export interface CategoryWithThreads {
  id: number;
  name: String;
  description: String;
  active: boolean;
  threads: Set<Thread>;
}
