import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, notification, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import React from 'react';
import { useQuery } from 'react-query';
import { Link, useParams } from 'react-router-dom';
import { Thread } from 'models/Thread';
import { getAllThreadsByCategoryId } from 'services/CategoryService';
import SingleThread from './singleThread';

export default function Threads() {
  const params = useParams();
  const categoryId = Number(params.id);
  console.log('>>>', categoryId);

  const query = useQuery(`categories/${categoryId}/threads`, () =>
    getAllThreadsByCategoryId(categoryId),
  );
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

  return (
    <>
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
          <span>nazwa</span>
        </BreadcrumbItem>
      </Breadcrumb>
      <Row gutter={[8, 8]}>
        {(query.data as Thread[]).map(value => (
          <Col key={value.id} xs={24} md={12}>
            <SingleThread id={categoryId} thread={value} />
          </Col>
        ))}
      </Row>
    </>
  );
}
