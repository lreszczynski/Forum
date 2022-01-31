import { AxiosResponse } from 'axios';

class FetchError extends Error {
  constructor(public res: AxiosResponse, message?: string) {
    super(message);
  }
}
export default FetchError;
