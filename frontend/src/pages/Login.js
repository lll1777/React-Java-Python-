import React, { useState } from 'react';
import { Form, Input, Button, Card, message, Select, Alert } from 'antd';
import { UserOutlined, LockOutlined, BookOutlined, TeamOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../store/userStore';

const { Option } = Select;

const Login = () => {
  const navigate = useNavigate();
  const { login, loading } = useUserStore();
  const [userType, setUserType] = useState('student');

  const onFinish = async (values) => {
    const result = await login(values.username, values.password);
    if (result.success) {
      message.success('登录成功！');
      navigate('/dashboard');
    } else {
      message.error(result.message || '登录失败，请检查用户名和密码');
    }
  };

  return (
    <div className="login-container">
      <Card className="login-card">
        <div className="login-title">
          <h1>📚 在线教育平台</h1>
          <p>作业批改与学习跟踪系统</p>
        </div>

        <Alert
          message="测试账号"
          description={
            <div>
              <p><strong>学生账号：</strong>student1 / 123456</p>
              <p><strong>教师账号：</strong>teacher1 / 123456</p>
            </div>
          }
          type="info"
          showIcon
          style={{ marginBottom: 24 }}
        />

        <Form
          name="login"
          onFinish={onFinish}
          autoComplete="off"
          size="large"
        >
          <Form.Item
            name="userType"
            initialValue="student"
          >
            <Select
              value={userType}
              onChange={setUserType}
              prefix={<TeamOutlined />}
            >
              <Option value="student">
                <BookOutlined /> 学生登录
              </Option>
              <Option value="teacher">
                <TeamOutlined /> 教师登录
              </Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名！' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="请输入用户名"
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码！' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="请输入密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              size="large"
            >
              登 录
            </Button>
          </Form.Item>
        </Form>

        <div style={{ textAlign: 'center', color: '#999', fontSize: 12 }}>
          <p>© 2024 在线教育作业批改与学习跟踪平台</p>
        </div>
      </Card>
    </div>
  );
};

export default Login;
