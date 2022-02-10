import { Button, Form, Space } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { CreatePost, Post } from 'models/Post';
import { Thread } from 'models/Thread';
import PostService from 'services/PostService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface INewPostProps {
  thread: Thread;
}

export default function NewPost(props: INewPostProps) {
  const { thread } = props;
  const queryClient = useQueryClient();
  const [hidden, setHidden] = React.useState(true);
  const [form] = Form.useForm();

  const mutation = useMutation<Post, AxiosError, CreatePost, any>(
    (newPost: CreatePost) => PostService.createPost(newPost),
  );

  const submit = async (values: any) => {
    const formValues = values;
    formValues.threadId = thread.id;
    console.log(formValues);

    const newPost: CreatePost = values;

    mutation.mutate(newPost, {
      onSuccess: (_data, _variables) => {
        form.resetFields();
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

  if (hidden) {
    return (
      <Button type="primary" onClick={() => setHidden(!hidden)}>
        Add a new reply
      </Button>
    );
  }
  return (
    <Form onFinish={submit} form={form}>
      <Title level={5}>Reply</Title>
      <Form.Item name="content" rules={[{ min: 10, max: 10000 }]}>
        <TextArea rows={6} />
      </Form.Item>

      <Space>
        <Button type="primary" htmlType="submit">
          Reply
        </Button>
        <Button onClick={() => setHidden(!hidden)}>Cancel</Button>
      </Space>
    </Form>
  );
}
