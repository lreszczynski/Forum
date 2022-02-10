import { HomeFilled } from '@ant-design/icons';
import { Breadcrumb, Button, Form, Input, Space } from 'antd';
import BreadcrumbItem from 'antd/lib/breadcrumb/BreadcrumbItem';
import TextArea from 'antd/lib/input/TextArea';
import Text from 'antd/lib/typography/Text';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Thread } from 'models/Thread';
import CategoryService from 'services/CategoryService';
import ThreadService from 'services/ThreadService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface INewThreadProps {}

export default function NewThread(_props: INewThreadProps) {
  const params = useParams();
  const categoryId = Number(params.id);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const queryCategory = useQuery(`categories/${categoryId}`, () =>
    CategoryService.getCategoryById(categoryId),
  );
  const mutation = useMutation<Thread, AxiosError, Thread, any>(
    (newThread: Thread) => ThreadService.createThread(newThread),
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
        <Link to={`/forum/${categoryId}`}>{queryCategory.data?.name}</Link>
      </BreadcrumbItem>
      <BreadcrumbItem>
        <Text>New thread</Text>
      </BreadcrumbItem>
    </Breadcrumb>
  );

  const submit = async (values: any) => {
    const formValues = values;
    formValues.categoryId = queryCategory.data?.id;
    console.log(formValues);

    const newThread: Thread = values;

    mutation.mutate(newThread, {
      onSuccess: (_data, _variables) => {
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
        navigate(-1);
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
  };

  const leftSpan = 3;

  if (queryCategory.isSuccess) {
    return (
      <>
        {breadcrumbs}
        <Title level={2} style={{ textAlign: 'center' }}>
          New thread
        </Title>
        <Form
          labelCol={{ span: leftSpan }}
          wrapperCol={{ span: 24 - 2 * leftSpan }}
          onFinish={submit}
        >
          <Form.Item label="Title" name="title" rules={[{ max: 80 }]}>
            <Input />
          </Form.Item>
          <Form.Item
            label="Content"
            name="content"
            rules={[{ min: 10, max: 10000 }]}
          >
            <TextArea rows={10} />
          </Form.Item>

          <Form.Item wrapperCol={{ sm: { offset: leftSpan } }}>
            <Space>
              <Button type="primary" htmlType="submit">
                Save
              </Button>
              <Button
                htmlType="button"
                onClick={() => {
                  navigate(-1);
                }}
              >
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </>
    );
  }

  return null;
}
