import './SingleCategory.scss';

import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Category } from 'models/Category';

export interface ISingleCategoryProps {
  category: Category;
}

export default function SingleCategory({ category }: ISingleCategoryProps) {
  const categoryId = category.id;
  const navigate = useNavigate();
  const link = () => navigate(`/forum/${categoryId}`);

  return (
    <div
      className="category"
      role="link"
      tabIndex={0}
      onClick={link}
      onKeyPress={link}
    >
      <h2>{category.name}</h2>
      <span>{category.description}</span>
    </div>
  );
}
