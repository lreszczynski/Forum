import {
  GroupOutlined,
  HomeFilled,
  HomeOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Layout, Menu } from 'antd';
import React, { useEffect, useState } from 'react';
import { BrowserRouter, Link } from 'react-router-dom';

import logo from 'images/logo192.png';
import { store } from 'store';
import RouterOutlet from './components/router-outlet/routerOutlet';

import './App.scss';

const { Header, Content, Footer, Sider } = Layout;
function App() {
  const [isCollapsed, setCollapsed] = useState(false);
  const [isRotated, setRotated] = useState(false);
  const [location, setLocation] = useState('/');

  store.subscribe(() => {
    console.log('>>>>', location);
  });

  const collapse = () => {
    setCollapsed(!isCollapsed);
    setRotated(!isRotated);
  };

  useEffect(() => {
    // const location = useLocation();
  }, []);

  return (
    <BrowserRouter>
      <Layout>
        <Sider
          id="sider"
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
          <div id="sidebar-logo">
            <Link to="/">
              <img
                className={isRotated ? 'rotateA' : 'rotateB'}
                width="32px"
                src={logo}
                alt=""
              />
              <h1>Forum</h1>
            </Link>
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
        </Sider>
        <Layout>
          <Header style={{ padding: 0 }} id="header-flex">
            <div className="start">
              <div
                tabIndex={0}
                role="button"
                className="triggerable"
                onClick={collapse}
                onKeyPress={collapse}
              >
                {isCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                {console.log(isCollapsed)}
              </div>
              <div
                tabIndex={0}
                role="button"
                className="triggerable"
                onClick={collapse}
                onKeyPress={collapse}
              >
                {isCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                {console.log(isCollapsed)}
              </div>
            </div>
            <div className="end">
              <div tabIndex={0} role="button" className="triggerable">
                <UserOutlined className="triggerable" />
              </div>

              <div tabIndex={0} role="button" className="triggerable">
                <UserOutlined className="triggerable" />
              </div>
            </div>
          </Header>
          <Content>
            <div
              style={{ padding: '24px', maxWidth: '1024px', margin: '0 auto' }}
            >
              <RouterOutlet stateChanger={setLocation} />
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
