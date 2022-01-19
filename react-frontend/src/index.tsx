import './index.scss';
// import 'antd/dist/antd.min.css';

import React from 'react';
import ReactDOM from 'react-dom';
import { QueryClient, QueryClientProvider } from 'react-query';
import { ReactQueryDevtools } from 'react-query/devtools';
import { Provider } from 'react-redux';

import App from 'app/App';
import { store } from 'store/store';
import reportWebVitals from './reportWebVitals';

const queryClientConfig = {
  defaultOptions: {
    queries: {
      retry: 3,
      staleTime: 1000 * 30, // 30seconds
      cacheTime: 1000 * 30, // 30 seconds
    },
    mutations: {
      retry: 3,
    },
  },
};

const queryClient = new QueryClient(queryClientConfig);

ReactDOM.render(
  <QueryClientProvider client={queryClient}>
    <Provider store={store}>
      <App />
    </Provider>
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>,
  document.getElementById('root'),
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
