import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, notification, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import React from 'react';
import { useQuery } from 'react-query';
import { Link, useParams } from 'react-router-dom';
import CategoryService from 'services/CategoryService';
import SingleThread from './SingleThread';

export default function Threads() {
  const params = useParams();
  const categoryId = Number(params.id);

  const queryThreads = useQuery(`categories/${categoryId}/threads`, () =>
    CategoryService.getCategoryByIdWithThreads(categoryId),
  );

  const breadcrumbs = (
    <Breadcrumb>
      <BreadcrumbItem>
        <Link to="/">
          <HomeFilled />
        </Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <Link to="/forum/">Forum</Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <span>{queryThreads.data?.name}</span>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  if (queryThreads.isLoading) {
    return (
      <>
        {breadcrumbs}
        <div>Loading...</div>
      </>
    );
  }
  if (queryThreads.isError) {
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

  if (queryThreads.isSuccess) {
    return (
      <>
        {breadcrumbs}
        <Row gutter={[8, 8]}>
          {Array.from(queryThreads.data.threads).map(value => (
            <Col key={value.id} xs={24}>
              <SingleThread id={categoryId} thread={value} />
            </Col>
          ))}
        </Row>
      </>
    );
  }
  return null;
}
