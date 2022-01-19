import React, { useEffect } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';
import Posts from 'components/posts/Posts';
import Threads from 'components/threads/Threads';

import Categories from '../components/categories/Categories';
import Main from '../components/main/Main';

function RouterOutlet({ stateChanger }: any) {
  const location = useLocation();

  useEffect(() => {
    console.log('Router Outlet: ', location.pathname);
    stateChanger(location.pathname);
    // const dispatch: AppDispatch = useDispatch();
  }, [location]);

  return (
    <Routes>
      <Route path="/" element={<Main />} />
      <Route path="/short" element={<Main longText={false} />} />
      <Route path="/forum" element={<Categories />} />
      <Route path="/forum/:id/" element={<Threads />} />
      <Route path="/forum/:id/threads/:id2" element={<Posts />} />
    </Routes>
  );
}

export default RouterOutlet;
