import { Col, Pagination, Row } from 'antd';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import React from 'react';
import { useQuery } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { Post } from 'models/Post';
import PostService from 'services/PostService';

export interface IPostSearchResultsProps {}

export default function PostSearchResults(_props: IPostSearchResultsProps) {
  const navigate = useNavigate();
  const search = new URLSearchParams(window.location.search);
  const text = String(search.get('text'));
  const pageId = Number(search.get('page') || 1);

  const querySearch = useQuery(
    `posts/search?text=${text}&page=${pageId - 1}`,
    () => PostService.searchForPostContent(text, pageId - 1),
  );

  function onChange(pageNumber: number) {
    navigate(`/posts/search?text=${text}&page=${pageNumber}`);
    window.scrollTo(0, 0);
  }

  function trimmedText(post: Post, searchWord: string) {
    const range = 200;
    let start = '';
    const index = post.content.toLowerCase().indexOf(searchWord.toLowerCase());
    const middle = post.content.slice(index, index + searchWord.length);
    let positionStart = index - range;
    if (positionStart < 0) positionStart = 0;
    else start += '...';
    start += post.content.slice(positionStart, index);
    const positionEnd = searchWord.length + positionStart + 2 * range;
    let end = post.content.slice(index + searchWord.length, positionEnd);
    if (positionEnd < post.content.length) end += '...';
    return {
      post,
      start,
      middle,
      end,
    };
  }

  function goTo(id: number) {
    navigate(`/forum/${id}/threads/${id}`);
  }

  if (querySearch.isSuccess) {
    const pagination = (
      <Pagination
        pageSize={20}
        showQuickJumper
        showSizeChanger={false}
        current={pageId}
        total={querySearch.data.totalElements}
        onChange={(pageNumber, _pageSize) => onChange(pageNumber)}
      />
    );

    const res = Array.from(querySearch.data.content).map(value =>
      trimmedText(value, text),
    );

    const results = Array.from(res).map(value => (
      <Col key={value.post.id} span={24}>
        <div
          className="category"
          role="link"
          tabIndex={0}
          onClick={() => goTo(value.post.thread.id)}
          onKeyPress={() => goTo(value.post.thread.id)}
        >
          <Title level={5}>Title: {value.post.thread.title}</Title>
          <Text type="secondary">{value.post.user.username}: </Text>
          <Text>{value.start}</Text>
          <Text style={{ color: 'gold' }}>{value.middle}</Text>
          <Text>{value.end}</Text>
        </div>
      </Col>
    ));

    return (
      <>
        {pagination}
        <div className="">
          <Title level={2} style={{ marginTop: '8px' }}>
            Search results for {text}
          </Title>
        </div>
        <Row gutter={[16, 16]}>{Array.from(results).map(value => value)}</Row>
        <div style={{ marginTop: '16px' }}>{pagination}</div>
      </>
    );
  }

  return <div />;
}
