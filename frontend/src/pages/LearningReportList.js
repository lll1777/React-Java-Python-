import React, { useState, useEffect } from 'react';
import {
  Card,
  List,
  Button,
  Space,
  Select,
  Tag,
  Empty,
  Spin,
  message,
  Statistic,
  Row,
  Col,
} from 'antd';
import {
  PlusOutlined,
  FileSearchOutlined,
  BarChartOutlined,
  RightOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../store/userStore';
import dayjs from 'dayjs';

const { Option } = Select;

const LearningReportList = () => {
  const navigate = useNavigate();
  const { user } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [reports, setReports] = useState([]);
  const [generating, setGenerating] = useState(false);

  const mockReports = [
    {
      id: 1,
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
    },
    {
      id: 2,
      studentId: 1,
      studentName: '张三',
      reportType: 'DAILY',
      title: '每日学习报告',
      summary: '报告期间：完成了 1 份作业。平均得分：85分。未解决的错题：1 道。',
      detailData: {},
      reportPeriodStart: '2024-01-14 00:00:00',
      reportPeriodEnd: '2024-01-15 00:00:00',
      totalAssignments: 2,
      submittedAssignments: 1,
      gradedAssignments: 1,
      averageScore: 85,
      highestScore: 85,
      lowestScore: 85,
      totalQuestions: 10,
      correctQuestions: 8,
      wrongQuestions: 1,
      accuracyRate: 80,
      knowledgePointAnalysis: '知识点掌握情况分析：\n- 函数连续性：错误 1 次',
      recommendations: '建议复习函数连续性相关内容。',
      generatedAt: '2024-01-15 08:00:00',
    },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setReports(mockReports);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, []);

  const getReportTypeText = (type) => {
    const typeMap = {
      DAILY: { text: '日报', color: 'blue' },
      WEEKLY: { text: '周报', color: 'green' },
      MONTHLY: { text: '月报', color: 'purple' },
      CUSTOM: { text: '自定义', color: 'orange' },
    };
    return typeMap[type] || { text: type, color: 'default' };
  };

  const handleGenerateReport = async (reportType) => {
    setGenerating(true);
    message.loading('正在生成学习报告...', 2).then(() => {
      const newReport = {
        id: Date.now(),
        studentId: user.id,
        studentName: user.realName,
        reportType: reportType,
        title: `${getReportTypeText(reportType).text} - ${dayjs().format('YYYY-MM-DD')}`,
        summary: '新生成的学习报告',
        generatedAt: new Date().toISOString(),
      };
      setReports([newReport, ...reports]);
      message.success('学习报告生成成功！');
      setGenerating(false);
    });
  };

  const stats = {
    totalReports: reports.length,
    averageScore: reports.length > 0
      ? (reports.reduce((sum, r) => sum + (r.averageScore || 0), 0) / reports.length).toFixed(1)
      : 0,
    totalWrongQuestions: reports.reduce((sum, r) => sum + (r.wrongQuestions || 0), 0),
    accuracyRate: reports.length > 0
      ? (reports.reduce((sum, r) => sum + (r.accuracyRate || 0), 0) / reports.length).toFixed(1)
      : 0,
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div>
      <Card className="card-shadow" style={{ marginBottom: 16 }}>
        <Row gutter={16}>
          <Col xs={12} sm={6}>
            <Statistic
              title="报告总数"
              value={stats.totalReports}
              prefix={<FileSearchOutlined />}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="平均成绩"
              value={stats.averageScore}
              suffix="/ 100"
              valueStyle={{ color: '#1890ff' }}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="累计错题"
              value={stats.totalWrongQuestions}
              valueStyle={{ color: '#ff4d4f' }}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="平均正确率"
              value={stats.accuracyRate}
              suffix="%"
              valueStyle={{ color: '#52c41a' }}
            />
          </Col>
        </Row>
      </Card>

      <Card
        title="学习报告"
        className="card-shadow"
        extra={
          <Space>
            <Select
              placeholder="生成报告"
              style={{ width: 150 }}
              onChange={handleGenerateReport}
              loading={generating}
            >
              <Option value="DAILY">生成日报</Option>
              <Option value="WEEKLY">生成周报</Option>
              <Option value="MONTHLY">生成月报</Option>
            </Select>
          </Space>
        }
      >
        {reports.length === 0 ? (
          <Empty
            description="暂无学习报告，点击上方按钮生成"
            style={{ padding: '40px 0' }}
          />
        ) : (
          <List
            grid={{ gutter: 16, column: 1, md: 2, lg: 3 }}
            dataSource={reports}
            renderItem={(report) => {
              const typeInfo = getReportTypeText(report.reportType);
              return (
                <List.Item>
                  <Card
                    hoverable
                    onClick={() => navigate(`/reports/${report.id}`)}
                    actions={[
                      <Button type="link" onClick={(e) => { e.stopPropagation(); navigate(`/reports/${report.id}`); }}>
                        查看详情 <RightOutlined />
                      </Button>,
                    ]}
                  >
                    <Card.Meta
                      title={
                        <Space>
                          <BarChartOutlined style={{ color: '#1890ff' }} />
                          {report.title}
                        </Space>
                      }
                      description={
                        <div>
                          <Space style={{ marginBottom: 8 }}>
                            <Tag color={typeInfo.color}>{typeInfo.text}</Tag>
                            {report.averageScore !== undefined && (
                              <Tag color="blue">平均分: {report.averageScore}</Tag>
                            )}
                          </Space>
                          <p style={{ marginBottom: 8, color: '#666', fontSize: 12 }}>
                            {report.summary?.substring(0, 100)}...
                          </p>
                          <p style={{ margin: 0, color: '#999', fontSize: 12 }}>
                            生成时间: {dayjs(report.generatedAt).format('YYYY-MM-DD HH:mm')}
                          </p>
                        </div>
                      }
                    />
                  </Card>
                </List.Item>
              );
            }}
          />
        )}
      </Card>
    </div>
  );
};

export default LearningReportList;
