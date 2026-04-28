import React from 'react';
import { Layout, Menu } from 'antd';
import {
  DashboardOutlined,
  FileTextOutlined,
  EditOutlined,
  BookOutlined,
  BarChartOutlined,
  FileSearchOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { useNavigate, useLocation } from 'react-router-dom';
import { useUserStore } from '../store/userStore';

const { Sider } = Layout;

const AppSider = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, isTeacher } = useUserStore();

  const teacherMenuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '工作台',
      onClick: () => navigate('/dashboard'),
    },
    {
      key: '/assignments',
      icon: <FileTextOutlined />,
      label: '作业管理',
      onClick: () => navigate('/assignments'),
    },
    {
      key: '/wrong-questions',
      icon: <EditOutlined />,
      label: '批改作业',
      onClick: () => navigate('/wrong-questions'),
    },
    {
      key: '/statistics',
      icon: <BarChartOutlined />,
      label: '成绩统计',
      onClick: () => navigate('/statistics'),
    },
    {
      key: '/reports',
      icon: <FileSearchOutlined />,
      label: '学习报告',
      onClick: () => navigate('/reports'),
    },
  ];

  const studentMenuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '工作台',
      onClick: () => navigate('/dashboard'),
    },
    {
      key: '/assignments',
      icon: <FileTextOutlined />,
      label: '我的作业',
      onClick: () => navigate('/assignments'),
    },
    {
      key: '/wrong-questions',
      icon: <BookOutlined />,
      label: '错题本',
      onClick: () => navigate('/wrong-questions'),
    },
    {
      key: '/reports',
      icon: <FileSearchOutlined />,
      label: '学习报告',
      onClick: () => navigate('/reports'),
    },
    {
      key: '/statistics',
      icon: <BarChartOutlined />,
      label: '学习统计',
      onClick: () => navigate('/statistics'),
    },
  ];

  const menuItems = isTeacher() ? teacherMenuItems : studentMenuItems;

  const getSelectedKeys = () => {
    const path = location.pathname;
    if (path === '/') return ['/dashboard'];
    if (path.startsWith('/assignments/')) return ['/assignments'];
    if (path.startsWith('/submissions/')) return ['/assignments'];
    if (path.startsWith('/reports/')) return ['/reports'];
    return [path];
  };

  return (
    <Sider width={240} theme="dark">
      <div
        style={{
          height: 64,
          margin: 16,
          background: 'rgba(255, 255, 255, 0.2)',
          borderRadius: 6,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        <span style={{ color: '#fff', fontSize: 18, fontWeight: 600 }}>
          📚 在线教育平台
        </span>
      </div>
      
      <Menu
        theme="dark"
        mode="inline"
        selectedKeys={getSelectedKeys()}
        defaultOpenKeys={['sub1']}
        items={menuItems}
      />
      
      <div
        style={{
          position: 'absolute',
          bottom: 24,
          left: 0,
          right: 0,
          padding: '0 24px',
          color: 'rgba(255, 255, 255, 0.6)',
          fontSize: 12,
          textAlign: 'center',
        }}
      >
        <p style={{ margin: 0 }}>当前身份: {isTeacher() ? '教师' : '学生'}</p>
        <p style={{ margin: 0, marginTop: 4 }}>版本 1.0.0</p>
      </div>
    </Sider>
  );
};

export default AppSider;
