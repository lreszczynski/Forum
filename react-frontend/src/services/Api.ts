/* eslint-disable no-underscore-dangle */
import axios from 'axios';
import LocalStorage from './LocalStorage';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL,
});

api.interceptors.request.use(
  config => {
    const token = LocalStorage.getCurrentUserTokens();
    if (token) {
      // eslint-disable-next-line no-param-reassign
      config.headers!.Authorization = `Bearer ${token.accessToken}`;
    }
    return config;
  },
  error => Promise.reject(error),
);
api.interceptors.response.use(
  res => res,
  async err => {
    const originalConfig = err.config;
    if (originalConfig.url !== '/login' && err.response) {
      if (err.response.status === 401 && !originalConfig._retry) {
        originalConfig._retry = true;
        try {
          await axios
            .get('/token/refresh', {
              baseURL: process.env.REACT_APP_API_URL,
              headers: {
                Authorization: `Bearer ${LocalStorage.getCurrentUserTokens()
                  ?.refreshToken!}`,
              },
            })
            .then(response => {
              localStorage.setItem('tokens', JSON.stringify(response.data));
            });
          return api(originalConfig);
        } catch (_error) {
          return Promise.reject(_error);
        }
      }
    }
    return Promise.reject(err);
  },
);

export default api;
