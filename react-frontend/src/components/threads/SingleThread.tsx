import './SingleThread.scss';

import { ClockCircleFilled, MessageFilled } from '@ant-design/icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Col, Row, Space } from 'antd';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import moment from 'moment';
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Category } from 'models/Category';
import { ThreadAndPostStats } from 'models/ThreadAndPostStats';
import { breakpoints } from 'utils/screenWidth';

export interface ISingleThreadProps {
  category: Category;
  threadAndLastPost: ThreadAndPostStats;
}

export default function SingleThread(params: ISingleThreadProps) {
  const { category, threadAndLastPost } = params;
  const { thread, lastPost, postsCount } = threadAndLastPost;
  const [width, setWidth] = useState(window.innerWidth);
  // const threadId = thread.id;
  const navigate = useNavigate();
  const link = () => navigate(`/forum/${category.id}/threads/${thread.id}`);

  useEffect(() => {
    function handleResize() {
      setWidth(window.innerWidth);
    }

    window.addEventListener('resize', handleResize);
    return () => {
      window.removeEventListener('resize', handleResize);
    };
  }, []);

  let colour = '';
  switch (thread.user.role.name) {
    case 'Moderator': {
      colour = 'green';
      break;
    }
    case 'Admin': {
      colour = 'red';
      break;
    }
    default: {
      break;
    }
  }

  const replies = postsCount - 1;

  if (width > breakpoints.sm) {
    return (
      <Row className="thread" wrap={false}>
        <Col flex="140px" style={{ cursor: 'pointer', padding: '6px' }}>
          <div className="userInfoContainer">
            <div className="avatar">
              <p>{thread.user.username.at(0)?.toUpperCase()}</p>
            </div>
            {colour !== '' ? (
              <Text ellipsis style={{ color: colour }}>
                {thread.user.username}
              </Text>
            ) : (
              <Text ellipsis>{thread.user.username}</Text>
            )}
          </div>
        </Col>
        <Col flex="10px">
          <div className="verticalLine" />
        </Col>
        <Col
          flex="auto"
          onClick={link}
          style={{ cursor: 'pointer', padding: '6px' }}
        >
          <Title level={4} ellipsis={{ rows: 2 }}>
            {thread.title}
          </Title>
          <Row>
            <Col>
              <Space>
                <Text type="secondary">
                  {moment(thread.createDate).format('L')}
                </Text>
                <Text>
                  <MessageFilled /> {replies}{' '}
                  {replies > 0 && (
                    <>
                      <FontAwesomeIcon icon="reply" /> {lastPost.user.username}
                    </>
                  )}
                </Text>
              </Space>
            </Col>
            <Col flex="auto" style={{ textAlign: 'right' }}>
              <Text>
                <ClockCircleFilled /> {moment(lastPost.createDate).fromNow()}
              </Text>
            </Col>
          </Row>
        </Col>
      </Row>
    );
  }
  return (
    <Row className="thread" wrap={false}>
      <Col
        flex="auto"
        onClick={link}
        style={{ cursor: 'pointer', padding: '6px' }}
      >
        <Title level={5} ellipsis={{ rows: 2 }}>
          {thread.title}
        </Title>
        <Row align="middle" gutter={8}>
          <Col>
            <Text type="secondary">
              <div className="smallAvatar">
                <Text>{thread.user.username.at(0)?.toUpperCase()}</Text>
              </div>
            </Text>
          </Col>
          <Col>
            <Text>{thread.user.username}</Text>
          </Col>
          <Col>
            <Text type="secondary">
              {moment(thread.createDate).format('L')}
            </Text>
          </Col>
        </Row>
        <Row align="middle" gutter={8}>
          <Col>
            <MessageFilled /> {replies}{' '}
            {replies > 0 && (
              <>
                <FontAwesomeIcon icon="reply" /> {lastPost.user.username}
              </>
            )}
          </Col>
          <Col flex="auto" style={{ textAlign: 'right' }}>
            <Text>
              <ClockCircleFilled /> {moment(lastPost.createDate).fromNow()}
            </Text>
          </Col>
        </Row>
      </Col>
    </Row>
  );
}
