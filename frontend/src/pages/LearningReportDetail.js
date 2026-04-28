import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Divider,
  Row,
  Col,
  Statistic,
  Spin,
  Empty,
  List,
  Progress,
} from 'antd';
import {
  ArrowLeftOutlined,
  BarChartOutlined,
  BookOutlined,
  WarningOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from 'recharts';
import dayjs from 'dayjs';

const COLORS = ['#ff4d4f', '#faad14', '#52c41a', '#1890ff', '#722ed1', '#13c2c2'];

const LearningReportDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [loading, setLoading] = useState(true);
  const [report, setReport] = useState(null);

  const mockReport = {
    id: parseInt(id),
    studentId: 1,
    studentName: '张三',
    reportType: 'WEEKLY',
    title: '每周学习报告',
    summary: '报告期间：完成了 3 份作业。平均得分：82.5分。未解决的错题：5 道。',
    detailData: {},
    reportPeriodStart: '2024-01-08 00:00:00',
    reportPeriodEnd: '2024-01-15 00:00:00',
    totalAssignments: 5,
    submittedAssignments: 3,
    gradedAssignments: 3,
    averageScore: 82.5,
    highestScore: 92,
    lowestScore: 75,
    totalQuestions: 45,
    correctQuestions: 37,
    wrongQuestions: 5,
    accuracyRate: 82.2,
    knowledgePointAnalysis: '知识点掌握情况分析：\n- 函数极限：错误 3 次\n- 导数应用：错误 2 次\n- 积分计算：错误 1 次',
    recommendations: '【学习建议】\n\n1. 优先攻克薄弱知识点：\n   - 函数极限（错误3次）\n   - 导数应用（错误2次）\n\n2. 建议的学习路径：\n   a. 复习相关概念和公式\n   b. 重做错题，理解错误原因\n   c. 找类似题目进行练习巩固\n   d. 定期回顾，避免遗忘',
    generatedAt: '2024-01-15 18:00:00',
  };

  const scoreTrendData = [
    { name: '作业1', score: 78, classAvg: 75 },
    { name: '作业2', score: 85, classAvg: 78 },
    { name: '作业3', score: 92, classAvg: 80 },
  ];

  const wrongQuestionData = [
    { name: '函数极限', value: 3 },
    { name: '导数应用', value: 2 },
    { name: '积分计算', value: 1 },
  ];

  const knowledgePoints = [
    { name: '函数极限', accuracy: 60, wrongCount: 3 },
    { name: '导数应用', accuracy: 70, wrongCount: 2 },
    { name: '积分计算', accuracy: 85, wrongCount: 1 },
    { name: '函数连续性', accuracy: 90, wrongCount: 1 },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setReport(mockReport);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, [id]);

  const getReportTypeText = (type) => {
    const typeMap = {
      DAILY: { text: '日报', color: 'blue' },
      WEEKLY: { text: '周报', color: 'green' },
      MONTHLY: { text: '月报', color: 'purple' },
      CUSTOM: { text: '自定义', color: 'orange' },
    };
    return typeMap[type] || { text: type, color: 'default' };
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  if (!report) {
    return (
      <div className="empty-container">
        <Empty description="报告不存在" />
      </div>
    );
  }

  const typeInfo = getReportTypeText(report.reportType);

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/reports')}
          style={{ marginBottom: 16 }}
        >
          返回报告列表
        </Button>
      </div>

      <Card
        className="card-shadow"
        title={
          <Space>
            <BarChartOutlined style={{ color: '#1890ff', fontSize: 24 }} />
            <span style={{ fontSize: 18 }}>{report.title}</span>
            <Tag color={typeInfo.color}>{typeInfo.text}</Tag>
          </Space>
        }
        extra={
          <Space>
            <Tag>
              生成时间: {dayjs(report.generatedAt).format('YYYY-MM-DD HH:mm')}
            </Tag>
          </Space>
        }
      >
        <Descriptions column={4} bordered>
          <Descriptions.Item label="报告周期">
            {dayjs(report.reportPeriodStart).format('YYYY-MM-DD')} ~ 
            {dayjs(report.reportPeriodEnd).format('YYYY-MM-DD')}
          </Descriptions.Item>
          <Descriptions.Item label="学生">{report.studentName}</Descriptions.Item>
          <Descriptions.Item label="作业总数">{report.totalAssignments}</Descriptions.Item>
          <Descriptions.Item label="已提交">{report.submittedAssignments}</Descriptions.Item>
          <Descriptions.Item label="已批改">{report.gradedAssignments}</Descriptions.Item>
          <Descriptions.Item label="平均成绩">
            <span style={{ color: '#1890ff', fontWeight: 600 }}>{report.averageScore}分</span>
          </Descriptions.Item>
          <Descriptions.Item label="最高分">
            <span style={{ color: '#52c41a', fontWeight: 600 }}>{report.highestScore}分</span>
          </Descriptions.Item>
          <Descriptions.Item label="最低分">
            <span style={{ color: '#ff4d4f', fontWeight: 600 }}>{report.lowestScore}分</span>
          </Descriptions.Item>
        </Descriptions>
      </Card>

      <Card title="成绩统计概览" className="card-shadow" style={{ marginTop: 16 }}>
        <Row gutter={16}>
          <Col xs={12} sm={6}>
            <Card size="small" className="stat-card">
              <Statistic
                title="总题目数"
                value={report.totalQuestions}
                prefix={<BookOutlined />}
              />
            </Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small" className="stat-card">
              <Statistic
                title="做对题目"
                value={report.correctQuestions}
                valueStyle={{ color: '#52c41a' }}
                prefix={<CheckCircleOutlined />}
              />
            </Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small" className="stat-card">
              <Statistic
                title="做错题目"
                value={report.wrongQuestions}
                valueStyle={{ color: '#ff4d4f' }}
                prefix={<WarningOutlined />}
              />
            </Card>
          </Col>
          <Col xs={12} sm={6}>
            <Card size="small" className="stat-card">
              <Statistic
                title="正确率"
                value={report.accuracyRate}
                suffix="%"
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
        </Row>
      </Card>

      <Row gutter={16} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="成绩趋势" className="card-shadow">
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={scoreTrendData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="score" name="我的成绩" fill="#1890ff" />
                <Bar dataKey="classAvg" name="班级平均" fill="#52c41a" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="错题分布" className="card-shadow">
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie
                  data={wrongQuestionData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {wrongQuestionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>

      <Card title="知识点掌握情况" className="card-shadow" style={{ marginTop: 16 }}>
        <List
          dataSource={knowledgePoints}
          renderItem={(item) => (
            <List.Item>
              <List.Item.Meta
                title={
                  <Space>
                    <span style={{ fontWeight: 500 }}>{item.name}</span>
                    {item.accuracy >= 80 ? (
                      <Tag color="green">掌握较好</Tag>
                    ) : item.accuracy >= 60 ? (
                      <Tag color="orange">需要加强</Tag>
                    ) : (
                      <Tag color="red">薄弱环节</Tag>
                    )}
                    <Tag>错误 {item.wrongCount} 次</Tag>
                  </Space>
                }
                description={
                  <div>
                    <div style={{ marginBottom: 4 }}>
                      正确率: {item.accuracy}%
                    </div>
                    <Progress
                      percent={item.accuracy}
                      strokeColor={
                        item.accuracy >= 80 ? '#52c41a' :
                        item.accuracy >= 60 ? '#faad14' : '#ff4d4f'
                      }
                      showInfo={false}
                    />
                  </div>
                }
              />
            </List.Item>
          )}
        />
      </Card>

      <Card title="学习建议" className="card-shadow" style={{ marginTop: 16 }}>
        <div className="report-content" style={{ whiteSpace: 'pre-wrap', lineHeight: 2 }}>
          {report.recommendations}
        </div>
      </Card>

      {report.knowledgePointAnalysis && (
        <Card title="知识点分析详情" className="card-shadow" style={{ marginTop: 16 }}>
          <div className="report-content" style={{ whiteSpace: 'pre-wrap', lineHeight: 2 }}>
            {report.knowledgePointAnalysis}
          </div>
        </Card>
      )}
    </div>
  );
};

export default LearningReportDetail;
