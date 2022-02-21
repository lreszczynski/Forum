import './SingleThread.scss';

import { ClockCircleFilled, MessageFilled } from '@ant-design/icons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Col, Modal, Row, Space } from 'antd';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import moment from 'moment';
import React, { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { Link, useNavigate } from 'react-router-dom';
import { Category } from 'models/Category';
import { ThreadAndPostStats } from 'models/ThreadAndPostStats';
import AuthService from 'services/AuthService';
import ThreadService from 'services/ThreadService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';
import { breakpoints } from 'utils/screenWidth';

export interface ISingleThreadProps {
  category: Category;
  threadAndLastPost: ThreadAndPostStats;
}

export default function SingleThread(params: ISingleThreadProps) {
  const { category, threadAndLastPost } = params;
  const { thread, lastPost, postsCount } = threadAndLastPost;
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [width, setWidth] = useState(window.innerWidth);
  // const threadId = thread.id;
  const navigate = useNavigate();
  const link = () => navigate(`/forum/${category.id}/threads/${thread.id}`);
  const canUserEditThread = AuthService.canUserEditThread(thread);
  const queryClient = useQueryClient();

  const mutation = useMutation<void, AxiosError, number, any>((id: number) =>
    ThreadService.deleteThread(id),
  );

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

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = () => {
    mutation.mutate(thread.id, {
      onSuccess: (_data, _variables) => {
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
    setIsModalVisible(false);
  };

  const handleCancel = () => {
    setIsModalVisible(false);
  };

  const editControls = (
    <Space>
      <Link to={`/threads/${thread.id}`}>
        <FontAwesomeIcon icon="edit" />
      </Link>
      <div
        style={{ cursor: 'pointer' }}
        role="none"
        onClick={() => showModal()}
        onKeyDown={() => showModal()}
      >
        <Text type="danger">
          <FontAwesomeIcon icon="trash" />
        </Text>
      </div>
    </Space>
  );

  const modal = (
    <Modal
      title="Confirm delete"
      visible={isModalVisible}
      onOk={handleOk}
      onCancel={handleCancel}
    >
      <Text>Do you want to delete &apos;{thread.title}&apos;?</Text>
    </Modal>
  );

  if (width > breakpoints.sm) {
    return (
      <Row className="thread" wrap={false}>
        <Col flex="140px">
          <div
            className="userInfoContainer"
            style={{
              cursor: 'pointer',
              padding: '6px',
              backgroundColor: 'rgb(13, 47, 99)',
              borderRadius: '8px 0 0 8px',
              minHeight: '100%',
            }}
          >
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
        <Col flex="auto" style={{ padding: '6px' }}>
          <Row wrap={false}>
            <Col>
              <Title
                level={4}
                ellipsis={{ rows: 2 }}
                onClick={link}
                style={{ cursor: 'pointer' }}
              >
                {thread.title}
              </Title>
            </Col>
            <Col flex="auto" style={{ textAlign: 'right' }}>
              {canUserEditThread && editControls}
            </Col>
          </Row>
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
        {modal}
      </Row>
    );
  }
  return (
    <Row className="thread" wrap={false}>
      <Col flex="auto" style={{ padding: '6px' }}>
        <Row>
          <Col flex="auto">
            <Title
              level={5}
              ellipsis={{ rows: 2 }}
              onClick={link}
              style={{ cursor: 'pointer' }}
            >
              {thread.title}
            </Title>
          </Col>
          <Col>{canUserEditThread && editControls}</Col>
        </Row>
        <Row align="middle" gutter={8}>
          <Col>
            <Text type="secondary">
              <div className="smallAvatar">
                <Text>{thread.user.username.at(0)?.toUpperCase()}</Text>
              </div>
            </Text>
          </Col>
          <Col>
            <Text ellipsis style={{ color: colour }}>
              {thread.user.username}
            </Text>
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
      {modal}
    </Row>
  );
}
