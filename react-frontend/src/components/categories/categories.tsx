import React from 'react';

import './categories.scss';
import get from '../../services/CategoryService';

function Categories() {
  const categories = get();
  const categoriesParsed = categories.map(value => (
    <h2 key={value.id}>{value.name}</h2>
  ));
  return <div>{categoriesParsed}</div>;
}

export default Categories;
