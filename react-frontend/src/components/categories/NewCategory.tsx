/* eslint-disable no-restricted-syntax */
import { Button, Checkbox, Form, Input, Select, Space } from 'antd';
import TextArea from 'antd/lib/input/TextArea';
import Title from 'antd/lib/typography/Title';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQuery, useQueryClient } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { Category } from 'models/Category';
import { Role } from 'models/Role';
import CategoryService from 'services/CategoryService';
import RoleService from 'services/RoleService';
import {
  notificationError,
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface INewCategoryProps {}

export default function NewCategory(_props: INewCategoryProps) {
  const queryClient = useQueryClient();
  const queryRoles = useQuery(`roles/`, () => RoleService.getAllRoles());
  const mutation = useMutation<Category, AxiosError, Category, any>(
    (newCategory: Category) => CategoryService.createCategory(newCategory),
  );

  const leftSpan = 4;
  const navigate = useNavigate();

  const update = async (values: any) => {
    const formValues = values;
    formValues.roles = [];
    if (formValues.rolesIds !== undefined) {
      (formValues?.rolesIds as String[])
        .map(r => Number(r))
        .forEach(r => {
          (formValues.roles as Role[]).push(
            queryRoles.data?.find(qr => qr.id === r)!,
          );
        });
    }
    delete formValues.rolesIds;

    const newCategory: Category = values;
    console.log(newCategory);

    mutation.mutate(newCategory, {
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

  if (queryRoles.isError) {
    notificationError();
  }

  if (queryRoles.isSuccess) {
    console.log('qrd', queryRoles.data);

    return (
      <>
        <Title level={2} style={{ textAlign: 'center' }}>
          Category
        </Title>
        <Form
          name="basic"
          onFinish={update}
          labelCol={{ span: leftSpan }}
          wrapperCol={{ span: 24 - 2 * leftSpan }}
        >
          <Form.Item
            label="Name"
            name="name"
            rules={[
              {
                required: true,
                max: 50,
              },
            ]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            label="Description"
            name="description"
            rules={[
              {
                required: true,
                max: 250,
                min: 5,
              },
            ]}
          >
            <TextArea />
          </Form.Item>
          <Form.Item label="Active" name="active" valuePropName="checked">
            <Checkbox />
          </Form.Item>
          <Form.Item
            label="Roles"
            name="rolesIds"
            /* normalize={(e: string[]) => {
              console.log('nn', e, typeof e);
              return e;
            }} */
          >
            <Select
              mode="multiple"
              allowClear
              style={{ width: '100%' }}
              placeholder="Please select"
              // defaultValue={selected.map(q => q.name)}
            >
              {queryRoles.data.map(qr => (
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
