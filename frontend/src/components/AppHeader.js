import React from 'react';
import { Layout, Dropdown, Avatar, Space, Badge, Button } from 'antd';
import { UserOutlined, BellOutlined, LogoutOutlined, SettingOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../store/userStore';

const { Header } = Layout;

const AppHeader = () => {
  const navigate = useNavigate();
  const { user, logout } = useUserStore();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人信息',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '设置',
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  const notificationItems = [
    {
      key: '1',
      label: (
        <div>
          <p style={{ margin: 0, fontWeight: 500 }}>新作业已发布</p>
          <p style={{ margin: 0, fontSize: 12, color: '#999' }}>高等数学 - 第三章练习</p>
        </div>
      ),
    },
    {
      key: '2',
      label: (
        <div>
          <p style={{ margin: 0, fontWeight: 500 }}>作业已批改</p>
          <p style={{ margin: 0, fontSize: 12, color: '#999' }}>您的作业已完成批改</p>
        </div>
      ),
    },
  ];

  return (
    <Header className="site-layout-background" style={{ padding: '0 24px' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'flex-end', width: '100%' }}>
        <Space size="large">
          <Dropdown menu={{ items: notificationItems }} placement="bottomRight">
            <Badge count={2} size="small">
              <Button type="text" icon={<BellOutlined style={{ fontSize: 18 }} />} />
            </Badge>
          </Dropdown>
          
          <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }}>
              <Avatar size={32} icon={<UserOutlined />} />
              <span style={{ color: '#333' }}>{user?.realName || user?.username}</span>
            </Space>
          </Dropdown>
        </Space>
      </div>
    </Header>
  );
};

export default AppHeader;
