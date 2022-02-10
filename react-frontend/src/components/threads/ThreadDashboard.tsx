import {
  Button,
  Checkbox,
  DatePicker,
  Form,
  Input,
  InputNumber,
  Select,
  Space,
} from 'antd';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import moment from 'moment';
import * as React from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { Thread } from 'models/Thread';
import CategoryService from 'services/CategoryService';
import ThreadService from 'services/ThreadService';
import {
  notificationError,
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface IThreadDashboardProps {}

export default function ThreadDashboard(_props: IThreadDashboardProps) {
  const params = useParams();
  const id = Number(params.id);
  const queryClient = useQueryClient();
  const query = useQuery(`threads/${id}`, () =>
    ThreadService.getThreadById(id),
  );
  const queryCategories = useQuery(`categories/`, () =>
    CategoryService.getAllCategories(),
  );
  const mutation = useMutation<Thread, AxiosError, Thread, any>(
    (newThread: Thread) => ThreadService.updateThread(newThread),
  );
  const queries = [query, queryCategories];

  const leftSpan = 4;
  const navigate = useNavigate();

  const update = async (values: any) => {
    const formValues = values;
    formValues.category = queryCategories.data?.find(
      qc => qc.id === Number(formValues.categoryId),
    );
    delete formValues.categoryId;

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

  if (queries.some(q => q.isError)) {
    notificationError();
  }

  if (query.isSuccess && queryCategories.isSuccess) {
    console.log(query.data);
    const categoryId = String(query.data.category.id);

    return (
      <>
        <Title level={2} style={{ textAlign: 'center' }}>
          Thread
        </Title>
        <Form
          name="basic"
          initialValues={{
            id: query.data.id,
            title: query.data.title,
            createDate: moment(query.data.createDate),
            active: query.data.active,
            pinned: query.data.pinned,
            categoryId,
          }}
          onFinish={update}
          labelCol={{ span: leftSpan }}
          wrapperCol={{ span: 24 - 2 * leftSpan }}
        >
          <Form.Item label="Id" name="id">
            <InputNumber disabled />
          </Form.Item>
          <Form.Item label="Title" name="title">
            <Input />
          </Form.Item>
          <Form.Item label="Creation date" name="createDate">
            <DatePicker allowClear={false} disabled />
          </Form.Item>
          <Form.Item label="Active" name="active" valuePropName="checked">
            <Checkbox />
          </Form.Item>
          <Form.Item label="Pinned" name="pinned" valuePropName="checked">
            <Checkbox />
          </Form.Item>
          <Form.Item
            label="Category"
            name={['categoryId']}
            /* normalize={(e: string[]) => {
                console.log('nn', e, typeof e);
                return e;
              }} */
          >
            <Select
              allowClear={false}
              style={{ width: '100%' }}
              placeholder="Please select"
              // defaultValue={selected.map(q => q.name)}
            >
              {queryCategories.data.map(qr => (
                <Select.Option key={qr.id}>{qr.name}</Select.Option>
              ))}
            </Select>
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
