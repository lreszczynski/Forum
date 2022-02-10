import { MenuFoldOutlined, MenuUnfoldOutlined } from '@ant-design/icons';
import { Button, Dropdown, Menu } from 'antd';
import Search from 'antd/lib/input/Search';
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

  const onSearch = (value: any) => {
    console.log(value);
    navigate(`/posts/search?text=${value}`);
  };

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
      </div>
      <div className="middle">
        <Search placeholder="input search text" onSearch={onSearch} />
      </div>
      <div className="end">
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
          <Dropdown overlay={menu} placement="bottomCenter" arrow>
            <div className="avatarHeader" style={{ cursor: 'pointer' }}>
              <Text>
                {AuthService.getCurrentUser()?.username.at(0)?.toUpperCase()}
              </Text>
            </div>
          </Dropdown>
        )}
      </div>
    </Header>
  );
}
