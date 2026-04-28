import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Table, Tag, Button, List, Progress, Space } from 'antd';
import {
  FileTextOutlined,
  EditOutlined,
  BookOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  WarningOutlined,
  PlusOutlined,
  RightOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, BarChart, Bar, PieChart, Pie, Cell } from 'recharts';
import { useUserStore } from '../store/userStore';
import dayjs from 'dayjs';

const Dashboard = () => {
  const navigate = useNavigate();
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(true);

  const scoreTrendData = [
    { name: '第1周', score: 75, classAvg: 70 },
    { name: '第2周', score: 82, classAvg: 75 },
    { name: '第3周', score: 78, classAvg: 73 },
    { name: '第4周', score: 90, classAvg: 78 },
    { name: '第5周', score: 88, classAvg: 80 },
  ];

  const scoreDistributionData = [
    { name: '0-59', value: 2, color: '#ff4d4f' },
    { name: '60-69', value: 5, color: '#faad14' },
    { name: '70-79', value: 8, color: '#1890ff' },
    { name: '80-89', value: 12, color: '#52c41a' },
    { name: '90-100', value: 6, color: '#13c2c2' },
  ];

  const recentAssignments = [
    {
      id: 1,
      title: '高等数学 - 第三章练习',
      status: 'OPEN',
      deadline: dayjs().add(2, 'day').format('YYYY-MM-DD HH:mm'),
      class: '高数1班',
    },
    {
      id: 2,
      title: '线性代数 - 矩阵运算',
      status: 'SUBMITTED',
      deadline: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm'),
      class: '高数1班',
    },
    {
      id: 3,
      title: '概率论 - 条件概率',
      status: 'GRADED',
      deadline: dayjs().subtract(3, 'day').format('YYYY-MM-DD HH:mm'),
      class: '高数1班',
      score: 85,
    },
  ];

  const pendingGrading = [
    { id: 1, student: '张三', assignment: '高等数学 - 第三章练习', submitTime: '2024-01-15 10:30' },
    { id: 2, student: '李四', assignment: '高等数学 - 第三章练习', submitTime: '2024-01-15 11:20' },
    { id: 3, student: '王五', assignment: '线性代数 - 矩阵运算', submitTime: '2024-01-14 16:45' },
  ];

  const wrongQuestionsData = [
    { id: 1, knowledge: '函数极限', wrongCount: 3, isResolved: false },
    { id: 2, knowledge: '导数应用', wrongCount: 2, isResolved: false },
    { id: 3, knowledge: '积分计算', wrongCount: 1, isResolved: true },
  ];

  useEffect(() => {
    const timer = setTimeout(() => setLoading(false), 500);
    return () => clearTimeout(timer);
  }, []);

  const getStatusTag = (status) => {
    const statusMap = {
      DRAFT: { color: 'default', text: '草稿' },
      PUBLISHED: { color: 'blue', text: '已发布' },
      OPEN: { color: 'green', text: '进行中' },
      SUBMITTED: { color: 'orange', text: '已提交' },
      AUTO_GRADING: { color: 'processing', text: '自动批改中' },
      AUTO_GRADED: { color: 'cyan', text: '自动批改完成' },
      MANUAL_GRADING: { color: 'processing', text: '人工批改中' },
      GRADED: { color: 'success', text: '已批改' },
      RETURNED: { color: 'purple', text: '已返回' },
      ARCHIVED: { color: 'default', text: '已归档' },
    };
    const info = statusMap[status] || { color: 'default', text: status };
    return <Tag color={info.color}>{info.text}</Tag>;
  };

  const studentDashboard = () => (
    <>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="待完成作业"
              value={3}
              prefix={<ClockCircleOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="已提交作业"
              value={12}
              prefix={<FileTextOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="待解决错题"
              value={5}
              prefix={<WarningOutlined style={{ color: '#ff4d4f' }} />}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="平均成绩"
              value={82.5}
              suffix="/ 100"
              prefix={<CheckCircleOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={16}>
          <Card
            title="成绩趋势"
            className="card-shadow"
            extra={
              <Button type="link" onClick={() => navigate('/statistics')}>
                查看详情 <RightOutlined />
              </Button>
            }
          >
            <ResponsiveContainer width="100%" height={280}>
              <LineChart data={scoreTrendData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Line type="monotone" dataKey="score" stroke="#1890ff" strokeWidth={2} name="我的成绩" />
                <Line type="monotone" dataKey="classAvg" stroke="#52c41a" strokeWidth={2} strokeDasharray="5 5" name="班级平均" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card title="薄弱知识点" className="card-shadow">
            <List
              dataSource={wrongQuestionsData.filter(w => !w.isResolved)}
              renderItem={(item) => (
                <List.Item>
                  <List.Item.Meta
                    title={
                      <Space>
                        <span>{item.knowledge}</span>
                        <Tag color="red">{item.wrongCount}次错误</Tag>
                      </Space>
                    }
                    description={
                      <Progress percent={0} size="small" showInfo={false} />
                    }
                  />
                </List.Item>
              )}
            />
            <Button type="link" block onClick={() => navigate('/wrong-questions')}>
              查看全部错题
            </Button>
          </Card>
        </Col>
      </Row>

      <Card
        title="最近作业"
        className="card-shadow"
        style={{ marginTop: 16 }}
        extra={
          <Button type="link" onClick={() => navigate('/assignments')}>
            全部作业 <RightOutlined />
          </Button>
        }
      >
        <List
          grid={{ gutter: 16, column: 1, md: 2, lg: 3 }}
          dataSource={recentAssignments}
          renderItem={(item) => (
            <List.Item>
              <Card
                hoverable
                onClick={() => navigate(`/assignments/${item.id}`)}
                actions={[
                  <Button type="link" onClick={(e) => { e.stopPropagation(); navigate(`/assignments/${item.id}`); }}>
                    查看详情
                  </Button>,
                ]}
              >
                <Card.Meta
                  title={item.title}
                  description={
                    <div>
                      <p style={{ margin: '8px 0' }}>截止时间: {item.deadline}</p>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        {getStatusTag(item.status)}
                        {item.score !== undefined && (
                          <span style={{ fontWeight: 600, color: '#1890ff' }}>
                            {item.score}分
                          </span>
                        )}
                      </div>
                    </div>
                  }
                />
              </Card>
            </List.Item>
          )}
        />
      </Card>
    </>
  );

  const teacherDashboard = () => (
    <>
      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="已发布作业"
              value={8}
              prefix={<FileTextOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="待批改作业"
              value={15}
              prefix={<EditOutlined style={{ color: '#faad14' }} />}
              valueStyle={{ color: '#faad14' }}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="班级学生"
              value={45}
              prefix={<BookOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card className="card-shadow">
            <Statistic
              title="班级平均分"
              value={76.8}
              suffix="/ 100"
              prefix={<CheckCircleOutlined style={{ color: '#13c2c2' }} />}
            />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card
            title="成绩分布"
            className="card-shadow"
            extra={
              <Button type="link" onClick={() => navigate('/statistics')}>
                查看详情 <RightOutlined />
              </Button>
            }
          >
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={scoreDistributionData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" name="人数">
                  {scoreDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card
            title="待批改作业"
            className="card-shadow"
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/assignments/create')}>
                发布新作业
              </Button>
            }
          >
            <List
              dataSource={pendingGrading}
              renderItem={(item) => (
                <List.Item
                  actions={[
                    <Button type="link" onClick={() => navigate(`/submissions/${item.id}`)}>
                      批改
                    </Button>,
                  ]}
                >
                  <List.Item.Meta
                    title={
                      <Space>
                        <span>{item.student}</span>
                        <Tag color="blue">{item.assignment}</Tag>
                      </Space>
                    }
                    description={`提交时间: ${item.submitTime}`}
                  />
                </List.Item>
              )}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="最近作业"
        className="card-shadow"
        style={{ marginTop: 16 }}
        extra={
          <Button type="link" onClick={() => navigate('/assignments')}>
            全部作业 <RightOutlined />
          </Button>
        }
      >
        <Table
          dataSource={recentAssignments}
          rowKey="id"
          pagination={false}
        >
          <Table.Column title="作业标题" dataIndex="title" />
          <Table.Column title="班级" dataIndex="class" />
          <Table.Column title="截止时间" dataIndex="deadline" />
          <Table.Column
            title="状态"
            dataIndex="status"
            render={(status) => getStatusTag(status)}
          />
          <Table.Column
            title="操作"
            render={(_, record) => (
              <Space>
                <Button type="link" onClick={() => navigate(`/assignments/${record.id}`)}>
                  查看
                </Button>
                <Button type="link" onClick={() => navigate(`/assignments/${record.id}/edit`)}>
                  编辑
                </Button>
              </Space>
            )}
          />
        </Table>
      </Card>
    </>
  );

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>
        {isTeacher() ? '教师工作台' : '学生工作台'}
      </h2>
      {isTeacher() ? teacherDashboard() : studentDashboard()}
    </div>
  );
};

export default Dashboard;
