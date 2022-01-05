import React from 'react';
import { Route, Routes } from 'react-router-dom';

import Categories from '../categories/categories';
import Main from '../main/main';

function RouterOutlet() {
  return (
    <Routes>
      <Route path="/" element={<Main longText />} />
      <Route path="/short" element={<Main longText={false} />} />
      <Route path="/categories" element={<Categories />} />
    </Routes>
  );
}

export default RouterOutlet;
