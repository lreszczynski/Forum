import {
  HomeFilled,
  HomeOutlined,
  Html5TwoTone,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import React, { useState } from 'react';
import { BrowserRouter, Link } from 'react-router-dom';

import RouterOutlet from './components/router-outlet/routerOutlet';

import './App.scss';

const { Header, Content, Footer, Sider } = Layout;
function App() {
  const [isCollapsed, setCollapsed] = useState(false);

  const collapse = () => {
    setCollapsed(!isCollapsed);
  };
  return (
    <BrowserRouter>
      <Layout>
        <Sider
          className="sider"
          collapsible
          breakpoint="md"
          collapsedWidth="80"
          onBreakpoint={broken => {
            console.log('OB', broken);
          }}
          onCollapse={(collapsed, type) => {
            console.log('OC', collapsed, type);
            collapse();
          }}
          collapsed={isCollapsed}
          width="256px"
          trigger={null}
        >
          <div className="sidebar-logo">
            <a href="https://ant.design/" target="_blank" rel="noreferrer">
              <Html5TwoTone className="icon" />
              <h1>Forum</h1>
            </a>
          </div>
          <Menu
            theme="dark"
            mode="inline"
            inlineCollapsed={isCollapsed}
            defaultSelectedKeys={['4']}
          >
            <Menu.Item key="1" icon={<HomeFilled />}>
              <Link to="/">
                <span>Home</span>
              </Link>
            </Menu.Item>
            <Menu.Item key="2" icon={<HomeOutlined />}>
              <Link to="/short">
                <span>Home short</span>
              </Link>
            </Menu.Item>
            <Menu.Item key="3" icon={<UploadOutlined />}>
              <Link to="/categories">
                <span>Categories</span>
              </Link>
            </Menu.Item>
            <Menu.Item key="4" icon={<UserOutlined />}>
              nav 4
            </Menu.Item>
          </Menu>
        </Sider>
        <Layout>
          <Header style={{ padding: 0 }} className="app-header">
            <div className="start">
              <span
                role="button"
                tabIndex={0}
                className="header-trigger"
                onClick={collapse}
                onKeyPress={collapse}
              >
                {isCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                {console.log(isCollapsed)}
              </span>
            </div>
            <div className="end">
              <UserOutlined className="header-trigger" />
            </div>
          </Header>
          <Content style={{ margin: '24px 16px 0' }}>
            <div
              className="site-layout-background"
              style={{ padding: 24, minHeight: 360 }}
            >
              <RouterOutlet />
            </div>
          </Content>
          <Footer style={{ textAlign: 'center' }}>
            Ant Design ©2018 Created by Ant UED
          </Footer>
        </Layout>
      </Layout>
    </BrowserRouter>
  );
}

export default App;
