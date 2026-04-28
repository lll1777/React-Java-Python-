import React, { useState, useEffect } from 'react';
import {
  Card,
  Row,
  Col,
  Statistic,
  Select,
  Spin,
  Empty,
  Tabs,
  List,
  Tag,
  Progress,
} from 'antd';
import {
  FileTextOutlined,
  BookOutlined,
  BarChartOutlined,
  TeamOutlined,
  CheckCircleOutlined,
  WarningOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons';
import { useUserStore } from '../store/userStore';
import {
  LineChart,
  Line,
  AreaChart,
  Area,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  RadarChart,
  PolarGrid,
  PolarAngleAxis,
  PolarRadiusAxis,
  Radar,
} from 'recharts';

const { Option } = Select;
const { TabPane } = Tabs;

const COLORS = ['#ff4d4f', '#faad14', '#52c41a', '#1890ff', '#722ed1', '#13c2c2'];

const Statistics = () => {
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [timeRange, setTimeRange] = useState('week');

  const scoreTrendData = [
    { name: '周一', score: 75, classAvg: 70 },
    { name: '周二', score: 82, classAvg: 72 },
    { name: '周三', score: 78, classAvg: 75 },
    { name: '周四', score: 90, classAvg: 78 },
    { name: '周五', score: 88, classAvg: 80 },
    { name: '周六', score: 85, classAvg: 79 },
    { name: '周日', score: 92, classAvg: 82 },
  ];

  const scoreDistributionData = [
    { name: '0-59', value: 2, color: '#ff4d4f' },
    { name: '60-69', value: 5, color: '#faad14' },
    { name: '70-79', value: 8, color: '#1890ff' },
    { name: '80-89', value: 12, color: '#52c41a' },
    { name: '90-100', value: 6, color: '#13c2c2' },
  ];

  const submissionTrendData = [
    { name: '第1周', submitted: 32, total: 45 },
    { name: '第2周', submitted: 40, total: 45 },
    { name: '第3周', submitted: 38, total: 45 },
    { name: '第4周', submitted: 42, total: 45 },
  ];

  const knowledgeRadarData = [
    { subject: '函数极限', A: 60, fullMark: 100 },
    { subject: '导数应用', A: 70, fullMark: 100 },
    { subject: '积分计算', A: 85, fullMark: 100 },
    { subject: '函数连续性', A: 90, fullMark: 100 },
    { subject: '矩阵运算', A: 75, fullMark: 100 },
    { subject: '概率统计', A: 65, fullMark: 100 },
  ];

  const assignmentStats = [
    {
      title: '高等数学 - 第三章练习',
      submitted: 32,
      total: 45,
      average: 78.5,
      highest: 98,
      lowest: 45,
      status: '已批改',
    },
    {
      title: '线性代数 - 矩阵运算',
      submitted: 42,
      total: 45,
      average: 82.3,
      highest: 100,
      lowest: 55,
      status: '已批改',
    },
    {
      title: '概率论 - 条件概率',
      submitted: 45,
      total: 45,
      average: 75.8,
      highest: 95,
      lowest: 35,
      status: '已批改',
    },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, []);

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  const studentContent = () => (
    <>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="已完成作业"
              value={12}
              prefix={<FileTextOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="平均成绩"
              value={82.5}
              suffix="/ 100"
              valueStyle={{ color: '#52c41a' }}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="待解决错题"
              value={5}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<WarningOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="已解决错题"
              value={8}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="成绩趋势分析"
        className="card-shadow"
        style={{ marginBottom: 16 }}
        extra={
          <Select
            value={timeRange}
            onChange={setTimeRange}
            style={{ width: 120 }}
          >
            <Option value="week">本周</Option>
            <Option value="month">本月</Option>
            <Option value="term">本学期</Option>
          </Select>
        }
      >
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={scoreTrendData}>
            <defs>
              <linearGradient id="colorScore" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#1890ff" stopOpacity={0.8} />
                <stop offset="95%" stopColor="#1890ff" stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis domain={[0, 100]} />
            <Tooltip />
            <Legend />
            <Area type="monotone" dataKey="score" stroke="#1890ff" fillOpacity={1} fill="url(#colorScore)" name="我的成绩" />
            <Line type="monotone" dataKey="classAvg" stroke="#52c41a" strokeDasharray="5 5" name="班级平均" />
          </AreaChart>
        </ResponsiveContainer>
      </Card>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="知识点雷达图" className="card-shadow">
            <ResponsiveContainer width="100%" height={300}>
              <RadarChart data={knowledgeRadarData}>
                <PolarGrid />
                <PolarAngleAxis dataKey="subject" />
                <PolarRadiusAxis angle={30} domain={[0, 100]} />
                <Radar name="掌握程度" dataKey="A" stroke="#1890ff" fill="#1890ff" fillOpacity={0.6} />
                <Tooltip />
              </RadarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="成绩分布" className="card-shadow">
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={scoreDistributionData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={100}
                  fill="#8884d8"
                  paddingAngle={5}
                  dataKey="value"
                  label={({ name, value }) => `${name}: ${value}人`}
                >
                  {scoreDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Card title="作业完成情况" className="card-shadow">
        <List
          dataSource={assignmentStats}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
              title={
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span>{item.title}</span>
                  <Tag>{item.status}</Tag>
                </div>
              }
              description={
                <div>
                  <div style={{ marginBottom: 8 }}>
                    <span style={{ marginRight: 24 }}>
                      提交: {item.submitted}/{item.total} 人
                    </span>
                    <span style={{ marginRight: 24, color: '#1890ff' }}>
                      平均分: {item.average}
                    </span>
                    <span style={{ marginRight: 24, color: '#52c41a' }}>
                      最高分: {item.highest}
                    </span>
                    <span style={{ color: '#ff4d4f' }}>
                      最低分: {item.lowest}
                    </span>
                  </div>
                  <Progress
                    percent={Math.round(item.submitted / item.total * 100)}
                    showInfo={false}
                  />
                </div>
              }
            />
            </List.Item>
          )}
        />
      </Card>
    </>
  );

  const teacherContent = () => (
    <>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="已发布作业"
              value={8}
              prefix={<FileTextOutlined style={{ color: '#1890ff' }} />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="待批改作业"
              value={15}
              valueStyle={{ color: '#faad14' }}
              prefix={<ClockCircleOutlined />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="班级学生"
              value={45}
              prefix={<TeamOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>
        <Col xs={12} sm={6}>
          <Card className="card-shadow">
            <Statistic
              title="班级平均分"
              value={76.8}
              suffix="/ 100"
              valueStyle={{ color: '#1890ff' }}
            />
          </Card>
        </Col>
      </Row>

      <Card
        title="提交趋势"
        className="card-shadow"
        style={{ marginBottom: 16 }}
        extra={
          <Select
            value={timeRange}
            onChange={setTimeRange}
            style={{ width: 120 }}
          >
            <Option value="week">本周</Option>
            <Option value="month">本月</Option>
            <Option value="term">本学期</Option>
          </Select>
        }
      >
        <ResponsiveContainer width="100%" height={300}>
          <BarChart data={submissionTrendData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="name" />
            <YAxis />
            <Tooltip />
            <Legend />
            <Bar dataKey="submitted" name="已提交" fill="#52c41a" />
            <Bar dataKey="total" name="总人数" fill="#e8e8e8" />
          </BarChart>
        </ResponsiveContainer>
      </Card>

      <Row gutter={16} style={{ marginBottom: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="班级成绩分布" className="card-shadow">
            <ResponsiveContainer width="100%" height={300}>
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
          <Card title="成绩分布饼图" className="card-shadow">
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={scoreDistributionData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {scoreDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Card title="各作业统计详情" className="card-shadow">
        <List
          dataSource={assignmentStats}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span>{item.title}</span>
                    <Tag color="green">{item.status}</Tag>
                  </div>
                }
                description={
                  <div>
                    <Row gutter={16}>
                      <Col xs={12} sm={4}>
                        <div>提交率</div>
                        <div style={{ fontWeight: 600 }}>
                          {Math.round(item.submitted / item.total * 100)}%
                        </div>
                      </Col>
                      <Col xs={12} sm={4}>
                        <div>平均分</div>
                        <div style={{ fontWeight: 600, color: '#1890ff' }}>
                          {item.average}
                        </div>
                      </Col>
                      <Col xs={12} sm={4}>
                        <div>最高分</div>
                        <div style={{ fontWeight: 600, color: '#52c41a' }}>
                          {item.highest}
                        </div>
                      </Col>
                      <Col xs={12} sm={4}>
                        <div>最低分</div>
                        <div style={{ fontWeight: 600, color: '#ff4d4f' }}>
                          {item.lowest}
                        </div>
                      </Col>
                    </Row>
                    <div style={{ marginTop: 12 }}>
                      <Progress
                        percent={Math.round(item.submitted / item.total * 100)}
                        format={(percent) => `${item.submitted}/${item.total} 人 (${percent}%)`}
                      />
                    </div>
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Card>
    </>
  );

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>
        {isTeacher() ? '班级成绩统计' : '我的学习统计'}
      </h2>
      {isTeacher() ? teacherContent() : studentContent()}
    </div>
  );
};

export default Statistics;
