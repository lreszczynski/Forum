import './categories.scss';

import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, notification, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import React, { Fragment } from 'react';
import { useQuery } from 'react-query';
import { Link } from 'react-router-dom';

import { Category } from 'models/Category';

import { getAllCategories } from 'services/CategoryService';
import SingleCategory from './singleCategory';

function Categories() {
  const query = useQuery('categories', () => getAllCategories());
  if (query.isLoading) {
    return <div>Loading...</div>;
  }
  if (query.isError) {
    notification.error({
      duration: 0,
      message: 'Connection problem',
      description: 'Could not connect to the server.',
      onClick: () => {
        console.log('Notification Clicked!');
      },
    });
    return <div>Error!</div>;
  }

  // useEffect(() => {}, []);

  return (
    <>
      <Breadcrumb>
        <BreadcrumbItem>
          <Link to="/">
            <HomeFilled />
          </Link>
        </BreadcrumbItem>
        <BreadcrumbItem>
          <span>Forum</span>
        </BreadcrumbItem>
      </Breadcrumb>
      <Row gutter={[8, 8]}>
        {(query.data as Category[]).map(value => (
          <Col key={value.id} xs={24} md={12}>
            <SingleCategory category={value} />
          </Col>
        ))}
      </Row>
    </>
  );
}

export default Categories;
