import './index.scss';
// import 'antd/dist/antd.min.css';
import 'moment/locale/en-gb';
import { library } from '@fortawesome/fontawesome-svg-core';
import {
  faEdit,
  faEye,
  faReply,
  faTrash,
} from '@fortawesome/free-solid-svg-icons';
import moment from 'moment';
import React from 'react';
import ReactDOM from 'react-dom';
import { QueryClient, QueryClientProvider } from 'react-query';
import { ReactQueryDevtools } from 'react-query/devtools';
import { Provider } from 'react-redux';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import App from 'app/App';
import CategoryDashboard from 'components/categories/CategoryDashboard';
import ListOfCategories from 'components/categories/ListOfCategories';
import NewCategory from 'components/categories/NewCategory';
import Login from 'components/login/Login';
import Register from 'components/login/Register';
import Main from 'components/main/Main';
import ListOfPosts from 'components/posts/ListOfPosts';
import PostSearchResults from 'components/posts/PostSearchResults';
import ListOfThreads from 'components/threads/ListOfThreads';
import NewThread from 'components/threads/NewThread';
import ThreadDashboard from 'components/threads/ThreadDashboard';
import { store } from 'store/store';
import reportWebVitals from './reportWebVitals';

const queryClientConfig = {
  defaultOptions: {
    queries: {
      retry: 3,
      staleTime: 1000 * 30, // 30 seconds
      cacheTime: 1000 * 30, // 30 seconds
    },
    mutations: {
      retry: 0,
    },
  },
};

moment.locale('en-gb');

library.add(faReply, faEye, faEdit, faTrash);

const queryClient = new QueryClient(queryClientConfig);

ReactDOM.render(
  <QueryClientProvider client={queryClient}>
    <Provider store={store}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<App />}>
            <Route path="/" element={<Main longText />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/short" element={<Main longText={false} />} />
            <Route path="/forum" element={<ListOfCategories />} />
            <Route path="/forum/:id/" element={<ListOfThreads />} />
            <Route path="/forum/:id/page/:id2" element={<ListOfThreads />} />
            <Route path="/forum/:id/threads/:id2" element={<ListOfPosts />} />
            <Route
              path="/forum/:id/threads/:id2/page/:id3"
              element={<ListOfPosts />}
            />
            <Route path="/categories/:id/" element={<CategoryDashboard />} />
            <Route path="/categories/new" element={<NewCategory />} />
            <Route path="/threads/:id/" element={<ThreadDashboard />} />
            <Route path="/categories/:id/newThread" element={<NewThread />} />
            <Route path="/posts/search" element={<PostSearchResults />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </Provider>
    <ReactQueryDevtools initialIsOpen={false} />
  </QueryClientProvider>,
  document.getElementById('root'),
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
