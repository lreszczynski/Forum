import Text from "antd/lib/typography/Text";
import React from "react";
import { NavLink } from "react-router-dom";
import './sider.scss'
import {Layout, Menu} from "antd";
import {DesktopOutlined, FileOutlined, PieChartOutlined, TeamOutlined, UserOutlined} from "@ant-design/icons";

class Sider extends React.Component {
    state = {
        collapsed: false,
    };

    onCollapse = (collapsed: boolean) => {
        console.log(collapsed);
        this.setState({collapsed});
    };

    render() {
        const {collapsed} = this.state;
        return (
            <Layout.Sider collapsible collapsed={collapsed} onCollapse={this.onCollapse}>
                <div className="logo"/>
                <Menu defaultSelectedKeys={['1']} mode="inline">
                    <Menu.Item key="1" icon={<PieChartOutlined/>}>
                        Forum
                    </Menu.Item>
                    <Menu.Item key="2" icon={<DesktopOutlined/>}>
                        Option 2
                    </Menu.Item>
                    <Menu.SubMenu key="sub1" icon={<UserOutlined/>} title="User">
                        <Menu.Item key="3">Tom</Menu.Item>
                        <Menu.Item key="4">Bill</Menu.Item>
                        <Menu.Item key="5">Alex</Menu.Item>
                    </Menu.SubMenu>
                    <Menu.SubMenu key="sub2" icon={<TeamOutlined/>} title="Team">
                        <Menu.Item key="6">Team 1</Menu.Item>
                        <Menu.Item key="8">Team 2</Menu.Item>
                    </Menu.SubMenu>
                    <Menu.Item key="9" icon={<FileOutlined/>}>
                        Files
                    </Menu.Item>
                </Menu>
            </Layout.Sider>
        );
    }
}

export default Sider;
