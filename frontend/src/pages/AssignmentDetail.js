import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Tag,
  Button,
  Space,
  Divider,
  List,
  Statistic,
  Row,
  Col,
  Modal,
  Form,
  Input,
  InputNumber,
  Select,
  message,
  Empty,
  Spin,
  Table,
} from 'antd';
import {
  ArrowLeftOutlined,
  EditOutlined,
  DeleteOutlined,
  SendOutlined,
  PlayCircleOutlined,
  CheckCircleOutlined,
  PlusOutlined,
  MinusCircleOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useUserStore } from '../store/userStore';
import { assignmentApi, submissionApi } from '../services/api';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;

const AssignmentDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [assignment, setAssignment] = useState(null);
  const [mySubmission, setMySubmission] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [submitModalVisible, setSubmitModalVisible] = useState(false);
  const [form] = Form.useForm();

  const mockAssignment = {
    id: parseInt(id),
    title: '高等数学 - 第三章练习',
    description: '函数极限与连续性的综合练习题，包括极限计算、连续性判断、间断点类型分析等内容。',
    classId: 1,
    className: '高数1班',
    creatorId: 3,
    creatorName: '王老师',
    status: 'OPEN',
    publishTime: '2024-01-10 09:00',
    deadline: '2024-01-20 23:59',
    totalScore: 100,
    submittedCount: 32,
    totalStudents: 45,
    questions: [
      {
        id: 1,
        orderIndex: 1,
        type: 'SINGLE_CHOICE',
        content: '求极限 lim(x→0) sinx/x = ?',
        options: 'A. 0\nB. 1\nC. ∞\nD. 不存在',
        correctAnswer: 'B',
        score: 10,
        knowledgePoints: '函数极限',
        difficulty: '简单',
        explanation: '根据重要极限公式，lim(x→0) sinx/x = 1',
        autoGradable: true,
      },
      {
        id: 2,
        orderIndex: 2,
        type: 'MULTIPLE_CHOICE',
        content: '下列函数中，在x=0处连续的有：',
        options: 'A. f(x) = sinx/x\nB. f(x) = |x|\nC. f(x) = x²\nD. f(x) = 1/x',
        correctAnswer: 'B,C',
        score: 15,
        knowledgePoints: '函数连续性',
        difficulty: '中等',
        explanation: '需要检查函数在x=0处的极限是否等于函数值',
        autoGradable: true,
      },
      {
        id: 3,
        orderIndex: 3,
        type: 'TRUE_FALSE',
        content: '若函数f(x)在x=a处可导，则f(x)在x=a处一定连续。',
        correctAnswer: 'TRUE',
        score: 5,
        knowledgePoints: '导数与连续',
        difficulty: '简单',
        explanation: '可导必连续是定理，连续不一定可导',
        autoGradable: true,
      },
      {
        id: 4,
        orderIndex: 4,
        type: 'FILL_BLANK',
        content: 'lim(x→∞) (1 + 1/x)^x = ______',
        correctAnswer: 'e',
        score: 10,
        knowledgePoints: '重要极限',
        difficulty: '简单',
        explanation: '这是自然常数e的定义式',
        autoGradable: true,
      },
      {
        id: 5,
        orderIndex: 5,
        type: 'SHORT_ANSWER',
        content: '请简述函数f(x)在点x0处连续的定义，并说明连续的三个条件。',
        score: 20,
        knowledgePoints: '函数连续性',
        difficulty: '中等',
        autoGradable: false,
      },
      {
        id: 6,
        orderIndex: 6,
        type: 'ESSAY',
        content: '试论述极限、连续、可导、可微这四个概念之间的关系，并举例说明。',
        score: 40,
        knowledgePoints: '微积分基础概念',
        difficulty: '困难',
        autoGradable: false,
      },
    ],
  };

  const mockSubmissions = [
    {
      id: 1,
      studentId: 1,
      studentName: '张三',
      status: 'SUBMITTED',
      submittedAt: '2024-01-15 10:30',
      totalScore: null,
    },
    {
      id: 2,
      studentId: 2,
      studentName: '李四',
      status: 'GRADED',
      submittedAt: '2024-01-14 16:45',
      totalScore: 85,
      autoScore: 40,
      manualScore: 45,
    },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setAssignment(mockAssignment);
      setSubmissions(mockSubmissions);
      setMySubmission(null);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, [id]);

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

  const getQuestionTypeText = (type) => {
    const typeMap = {
      SINGLE_CHOICE: '单选题',
      MULTIPLE_CHOICE: '多选题',
      TRUE_FALSE: '判断题',
      FILL_BLANK: '填空题',
      SHORT_ANSWER: '简答题',
      ESSAY: '论述题',
      CODING: '编程题',
    };
    return typeMap[type] || type;
  };

  const handleSubmit = async (values) => {
    try {
      message.success('作业提交成功！');
      setSubmitModalVisible(false);
      form.resetFields();
    } catch (error) {
      message.error('提交失败，请重试');
    }
  };

  const handlePublish = async () => {
    try {
      message.success('作业发布成功！');
      setAssignment({ ...assignment, status: 'PUBLISHED' });
    } catch (error) {
      message.error('发布失败，请重试');
    }
  };

  const handleStartAutoGrading = async () => {
    message.loading('正在启动自动批改...', 1).then(() => {
      message.success('自动批改已启动，请稍后查看结果');
    });
  };

  const columns = [
    {
      title: '学生',
      dataIndex: 'studentName',
      key: 'studentName',
    },
    {
      title: '提交时间',
      dataIndex: 'submittedAt',
      key: 'submittedAt',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: '得分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      render: (score) => score !== null ? `${score}分` : '-',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => navigate(`/submissions/${record.id}`)}>
            查看/批改
          </Button>
        </Space>
      ),
    },
  ];

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  if (!assignment) {
    return (
      <div className="empty-container">
        <Empty description="作业不存在" />
      </div>
    );
  }

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/assignments')}
          style={{ marginBottom: 16 }}
        >
          返回作业列表
        </Button>
      </div>

      <Card
        className="card-shadow"
        title={
          <Space>
            <span>{assignment.title}</span>
            {getStatusTag(assignment.status)}
          </Space>
        }
        extra={
          <Space>
            {isTeacher() && assignment.status === 'DRAFT' && (
              <>
                <Button
                  type="primary"
                  onClick={handlePublish}
                >
                  发布作业
                </Button>
                <Button
                  icon={<EditOutlined />}
                  onClick={() => navigate(`/assignments/${id}/edit`)}
                >
                  编辑
                </Button>
              </>
            )}
            {isTeacher() && assignment.status === 'OPEN' && (
              <Button
                icon={<PlayCircleOutlined />}
                onClick={handleStartAutoGrading}
              >
                开始自动批改
              </Button>
            )}
            {!isTeacher() && assignment.status === 'OPEN' && !mySubmission && (
              <Button
                type="primary"
                icon={<SendOutlined />}
                onClick={() => setSubmitModalVisible(true)}
              >
                提交作业
              </Button>
            )}
          </Space>
        }
      >
        <Descriptions column={3} bordered>
          <Descriptions.Item label="班级">{assignment.className}</Descriptions.Item>
          <Descriptions.Item label="创建教师">{assignment.creatorName}</Descriptions.Item>
          <Descriptions.Item label="总分">
            <span style={{ fontWeight: 600, color: '#1890ff', fontSize: 16 }}>
              {assignment.totalScore}分
            </span>
          </Descriptions.Item>
          <Descriptions.Item label="发布时间">
            {assignment.publishTime ? dayjs(assignment.publishTime).format('YYYY-MM-DD HH:mm') : '-'}
          </Descriptions.Item>
          <Descriptions.Item label="截止时间">
            {dayjs(assignment.deadline).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          {isTeacher() && (
            <Descriptions.Item label="提交情况">
              <span style={{ color: '#52c41a' }}>{assignment.submittedCount}</span>
              <span style={{ color: '#999' }}>/{assignment.totalStudents} 人</span>
            </Descriptions.Item>
          )}
        </Descriptions>

        {assignment.description && (
          <div style={{ marginTop: 16 }}>
            <h4>作业描述</h4>
            <p style={{ color: '#666', whiteSpace: 'pre-wrap' }}>
              {assignment.description}
            </p>
          </div>
        )}
      </Card>

      {isTeacher() && (
        <Card title="提交统计" className="card-shadow" style={{ marginTop: 16 }}>
          <Row gutter={16}>
            <Col xs={12} sm={6}>
              <Statistic
                title="已提交"
                value={assignment.submittedCount}
                suffix={`/ ${assignment.totalStudents}`}
              />
            </Col>
            <Col xs={12} sm={6}>
              <Statistic
                title="未提交"
                value={assignment.totalStudents - assignment.submittedCount}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Col>
            <Col xs={12} sm={6}>
              <Statistic
                title="已批改"
                value={submissions.filter(s => s.status === 'GRADED').length}
              />
            </Col>
            <Col xs={12} sm={6}>
              <Statistic
                title="待批改"
                value={submissions.filter(s => s.status === 'SUBMITTED').length}
                valueStyle={{ color: '#faad14' }}
              />
            </Col>
          </Row>
        </Card>
      )}

      <Card
        title="题目列表"
        className="card-shadow"
        style={{ marginTop: 16 }}
        extra={<Tag color="blue">共 {assignment.questions?.length || 0} 题</Tag>}
      >
        <List
          dataSource={assignment.questions || []}
          renderItem={(question) => (
            <List.Item className="question-card">
              <div>
                <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
                  <span className="question-number">第{question.orderIndex}题</span>
                  <Tag>{getQuestionTypeText(question.type)}</Tag>
                  <Tag color="blue">{question.score}分</Tag>
                  {question.difficulty && <Tag color={question.difficulty === '困难' ? 'red' : question.difficulty === '中等' ? 'orange' : 'green'}>{question.difficulty}</Tag>}
                  {question.autoGradable && <Tag color="cyan">可自动批改</Tag>}
                </div>
                <div style={{ marginBottom: 8, fontWeight: 500 }}>
                  {question.content}
                </div>
                {question.options && (
                  <div style={{ marginBottom: 8, whiteSpace: 'pre-wrap', color: '#666' }}>
                    {question.options}
                  </div>
                )}
                {isTeacher() && question.correctAnswer && (
                  <div style={{ color: '#52c41a' }}>
                    <strong>正确答案：</strong>{question.correctAnswer}
                  </div>
                )}
                {isTeacher() && question.explanation && (
                  <div style={{ marginTop: 8, color: '#666', fontSize: 13 }}>
                    <strong>解析：</strong>{question.explanation}
                  </div>
                )}
                {question.knowledgePoints && (
                  <div style={{ marginTop: 8 }}>
                    <strong>知识点：</strong>
                    <Tag>{question.knowledgePoints}</Tag>
                  </div>
                )}
              </div>
            </List.Item>
          )}
        />
      </Card>

      {isTeacher() && (
        <Card
          title="学生提交列表"
          className="card-shadow"
          style={{ marginTop: 16 }}
        >
          <Table
            columns={columns}
            dataSource={submissions}
            rowKey="id"
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `共 ${total} 条记录`,
            }}
          />
        </Card>
      )}

      <Modal
        title="提交作业"
        open={submitModalVisible}
        onCancel={() => setSubmitModalVisible(false)}
        footer={null}
        width={800}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
        >
          <Divider>请填写以下题目答案</Divider>
          
          {assignment.questions?.map((question) => (
            <div key={question.id} style={{ marginBottom: 24 }}>
              <div style={{ marginBottom: 8, display: 'flex', alignItems: 'center', gap: 8 }}>
                <span className="question-number">第{question.orderIndex}题</span>
                <Tag>{getQuestionTypeText(question.type)}</Tag>
                <Tag color="blue">{question.score}分</Tag>
              </div>
              <div style={{ marginBottom: 12, fontWeight: 500 }}>
                {question.content}
              </div>
              {question.options && (
                <div style={{ marginBottom: 12, whiteSpace: 'pre-wrap', color: '#666' }}>
                  {question.options}
                </div>
              )}

              {question.type === 'SINGLE_CHOICE' && (
                <Form.Item
                  name={`answer_${question.id}`}
                  label="您的答案"
                  rules={[{ required: true, message: '请选择答案' }]}
                >
                  <Select placeholder="请选择答案">
                    {['A', 'B', 'C', 'D'].map(opt => (
                      <Option key={opt} value={opt}>{opt}</Option>
                    ))}
                  </Select>
                </Form.Item>
              )}

              {question.type === 'MULTIPLE_CHOICE' && (
                <Form.Item
                  name={`answer_${question.id}`}
                  label="您的答案（多选，用逗号分隔）"
                  rules={[{ required: true, message: '请选择答案' }]}
                >
                  <Input placeholder="例如：A,B,C" />
                </Form.Item>
              )}

              {question.type === 'TRUE_FALSE' && (
                <Form.Item
                  name={`answer_${question.id}`}
                  label="您的答案"
                  rules={[{ required: true, message: '请选择答案' }]}
                >
                  <Select placeholder="请选择答案">
                    <Option value="TRUE">正确 (True)</Option>
                    <Option value="FALSE">错误 (False)</Option>
                  </Select>
                </Form.Item>
              )}

              {question.type === 'FILL_BLANK' && (
                <Form.Item
                  name={`answer_${question.id}`}
                  label="您的答案"
                  rules={[{ required: true, message: '请填写答案' }]}
                >
                  <Input placeholder="请填写答案" />
                </Form.Item>
              )}

              {(question.type === 'SHORT_ANSWER' || question.type === 'ESSAY') && (
                <Form.Item
                  name={`answer_${question.id}`}
                  label="您的答案"
                  rules={[{ required: true, message: '请填写答案' }]}
                >
                  <TextArea
                    rows={6}
                    placeholder="请在此输入您的答案..."
                  />
                </Form.Item>
              )}
            </div>
          ))}

          <Form.Item style={{ textAlign: 'right' }}>
            <Space>
              <Button onClick={() => setSubmitModalVisible(false)}>
                取消
              </Button>
              <Button type="primary" htmlType="submit" icon={<SendOutlined />}>
                提交作业
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default AssignmentDetail;
