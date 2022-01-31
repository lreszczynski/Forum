import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Col, Collapse, notification, Pagination, Row } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import CollapsePanel from 'antd/lib/collapse/CollapsePanel';
import React from 'react';
import { useQuery } from 'react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import CategoryService from 'services/CategoryService';
import SingleThread from './SingleThread';

export default function ListOfThreads() {
  const params = useParams();
  const categoryId = Number(params.id);
  const pageId = Number(params.id2) || 1;
  const navigate = useNavigate();

  const queryCategory = useQuery(`categories/${categoryId}`, () =>
    CategoryService.getCategoryById(categoryId),
  );

  const queryThreads = useQuery(
    `categories/${categoryId}/threads?page=${pageId - 1}`,
    () => CategoryService.getThreadsByCategoryId(categoryId, pageId - 1),
  );

  const queryPinnedThreads = useQuery(`/${categoryId}/pinned-threads`, () =>
    CategoryService.getPinnedThreadsByCategoryId(categoryId),
  );

  const queries = [queryCategory, queryThreads, queryPinnedThreads];

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
        <span>{queryCategory.data?.name}</span>
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
    navigate(`/forum/${categoryId}/page/${pageNumber}`);
    window.scrollTo(0, 0);
  }

  if (
    queryThreads.isSuccess &&
    queryCategory.isSuccess &&
    queryPinnedThreads.isSuccess
  ) {
    const pinnedThreads = Array.from(queryPinnedThreads.data).map(value => (
      <Col key={value.thread.id} xs={24}>
        <SingleThread category={queryCategory.data} threadAndLastPost={value} />
      </Col>
    ));
    const threads = Array.from(queryThreads.data.content).map(value => (
      <Col key={value.thread.id} xs={24}>
        <SingleThread category={queryCategory.data} threadAndLastPost={value} />
      </Col>
    ));
    const pagination = (
      <Pagination
        pageSize={20}
        showQuickJumper
        showSizeChanger={false}
        current={pageId}
        total={queryThreads.data.totalElements}
        onChange={(pageNumber, _pageSize) => onChange(pageNumber)}
      />
    );

    return (
      <>
        {breadcrumbs}
        <div style={{ marginBottom: '16px' }}>{pagination}</div>
        {pageId === 1 && queryPinnedThreads.data.length > 0 && (
          <div style={{ marginBottom: '8px' }}>
            <Collapse defaultActiveKey={['1']}>
              <CollapsePanel header="Pinned threads" key="1">
                <Row gutter={[8, 8]}>{pinnedThreads.map(value => value)}</Row>
              </CollapsePanel>
            </Collapse>
          </div>
        )}
        <Row gutter={[8, 8]}>{threads.map(value => value)}</Row>
        <div style={{ marginTop: '16px' }}>{pagination}</div>
      </>
    );
  }
  return null;
}
