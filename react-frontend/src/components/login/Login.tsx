import { Button, Checkbox, Form, Input } from 'antd';
import * as React from 'react';
import { useNavigate } from 'react-router-dom';
import AuthService from 'services/AuthService';

export interface ILoginProps {}

export default function Login(_props: ILoginProps) {
  const navigate = useNavigate();

  const onFinish = async (values: any) => {
    await AuthService.login(values.username, values.password);
    navigate('/forum');
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
        label="Password"
        name="password"
        rules={[{ required: true, message: 'Please input your password!' }]}
      >
        <Input.Password />
      </Form.Item>

      <Form.Item
        name="remember"
        valuePropName="checked"
        wrapperCol={{ sm: { offset: leftSpan } }}
      >
        <Checkbox>Remember me</Checkbox>
      </Form.Item>

      <Form.Item wrapperCol={{ sm: { offset: leftSpan } }}>
        <Button type="primary" htmlType="submit">
          Submit
        </Button>
      </Form.Item>
    </Form>
  );
}
