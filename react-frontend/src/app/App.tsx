import './App.scss';

import { Drawer, Layout } from 'antd';
import React, { useEffect, useState } from 'react';
import { use100vh } from 'react-div-100vh';
import { Outlet, useLocation } from 'react-router-dom';
import AppHeader from './AppHeader';
import AppNavbar from './AppNavbar';

const { Content, Footer, Sider } = Layout;
function App() {
  const [isCollapsed, setCollapsed] = useState(false);
  const [isRotated, setRotated] = useState(false);
  const myheight = use100vh();

  const location = useLocation();

  useEffect(() => {
    console.log('Router Outlet: ', location.pathname);
  }, [location]);

  const collapse = () => {
    setCollapsed(!isCollapsed);
    setRotated(!isRotated);
  };

  useEffect(() => {}, []);

  return (
    <Layout style={{ minHeight: myheight! }}>
      <AppHeader collapse={collapse} isCollapsed={isCollapsed} />

      <Layout>
        <Drawer
          className="hideOnDesktop"
          placement="left"
          onClose={collapse}
          closable={false}
          visible={!isCollapsed}
          width="256px"
          bodyStyle={{ padding: '0' }}
        >
          <AppNavbar
            location={location.pathname}
            isRotated={isRotated}
            setCollapsed={collapse}
            isDrawer
          />
        </Drawer>
        <Sider
          style={{
            overflow: 'auto',
            height: '100vh',
            position: 'sticky',
            top: 0,
            left: 0,
            bottom: 0,
          }}
          className="hideOnMobile"
          collapsible
          breakpoint="lg"
          collapsedWidth="80"
          onCollapse={collapse}
          collapsed={isCollapsed}
          width="256px"
          trigger={null}
          theme="dark"
        >
          <AppNavbar
            location={location.pathname}
            isRotated={isRotated}
            setCollapsed={collapse}
          />
        </Sider>
        <Layout
          style={{
            backgroundColor: '#111',
          }}
        >
          <Content>
            <div
              style={{
                padding: '24px',
                maxWidth: '1280px',
                margin: '64px auto 0',
              }}
            >
              <Outlet />
            </div>
          </Content>
          <Footer style={{ textAlign: 'center' }}>
            Ant Design ©2018 Created by Ant UED
          </Footer>
        </Layout>
      </Layout>
    </Layout>
  );
}

export default App;
