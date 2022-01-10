import React, { useEffect } from 'react';
import { Route, Routes, useLocation } from 'react-router-dom';

import Threads from 'components/threads/threads';
import Categories from '../categories/categories';
import Main from '../main/main';

function RouterOutlet({ stateChanger }: any) {
  const location = useLocation();

  useEffect(() => {
    console.log('Router Outlet: ', location.pathname);
    stateChanger(location.pathname);
    // const dispatch: AppDispatch = useDispatch();
  }, [location]);

  return (
    <Routes>
      <Route path="/" element={<Main longText />} />
      <Route path="/short" element={<Main longText={false} />} />
      <Route path="/forum" element={<Categories />} />
      <Route path="/forum/:id/" element={<Threads />} />
    </Routes>
  );
}

export default RouterOutlet;
