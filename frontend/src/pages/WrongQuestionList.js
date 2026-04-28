import React, { useState, useEffect } from 'react';
import {
  Card,
  List,
  Tag,
  Button,
  Space,
  Input,
  Select,
  Tabs,
  Modal,
  Form,
  Input as AntInput,
  message,
  Empty,
  Spin,
  Statistic,
  Row,
  Col,
  Divider,
} from 'antd';
import {
  CheckCircleOutlined,
  EditOutlined,
  BookOutlined,
  ClockCircleOutlined,
  SearchOutlined,
} from '@ant-design/icons';
import { useUserStore } from '../store/userStore';

const { TextArea } = AntInput;
const { Option } = Select;
const { TabPane } = Tabs;

const WrongQuestionList = () => {
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [wrongQuestions, setWrongQuestions] = useState([]);
  const [filteredQuestions, setFilteredQuestions] = useState([]);
  const [resolveModalVisible, setResolveModalVisible] = useState(false);
  const [currentQuestion, setCurrentQuestion] = useState(null);
  const [form] = Form.useForm();
  const [filters, setFilters] = useState({
    status: 'unresolved',
    keyword: '',
    knowledgePoint: '',
  });
  const [activeTab, setActiveTab] = useState('unresolved');

  const mockWrongQuestions = [
    {
      id: 1,
      studentId: 1,
      studentName: '张三',
      questionId: 2,
      question: {
        id: 2,
        orderIndex: 2,
        type: 'MULTIPLE_CHOICE',
        content: '下列函数中，在x=0处连续的有：',
        options: 'A. f(x) = sinx/x\nB. f(x) = |x|\nC. f(x) = x²\nD. f(x) = 1/x',
        correctAnswer: 'B,C',
        score: 15,
        knowledgePoints: '函数连续性',
        difficulty: '中等',
      },
      studentAnswer: 'A,B',
      wrongCount: 2,
      isResolved: false,
      firstWrongAt: '2024-01-10 10:30',
      lastWrongAt: '2024-01-15 10:30',
      notes: '',
    },
    {
      id: 2,
      studentId: 1,
      studentName: '张三',
      questionId: 7,
      question: {
        id: 7,
        orderIndex: 1,
        type: 'SINGLE_CHOICE',
        content: '函数f(x) = |x|在x=0处：',
        options: 'A. 连续且可导\nB. 连续但不可导\nC. 不连续但可导\nD. 不连续也不可导',
        correctAnswer: 'B',
        score: 10,
        knowledgePoints: '导数与连续',
        difficulty: '中等',
      },
      studentAnswer: 'A',
      wrongCount: 1,
      isResolved: false,
      firstWrongAt: '2024-01-12 14:20',
      lastWrongAt: '2024-01-12 14:20',
      notes: '',
    },
    {
      id: 3,
      studentId: 1,
      studentName: '张三',
      questionId: 9,
      question: {
        id: 9,
        orderIndex: 3,
        type: 'FILL_BLANK',
        content: '函数f(x)在x=a处可导是f(x)在x=a处连续的______条件。',
        correctAnswer: '充分不必要',
        score: 5,
        knowledgePoints: '导数与连续',
        difficulty: '简单',
      },
      studentAnswer: '必要',
      wrongCount: 3,
      isResolved: true,
      firstWrongAt: '2024-01-08 09:15',
      lastWrongAt: '2024-01-10 16:30',
      resolvedAt: '2024-01-11 10:00',
      notes: '可导必连续，连续不一定可导，所以可导是连续的充分不必要条件。',
    },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setWrongQuestions(mockWrongQuestions);
      setFilteredQuestions(mockWrongQuestions.filter(q => !q.isResolved));
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, []);

  useEffect(() => {
    let result = [...wrongQuestions];
    
    if (activeTab === 'unresolved') {
      result = result.filter(q => !q.isResolved);
    } else if (activeTab === 'resolved') {
      result = result.filter(q => q.isResolved);
    }
    
    if (filters.keyword) {
      result = result.filter(q =>
        q.question.content.toLowerCase().includes(filters.keyword.toLowerCase())
      );
    }
    
    if (filters.knowledgePoint) {
      result = result.filter(q => q.question.knowledgePoints === filters.knowledgePoint);
    }
    
    setFilteredQuestions(result);
  }, [wrongQuestions, activeTab, filters]);

  const getQuestionTypeText = (type) => {
    const typeMap = {
      SINGLE_CHOICE: '单选题',
      MULTIPLE_CHOICE: '多选题',
      TRUE_FALSE: '判断题',
      FILL_BLANK: '填空题',
      SHORT_ANSWER: '简答题',
      ESSAY: '论述题',
    };
    return typeMap[type] || type;
  };

  const getPriority = (wrongCount) => {
    if (wrongCount >= 3) return { text: '高', color: 'red', class: 'priority-high' };
    if (wrongCount >= 2) return { text: '中', color: 'orange', class: 'priority-medium' };
    return { text: '低', color: 'green', class: 'priority-low' };
  };

  const handleMarkResolved = (question) => {
    setCurrentQuestion(question);
    form.setFieldsValue({ notes: question.notes });
    setResolveModalVisible(true);
  };

  const handleSaveResolve = async (values) => {
    const updatedQuestions = wrongQuestions.map(q =>
      q.id === currentQuestion.id
        ? { ...q, isResolved: true, notes: values.notes, resolvedAt: new Date().toISOString() }
        : q
    );
    setWrongQuestions(updatedQuestions);
    message.success('已标记为已解决');
    setResolveModalVisible(false);
  };

  const unresolvedCount = wrongQuestions.filter(q => !q.isResolved).length;
  const resolvedCount = wrongQuestions.filter(q => q.isResolved).length;

  const knowledgePoints = [...new Set(wrongQuestions.map(q => q.question.knowledgePoints))];

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
              title="总错题数"
              value={wrongQuestions.length}
              prefix={<BookOutlined />}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="待解决"
              value={unresolvedCount}
              valueStyle={{ color: '#ff4d4f' }}
              prefix={<ClockCircleOutlined />}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="已解决"
              value={resolvedCount}
              valueStyle={{ color: '#52c41a' }}
              prefix={<CheckCircleOutlined />}
            />
          </Col>
          <Col xs={12} sm={6}>
            <Statistic
              title="知识点"
              value={knowledgePoints.length}
              suffix="个"
            />
          </Col>
        </Row>
      </Card>

      <Card
        title={isTeacher() ? '学生错题管理' : '我的错题本'}
        className="card-shadow"
        extra={
          <Space>
            <Input
              placeholder="搜索题目"
              prefix={<SearchOutlined />}
              style={{ width: 200 }}
              value={filters.keyword}
              onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
              allowClear
            />
            <Select
              placeholder="筛选知识点"
              style={{ width: 150 }}
              value={filters.knowledgePoint || undefined}
              onChange={(value) => setFilters({ ...filters, knowledgePoint: value })}
              allowClear
            >
              {knowledgePoints.map(kp => (
                <Option key={kp} value={kp}>{kp}</Option>
              ))}
            </Select>
          </Space>
        }
      >
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'unresolved',
              label: `待解决 (${unresolvedCount})`,
            },
            {
              key: 'resolved',
              label: `已解决 (${resolvedCount})`,
            },
            {
              key: 'all',
              label: `全部 (${wrongQuestions.length})`,
            },
          ]}
        />

        {filteredQuestions.length === 0 ? (
          <Empty
            description={
              activeTab === 'unresolved'
                ? '暂无待解决的错题，继续加油！'
                : activeTab === 'resolved'
                ? '暂无已解决的错题'
                : '暂无错题记录'
            }
            style={{ padding: '40px 0' }}
          />
        ) : (
          <List
            dataSource={filteredQuestions}
            renderItem={(item) => {
              const priority = getPriority(item.wrongCount);
              return (
                <List.Item
                  className="wrong-question-item"
                  actions={
                    !item.isResolved
                      ? [
                          <Button
                            type="link"
                            icon={<CheckCircleOutlined />}
                            onClick={() => handleMarkResolved(item)}
                          >
                            标记已解决
                          </Button>,
                        ]
                      : []
                  }
                >
                  <List.Item.Meta
                    title={
                      <Space wrap>
                        <span className="question-number">
                          {getQuestionTypeText(item.question.type)}
                        </span>
                        <Tag color={item.question.difficulty === '困难' ? 'red' : item.question.difficulty === '中等' ? 'orange' : 'green'}>
                          {item.question.difficulty}
                        </Tag>
                        <Tag color={priority.color}>
                          优先级: {priority.text}
                        </Tag>
                        <Tag>
                          错误 {item.wrongCount} 次
                        </Tag>
                        {item.isResolved && <Tag color="green">已解决</Tag>}
                      </Space>
                    }
                    description={
                      <div style={{ width: '100%' }}>
                        <div style={{ marginBottom: 8, fontWeight: 500 }}>
                          {item.question.content}
                        </div>

                        {item.question.options && (
                          <div style={{ marginBottom: 8, whiteSpace: 'pre-wrap', color: '#666', fontSize: 13 }}>
                            {item.question.options}
                          </div>
                        )}

                        <Divider style={{ margin: '8px 0' }} />

                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16 }}>
                          <div>
                            <span style={{ fontWeight: 500 }}>我的答案：</span>
                            <span className="wrong-answer">{item.studentAnswer || '(未作答)'}</span>
                          </div>
                          <div>
                            <span style={{ fontWeight: 500 }}>正确答案：</span>
                            <span className="correct-answer">{item.question.correctAnswer}</span>
                          </div>
                          <div>
                            <span style={{ fontWeight: 500 }}>知识点：</span>
                            <Tag>{item.question.knowledgePoints}</Tag>
                          </div>
                        </div>

                        {item.notes && (
                          <div style={{ marginTop: 8, padding: 8, background: '#fafafa', borderRadius: 4 }}>
                            <span style={{ fontWeight: 500 }}>笔记：</span>
                            {item.notes}
                          </div>
                        )}

                        <div style={{ marginTop: 8, color: '#999', fontSize: 12 }}>
                          第一次错误: {item.firstWrongAt} | 
                          最后错误: {item.lastWrongAt}
                          {item.resolvedAt && ` | 解决时间: ${item.resolvedAt}`}
                        </div>
                      </div>
                    }
                  />
                </List.Item>
              );
            }}
          />
        )}
      </Card>

      <Modal
        title="标记为已解决"
        open={resolveModalVisible}
        onCancel={() => setResolveModalVisible(false)}
        footer={null}
      >
        <Form form={form} layout="vertical" onFinish={handleSaveResolve}>
          <Form.Item
            name="notes"
            label="学习笔记（可选）"
          >
            <TextArea
              rows={4}
              placeholder="请输入您对这道题的理解和总结..."
            />
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setResolveModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" icon={<CheckCircleOutlined />}>
                确认解决
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default WrongQuestionList;
