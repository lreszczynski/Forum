import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { Button, Col, Divider, Form, Modal, Row, Space } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import moment from 'moment';
import React, { useEffect, useState } from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { Post } from 'models/Post';
import AuthService from 'services/AuthService';
import PostService from 'services/PostService';

import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';
import { breakpoints } from 'utils/screenWidth';

export interface ISinglePostProps {
  post: Post;
}

export default function SinglePost(props: ISinglePostProps) {
  const { post } = props;
  const [width, setWidth] = useState(window.innerWidth);
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [editingPost, setEditingPost] = useState(false);
  const [form] = Form.useForm();
  const queryClient = useQueryClient();

  const canUserEditPost = AuthService.canUserEditPost(post);
  const canUserDeletePost = AuthService.canUserDeletePost(post);

  const mutationUpdatePost = useMutation<Post, AxiosError, Post, any>(
    (editedPost: Post) => PostService.updatePost(editedPost),
  );

  const mutationDeletePost = useMutation<void, AxiosError, number, any>(
    (id: number) => PostService.deletePost(id),
  );

  const submit = async (values: any) => {
    const formValues = values;
    console.log(formValues);

    const editedPost: Post = post;
    editedPost.content = values.content;

    mutationUpdatePost.mutate(editedPost, {
      onSuccess: (_data, _variables) => {
        form.resetFields();
        setEditingPost(false);
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
  };

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
  switch (post.user.role.name) {
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
  const nick = (
    <Text
      style={{
        textDecoration: post.user.banned ? 'line-through' : 'e',
        color: colour !== '' ? colour : undefined,
      }}
    >
      {post.user.username}
    </Text>
  );

  const editForm = (
    <Form
      onFinish={submit}
      form={form}
      initialValues={{ content: post.content }}
    >
      <Title level={5}>Editing post</Title>
      <Form.Item name="content" rules={[{ min: 10, max: 10000 }]}>
        <TextArea />
      </Form.Item>

      <Space>
        <Button type="primary" htmlType="submit">
          Save
        </Button>
        <Button onClick={() => setEditingPost(!editingPost)}>Cancel</Button>
      </Space>
    </Form>
  );

  const showModal = () => {
    setIsModalVisible(true);
  };

  const handleOk = () => {
    mutationDeletePost.mutate(post.id, {
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

  const modal = (
    <Modal
      title="Confirm delete"
      visible={isModalVisible}
      onOk={handleOk}
      onCancel={handleCancel}
    >
      <Text>Do you want to delete post?</Text>
    </Modal>
  );

  const postContent = (
    <div className="postContent" style={{ minHeight: '100%' }}>
      <Row>
        <Col flex="auto">
          <Text type="secondary">{moment(post.createDate).calendar()}</Text>
        </Col>
        <Col>
          <Space>
            {canUserEditPost && (
              <FontAwesomeIcon
                className="highlight"
                icon="edit"
                onClick={() => {
                  setEditingPost(true);
                }}
              />
            )}
            {canUserDeletePost && (
              <Col>
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
              </Col>
            )}
          </Space>
        </Col>
      </Row>
      <Divider style={{ margin: '4px 0px' }} />
      {editingPost ? editForm : post.content}
    </div>
  );

  if (width > breakpoints.sm) {
    return (
      <div className="postContainer">
        <Row style={{ minHeight: 'inherit' }} wrap={false}>
          <Col span="100px">
            <div
              className="postHeader"
              style={{
                minHeight: '100%',
                width: '120px',
                borderRadius: '8px 0px 0px 8px',
              }}
            >
              <Row align="middle" gutter={8}>
                <Col span={24}>
                  <div className="avatar">
                    <Text>{post.user.username.at(0)?.toUpperCase()}</Text>
                  </div>
                </Col>
                <Col span={24} style={{ textAlign: 'center' }}>
                  {nick}
                  <br />
                  <Text type="secondary">{post.user.role.name}</Text>
                </Col>
              </Row>
            </div>
          </Col>
          <Col flex="auto">{postContent}</Col>
        </Row>
        {modal}
      </div>
    );
  }
  return (
    <div className="postContainer">
      <div className="postHeader" style={{ borderRadius: '8px 8px 0px 0px' }}>
        <Row style={{ height: 'inherit' }} align="middle" gutter={8}>
          <Col>
            <div className="avatar">
              <Text>{post.user.username.at(0)?.toUpperCase()}</Text>
            </div>
          </Col>
          <Col>
            {nick}
            <br />
            <Text type="secondary">{post.user.role.name}</Text>
          </Col>
        </Row>
      </div>
      {postContent}
      {modal}
    </div>
  );
}
