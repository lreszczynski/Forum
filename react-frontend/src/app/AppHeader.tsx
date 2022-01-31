import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  SearchOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, Dropdown, Menu } from 'antd';
import { Header } from 'antd/lib/layout/layout';
import Text from 'antd/lib/typography/Text';
import * as React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import AuthService from 'services/AuthService';

export interface IAppHeaderProps {
  isCollapsed: boolean;
  collapse: Function;
}

export default function AppHeader(props: IAppHeaderProps) {
  const { isCollapsed, collapse } = props;
  const navigate = useNavigate();
  const tokens = AuthService.getCurrentUserTokens();

  const menu = (
    <Menu>
      <Menu.Item key={1}>
        <Link
          to="/"
          onClick={() => {
            AuthService.logout();
          }}
        >
          Logout
        </Link>
      </Menu.Item>
    </Menu>
  );

  return (
    <Header
      style={{ padding: 0, position: 'fixed', zIndex: 1, width: '100%' }}
      id="header-flex"
    >
      <div className="start">
        <div
          tabIndex={0}
          role="button"
          className="triggerable"
          onClick={() => collapse()}
          onKeyPress={() => collapse()}
        >
          {isCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        </div>
        <div
          tabIndex={0}
          role="button"
          className="triggerable"
          onClick={() => collapse()}
          onKeyPress={() => collapse()}
        >
          {isCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
        </div>
      </div>
      <div className="end">
        <div
          tabIndex={0}
          role="button"
          className="triggerable"
          onClick={() => AuthService.logout()}
          onKeyPress={() => AuthService.logout()}
        >
          <SearchOutlined />
        </div>

        {tokens === undefined ? (
          <Button
            style={{ marginRight: '16px' }}
            onKeyPress={() => {
              navigate('/login');
            }}
            onClick={() => {
              navigate('/login');
            }}
          >
            Log in
          </Button>
        ) : (
          <>
            <div
              tabIndex={0}
              role="button"
              className="triggerable"
              onKeyPress={() => {
                navigate('/login');
              }}
              onClick={() => {
                navigate('/login');
              }}
            >
              <UserOutlined />
            </div>
            <Dropdown overlay={menu} placement="bottomCenter" arrow>
              <div className="avatarHeader" style={{ cursor: 'pointer' }}>
                <Text>{AuthService.getCurrentUser()?.username.at(0)}</Text>
              </div>
            </Dropdown>
          </>
        )}
      </div>
    </Header>
  );
}
