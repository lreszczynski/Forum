import './App.scss';

import { Drawer, Layout } from 'antd';
import React, { useEffect, useState } from 'react';
import { use100vh } from 'react-div-100vh';
import { BrowserRouter } from 'react-router-dom';
import AppHeader from './AppHeader';
import AppNavbar from './AppNavbar';
import RouterOutlet from './RouterOutlet';

const { Content, Footer, Sider } = Layout;
function App() {
  const [isCollapsed, setCollapsed] = useState(false);
  const [isRotated, setRotated] = useState(false);
  const [location, setLocation] = useState('/');
  const myheight = use100vh();

  const collapse = () => {
    setCollapsed(!isCollapsed);
    setRotated(!isRotated);
  };

  useEffect(() => {}, []);

  return (
    <BrowserRouter>
      <Layout style={{ minHeight: myheight! }}>
        <Drawer
          className="hideOnDesktop"
          placement="left"
          onClose={collapse}
          closable={false}
          visible={!isCollapsed}
          width="256px"
          bodyStyle={{ backgroundColor: '#001529', padding: '0' }}
        >
          <AppNavbar
            location={location}
            isRotated={isRotated}
            setCollapsed={collapse}
            isDrawer
          />
        </Drawer>
        <Sider
          className="hideOnMobile"
          collapsible
          breakpoint="lg"
          collapsedWidth="80"
          onCollapse={collapse}
          collapsed={isCollapsed}
          width="256px"
          trigger={null}
        >
          <AppNavbar
            location={location}
            isRotated={isRotated}
            setCollapsed={collapse}
          />
        </Sider>
        <Layout>
          <AppHeader collapse={collapse} isCollapsed={isCollapsed} />
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
