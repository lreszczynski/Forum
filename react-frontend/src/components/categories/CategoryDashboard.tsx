/* eslint-disable no-restricted-syntax */
import { Button, Checkbox, Form, Input, InputNumber, Space } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQuery } from 'react-query';
import { useNavigate, useParams } from 'react-router-dom';
import { Category } from 'models/Category';
import CategoryService from 'services/CategoryService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface ICategoryDashboardProps {}

export default function CategoryDashboard(_props: ICategoryDashboardProps) {
  const params = useParams();
  const id = Number(params.id);
  const query = useQuery(`categories/${id}`, () =>
    CategoryService.getCategoryById(id),
  );
  const mutation = useMutation<Category, AxiosError, Category, any>(
    (newCategory: Category) => CategoryService.updateCategory(newCategory),
  );

  const leftSpan = 4;
  const navigate = useNavigate();

  const update = async (values: any) => {
    console.log(',,,', values);

    const newCategory: Category = values;
    mutation.mutate(newCategory, {
      onSuccess: (_data, _variables) => {
        notificationSuccessfulEdit();
      },
      onError: (error, _variables) => {
        console.log();

        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
  };

  if (query.isSuccess) {
    return (
      <>
        <Title level={2} style={{ textAlign: 'center' }}>
          Category
        </Title>
        <Form
          name="basic"
          initialValues={{
            id: query.data.id,
            name: query.data.name,
            description: query.data.description,
            active: query.data.active,
            remember: true,
          }}
          onFinish={update}
          labelCol={{ span: leftSpan }}
          wrapperCol={{ span: 24 - 2 * leftSpan }}
        >
          <Form.Item label="Id" name="id">
            <InputNumber disabled />
          </Form.Item>
          <Form.Item label="Name" name="name">
            <Input />
          </Form.Item>
          <Form.Item label="Description" name="description">
            <TextArea />
          </Form.Item>
          <Form.Item label="Active" name="active" valuePropName="checked">
            <Checkbox>Active</Checkbox>
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
