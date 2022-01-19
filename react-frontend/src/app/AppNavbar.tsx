import './AppNavbar.scss';

import {
  CloseOutlined,
  GroupOutlined,
  HomeFilled,
  HomeOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Col, Menu, Row } from 'antd';
import Title from 'antd/lib/typography/Title';
import * as React from 'react';
import { Link } from 'react-router-dom';
import logo from 'images/logo192.png';

export interface IAppNavbarProps {
  location: string;
  isRotated: boolean;
  setCollapsed: Function;
  isDrawer: boolean;
}

export default function AppNavbar(props: IAppNavbarProps) {
  const { isRotated, location, setCollapsed, isDrawer } = props;

  return (
    <>
      <div id="sidebar-logo">
        <Row>
          <Col flex="auto" style={{ marginTop: '-2px' }}>
            <Link to="/">
              <img
                className={isRotated ? 'rotateA' : 'rotateB'}
                width="32px"
                src={logo}
                alt=""
              />
              <Title>Forum</Title>
            </Link>
          </Col>
          {isDrawer && (
            <Col>
              <div
                className="triggerable"
                role="button"
                tabIndex={0}
                onKeyDown={() => setCollapsed()}
                onClick={() => setCollapsed()}
              >
                <CloseOutlined />
              </div>
            </Col>
          )}
        </Row>
      </div>
      <Menu
        theme="dark"
        mode="inline"
        defaultSelectedKeys={['/']}
        selectedKeys={[`/${location.split('/').at(1)}` || '']}
      >
        <Menu.Item key="/" icon={<HomeFilled />}>
          <Link to="/">
            <span>Home</span>
          </Link>
        </Menu.Item>
        <Menu.Item key="/short" icon={<HomeOutlined />}>
          <Link to="/short">
            <span>Home short</span>
          </Link>
        </Menu.Item>
        <Menu.Item key="/forum" icon={<GroupOutlined />}>
          <Link to="/forum">
            <span>Forum</span>
          </Link>
        </Menu.Item>
        <Menu.Item key="4" icon={<UserOutlined />}>
          nav 4
        </Menu.Item>
      </Menu>
    </>
  );
}

AppNavbar.defaultProps = {
  isDrawer: false,
};
