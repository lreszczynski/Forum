/* eslint-disable react/jsx-props-no-spreading */
import { Button, Form, Input } from 'antd';
import { AxiosError } from 'axios';
import * as React from 'react';
import { useMutation, useQueryClient } from 'react-query';
import { useNavigate } from 'react-router-dom';
import { UserRegistration } from 'models/UserRegistration';
import AuthService from 'services/AuthService';
import {
  notificationErrorStatusCode,
  notificationSuccessfulEdit,
} from 'utils/Notifications';

export interface IRegisterProps {}

export default function Register(_props: IRegisterProps) {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const mutation = useMutation<void, AxiosError, UserRegistration, any>(
    (user: UserRegistration) => AuthService.register(user),
  );

  const onFinish = async (values: any) => {
    const user: UserRegistration = {
      username: values.username,
      password: values.password,
      email: values.email,
    };

    mutation.mutate(user, {
      onSuccess: (_data, _variables) => {
        queryClient.invalidateQueries();
        notificationSuccessfulEdit();
        navigate('/login');
      },
      onError: (error, _variables) => {
        if (error.response !== undefined) {
          notificationErrorStatusCode(error.response.status);
        }
      },
    });
  };

  const onFinishFailed = (errorInfo: any) => {
    console.log('Failed:', errorInfo);
  };

  const leftSpan = 6;

  return (
    <Form
      name="basic"
      labelCol={{ span: leftSpan }}
      wrapperCol={{ span: 24 - 2 * leftSpan }}
      initialValues={{ remember: true }}
      onFinish={onFinish}
      onFinishFailed={onFinishFailed}
      autoComplete="off"
    >
      <Form.Item
        label="Username"
        name="username"
        rules={[{ required: true, message: 'Please input your username!' }]}
      >
        <Input />
      </Form.Item>
      <Form.Item
        name="email"
        label="E-mail"
        rules={[
          {
            type: 'email',
            message: 'The input is not valid E-mail!',
          },
          {
            required: true,
            message: 'Please input your E-mail!',
          },
        ]}
      >
        <Input />
      </Form.Item>

      <Form.Item
        label="Password"
        name="password"
        rules={[
          { required: true, message: 'Please input your password!' },
          () => ({
            validator(_, value: string) {
              if (value.length < 8) {
                return Promise.reject(
                  new Error('Password needs to contain at least 8 characters.'),
                );
              }
              if (!value.match(/[A-Z]/)) {
                return Promise.reject(
                  new Error(
                    'Password needs to contain at least 1 uppercase letter.',
                  ),
                );
              }
              if (!value.match(/[a-z]/)) {
                return Promise.reject(
                  new Error(
                    'Password needs to contain at least 1 lowercase letter.',
                  ),
                );
              }
              if (!value.match(/[0-9]/)) {
                return Promise.reject(
                  new Error('Password needs to contain at least 1 number.'),
                );
              }
              if (!value.match(/[^A-Za-z0-9]/)) {
                return Promise.reject(
                  new Error(
                    'Password needs to contain at least 1 special character (#,$,%, etc.).',
                  ),
                );
              }
              return Promise.resolve();
            },
          }),
        ]}
      >
        <Input.Password />
      </Form.Item>

      <Form.Item
        name="confirm"
        label="Confirm Password"
        dependencies={['password']}
        hasFeedback
        rules={[
          {
            required: true,
            message: 'Please confirm your password!',
          },
          ({ getFieldValue }) => ({
            validator(_, value) {
              if (!value || getFieldValue('password') === value) {
                return Promise.resolve();
              }
              return Promise.reject(
                new Error('The two passwords that you entered do not match!'),
              );
            },
          }),
        ]}
      >
        <Input.Password />
      </Form.Item>

      <Form.Item wrapperCol={{ sm: { offset: leftSpan } }}>
        <Button type="primary" htmlType="submit">
          Submit
        </Button>
      </Form.Item>
    </Form>
  );
}
