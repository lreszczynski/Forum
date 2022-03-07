import './ListOfPosts.scss';

import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, notification, Pagination, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import Title from 'antd/lib/typography/Title';
import React from 'react';
import { useQuery } from 'react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import AuthService from 'services/AuthService';
import ThreadService from 'services/ThreadService';
import NewPost from './NewPost';
import SinglePost from './SinglePost';

export interface IListOfPostsProps {}

export default function ListOfPosts(_props: IListOfPostsProps) {
  const params = useParams();
  const categoryId = Number(params.id);
  const threadId = Number(params.id2);
  const pageId = Number(params.id3) || 1;
  const navigate = useNavigate();

  const queryThread = useQuery(`threads/${threadId}`, () =>
    ThreadService.getThreadById(threadId),
  );

  const queryPosts = useQuery(
    `threads/${threadId}/posts?page=${pageId - 1}`,
    () => ThreadService.getPostsByThreadId(threadId, pageId - 1),
  );

  const queries = [queryThread, queryPosts];

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
        <Link to={`/forum/${queryThread.data?.category.id}`}>
          <span>{queryThread.data?.category.name}</span>
        </Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <span>{queryThread.data?.title}</span>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  if (queries.some(query => query.isLoading)) {
    return (
      <>
        {breadcrumbs}
        <div>Loading...</div>
      </>
    );
  }

  if (queries.some(query => query.isError)) {
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

  function onChange(pageNumber: number) {
    // setPage(pageNumber);
    navigate(`/forum/${categoryId}/threads/${threadId}/page/${pageNumber}`);
    window.scrollTo(0, 0);
  }

  if (queryThread.isSuccess && queryPosts.isSuccess) {
    const canUserCreatePost = AuthService.canUserCreatePost(queryThread.data);
    const pagination = (
      <Pagination
        pageSize={20}
        showQuickJumper
        showSizeChanger={false}
        current={pageId}
        total={queryPosts.data.totalElements}
        onChange={(pageNumber, _pageSize) => onChange(pageNumber)}
      />
    );

    return (
      <>
        {breadcrumbs}
        {pagination}
        <div className="title">
          <Title level={2} style={{ marginTop: '8px' }}>
            {queryThread.data.title}
          </Title>
        </div>
        <Row gutter={[16, 16]}>
          {Array.from(queryPosts.data.content).map(value => (
            <Col key={value.id} span={24}>
              <SinglePost post={value} />
            </Col>
          ))}
        </Row>
        {canUserCreatePost && (
          <div style={{ marginTop: '16px', marginBottom: '16px' }}>
            <NewPost thread={queryThread.data} />
          </div>
        )}
        <div style={{ marginTop: '16px' }}>{pagination}</div>
      </>
    );
  }
  return null;
}
