import './Posts.scss';

import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, notification, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import React, { useEffect, useState } from 'react';
import { useQuery } from 'react-query';
import { Link, useParams } from 'react-router-dom';
import ThreadService from 'services/ThreadService';
import { breakpoints } from 'utils/screenWidth';
import SinglePost from './SinglePost';

export interface IPostsProps {}

export default function Posts(_props: IPostsProps) {
  const params = useParams();
  const threadId = Number(params.id2);
  const [width, setWidth] = useState(window.innerWidth);

  useEffect(() => {
    function handleResize() {
      setWidth(window.innerWidth);
    }

    window.addEventListener('resize', handleResize);
  });

  const queryThreads = useQuery(`threads/${threadId}/posts`, () =>
    ThreadService.getThreadWithPosts(threadId),
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
        <Link to={`/forum/${queryThreads.data?.category.id}`}>
          <span>{queryThreads.data?.category.name}</span>
        </Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <span>{queryThreads.data?.title}</span>
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
    if (width > breakpoints.sm) {
      return <div>todo</div>;
    }

    return (
      <>
        {breadcrumbs}
        <Row gutter={[8, 8]}>
          {Array.from(queryThreads.data.posts).map(value => (
            <Col key={value.id} span={24}>
              <SinglePost post={value} />
            </Col>
          ))}
        </Row>
      </>
    );
  }
  return null;
}
