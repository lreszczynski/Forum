import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Header } from 'antd/lib/layout/layout';
import * as React from 'react';

export interface IAppHeaderProps {
  isCollapsed: boolean;
  collapse: Function;
}

export default function AppHeader(props: IAppHeaderProps) {
  const { isCollapsed, collapse } = props;
  return (
    <Header style={{ padding: 0 }} id="header-flex">
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
        <div tabIndex={0} role="button" className="triggerable">
          <UserOutlined className="triggerable" />
        </div>

        <div tabIndex={0} role="button" className="triggerable">
          <UserOutlined className="triggerable" />
        </div>
      </div>
    </Header>
  );
}
