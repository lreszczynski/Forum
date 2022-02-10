import { Col, Pagination, Row } from 'antd';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import React from 'react';
import { useQuery } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { Thread } from 'models/Thread';
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

  function onChange(_pageNumber: number) {
    // setPage(pageNumber);
    navigate(`posts/search?text=${text}&page=${pageId - 1}`);
    window.scrollTo(0, 0);
  }

  function trimmedText(
    id: number,
    thread: Thread,
    content: string,
    searchWord: string,
  ): any {
    const range = 200;
    let start = '';
    const index = content.indexOf(searchWord);
    let positionStart = index - range;
    if (positionStart < 0) positionStart = 0;
    else start += '...';
    start += content.slice(positionStart, index);
    const positionEnd = searchWord.length + positionStart + 2 * range;
    let end = content.slice(index + text.length, positionEnd);
    if (positionEnd < content.length) end += '...';
    return {
      id,
      thread,
      start,
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
      trimmedText(value.id, value.thread, value.content, text),
    );

    const results = Array.from(res).map(value => (
      <Col key={value.id} span={24}>
        <div
          className="category"
          role="link"
          tabIndex={0}
          onClick={() => goTo(value.thread.id)}
          onKeyPress={() => goTo(value.thread.id)}
        >
          <Title level={5}>Title: {value.thread.title}</Title>
          <Text>{value.start}</Text>
          <Text style={{ color: 'gold' }}>{text}</Text>
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
