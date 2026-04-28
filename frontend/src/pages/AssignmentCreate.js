import React, { useState } from 'react';
import {
  Card,
  Form,
  Input,
  InputNumber,
  Select,
  DatePicker,
  Button,
  Space,
  Divider,
  Row,
  Col,
  message,
  Modal,
  Typography,
} from 'antd';
import {
  ArrowLeftOutlined,
  PlusOutlined,
  DeleteOutlined,
  SaveOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import dayjs from 'dayjs';

const { Option } = Select;
const { TextArea } = Input;
const { Title } = Typography;

const questionTypes = [
  { value: 'SINGLE_CHOICE', label: '单选题' },
  { value: 'MULTIPLE_CHOICE', label: '多选题' },
  { value: 'TRUE_FALSE', label: '判断题' },
  { value: 'FILL_BLANK', label: '填空题' },
  { value: 'SHORT_ANSWER', label: '简答题' },
  { value: 'ESSAY', label: '论述题' },
];

const difficulties = [
  { value: '简单', label: '简单' },
  { value: '中等', label: '中等' },
  { value: '困难', label: '困难' },
];

const classes = [
  { id: 1, name: '高数1班' },
  { id: 2, name: '高数2班' },
  { id: 3, name: '线性代数班' },
];

const AssignmentCreate = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [questions, setQuestions] = useState([]);
  const [previewModalVisible, setPreviewModalVisible] = useState(false);

  const isEdit = !!id;

  const addQuestion = () => {
    const newQuestion = {
      id: Date.now(),
      orderIndex: questions.length + 1,
      type: 'SINGLE_CHOICE',
      content: '',
      options: '',
      correctAnswer: '',
      score: 10,
      knowledgePoints: '',
      difficulty: '中等',
      explanation: '',
      autoGradable: true,
    };
    setQuestions([...questions, newQuestion]);
  };

  const removeQuestion = (index) => {
    const newQuestions = questions.filter((_, i) => i !== index);
    newQuestions.forEach((q, i) => {
      q.orderIndex = i + 1;
    });
    setQuestions(newQuestions);
  };

  const updateQuestion = (index, field, value) => {
    const newQuestions = [...questions];
    newQuestions[index] = { ...newQuestions[index], [field]: value };
    
    if (field === 'type') {
      const autoGradableTypes = ['SINGLE_CHOICE', 'MULTIPLE_CHOICE', 'TRUE_FALSE', 'FILL_BLANK'];
      newQuestions[index].autoGradable = autoGradableTypes.includes(value);
    }
    
    setQuestions(newQuestions);
  };

  const calculateTotalScore = () => {
    return questions.reduce((sum, q) => sum + (q.score || 0), 0);
  };

  const handleSave = async () => {
    try {
      const values = await form.validateFields();
      
      if (questions.length === 0) {
        message.warning('请至少添加一道题目');
        return;
      }

      setLoading(true);
      
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      message.success(isEdit ? '作业更新成功！' : '作业创建成功！');
      setLoading(false);
      navigate('/assignments');
    } catch (error) {
      setLoading(false);
      console.error('Validation failed:', error);
    }
  };

  const handleSaveAndPublish = async () => {
    try {
      const values = await form.validateFields();
      
      if (questions.length === 0) {
        message.warning('请至少添加一道题目');
        return;
      }

      setLoading(true);
      
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      message.success('作业发布成功！');
      setLoading(false);
      navigate('/assignments');
    } catch (error) {
      setLoading(false);
      console.error('Validation failed:', error);
    }
  };

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
        title={isEdit ? '编辑作业' : '发布新作业'}
        className="card-shadow"
        extra={
          <Space>
            <Tag color={calculateTotalScore() > 0 ? 'blue' : 'default'}>
              总分: {calculateTotalScore()}分
            </Tag>
            <Tag color={questions.length > 0 ? 'green' : 'default'}>
              题目: {questions.length}题
            </Tag>
          </Space>
        }
      >
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            classId: 1,
            deadline: dayjs().add(7, 'day').hour(23).minute(59),
          }}
        >
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="title"
                label="作业标题"
                rules={[{ required: true, message: '请输入作业标题' }]}
              >
                <Input placeholder="请输入作业标题" size="large" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="classId"
                label="所属班级"
                rules={[{ required: true, message: '请选择班级' }]}
              >
                <Select placeholder="请选择班级" size="large">
                  {classes.map(cls => (
                    <Option key={cls.id} value={cls.id}>{cls.name}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="deadline"
                label="截止时间"
                rules={[{ required: true, message: '请选择截止时间' }]}
              >
                <DatePicker
                  showTime
                  style={{ width: '100%' }}
                  placeholder="请选择截止时间"
                  size="large"
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="totalScore"
                label="总分"
                rules={[{ required: true, message: '请输入总分' }]}
              >
                <InputNumber
                  min={1}
                  max={1000}
                  placeholder="请输入总分"
                  style={{ width: '100%' }}
                  size="large"
                  value={calculateTotalScore()}
                  onChange={(value) => form.setFieldValue('totalScore', value)}
                />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="作业描述"
          >
            <TextArea
              rows={4}
              placeholder="请输入作业描述（可选）"
            />
          </Form.Item>

          <Divider>
            <Space>
              <span>题目列表</span>
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={addQuestion}
              >
                添加题目
              </Button>
            </Space>
          </Divider>

          {questions.length === 0 && (
            <div style={{ textAlign: 'center', padding: '40px 0', color: '#999' }}>
              <p>暂无题目，请点击上方"添加题目"按钮添加</p>
            </div>
          )}

          {questions.map((question, index) => (
            <Card
              key={question.id}
              size="small"
              style={{ marginBottom: 16 }}
              title={
                <Space>
                  <span>第{question.orderIndex}题</span>
                  <Select
                    value={question.type}
                    onChange={(value) => updateQuestion(index, 'type', value)}
                    style={{ width: 120 }}
                    size="small"
                  >
                    {questionTypes.map(type => (
                      <Option key={type.value} value={type.value}>{type.label}</Option>
                    ))}
                  </Select>
                  <InputNumber
                    min={1}
                    max={100}
                    value={question.score}
                    onChange={(value) => updateQuestion(index, 'score', value)}
                    size="small"
                    prefix="分值:"
                    style={{ width: 100 }}
                  />
                  {question.autoGradable && <Tag color="cyan">可自动批改</Tag>}
                </Space>
              }
              extra={
                <Button
                  type="text"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => removeQuestion(index)}
                >
                  删除
                </Button>
              }
            >
              <Form.Item label="题目内容" style={{ marginBottom: 12 }}>
                <TextArea
                  rows={2}
                  value={question.content}
                  onChange={(e) => updateQuestion(index, 'content', e.target.value)}
                  placeholder="请输入题目内容"
                />
              </Form.Item>

              {(question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') && (
                <Form.Item label="选项（每行一个，如：A. 选项内容）" style={{ marginBottom: 12 }}>
                  <TextArea
                    rows={4}
                    value={question.options}
                    onChange={(e) => updateQuestion(index, 'options', e.target.value)}
                    placeholder="A. 选项1&#10;B. 选项2&#10;C. 选项3&#10;D. 选项4"
                  />
                </Form.Item>
              )}

              {question.type !== 'ESSAY' && question.type !== 'SHORT_ANSWER' && (
                <Row gutter={16}>
                  <Col xs={12}>
                    <Form.Item label="正确答案" style={{ marginBottom: 12 }}>
                      <Input
                        value={question.correctAnswer}
                        onChange={(e) => updateQuestion(index, 'correctAnswer', e.target.value)}
                        placeholder={
                          question.type === 'TRUE_FALSE' 
                            ? 'TRUE 或 FALSE' 
                            : question.type === 'MULTIPLE_CHOICE' 
                              ? '如：A,B,C' 
                              : '请输入正确答案'
                        }
                      />
                    </Form.Item>
                  </Col>
                  <Col xs={12}>
                    <Form.Item label="知识点" style={{ marginBottom: 12 }}>
                      <Input
                        value={question.knowledgePoints}
                        onChange={(e) => updateQuestion(index, 'knowledgePoints', e.target.value)}
                        placeholder="如：函数极限"
                      />
                    </Form.Item>
                  </Col>
                </Row>
              )}

              {question.type === 'ESSAY' || question.type === 'SHORT_ANSWER' ? (
                <Row gutter={16}>
                  <Col xs={12}>
                    <Form.Item label="知识点" style={{ marginBottom: 12 }}>
                      <Input
                        value={question.knowledgePoints}
                        onChange={(e) => updateQuestion(index, 'knowledgePoints', e.target.value)}
                        placeholder="如：函数极限"
                      />
                    </Form.Item>
                  </Col>
                  <Col xs={12}>
                    <Form.Item label="参考答案" style={{ marginBottom: 12 }}>
                      <Input
                        value={question.correctAnswer}
                        onChange={(e) => updateQuestion(index, 'correctAnswer', e.target.value)}
                        placeholder="参考答案（用于参考）"
                      />
                    </Form.Item>
                  </Col>
                </Row>
              ) : null}

              <Row gutter={16}>
                <Col xs={12}>
                  <Form.Item label="难度" style={{ marginBottom: 12 }}>
                    <Select
                      value={question.difficulty}
                      onChange={(value) => updateQuestion(index, 'difficulty', value)}
                      style={{ width: '100%' }}
                    >
                      {difficulties.map(d => (
                        <Option key={d.value} value={d.value}>{d.label}</Option>
                      ))}
                    </Select>
                  </Form.Item>
                </Col>
              </Row>

              <Form.Item label="解析">
                <TextArea
                  rows={2}
                  value={question.explanation}
                  onChange={(e) => updateQuestion(index, 'explanation', e.target.value)}
                  placeholder="请输入题目解析（可选）"
                />
              </Form.Item>
            </Card>
          ))}

          <Divider />

          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Space size="large">
              <Button onClick={() => navigate('/assignments')}>
                取消
              </Button>
              <Button
                icon={<SaveOutlined />}
                loading={loading}
                onClick={handleSave}
              >
                保存为草稿
              </Button>
              <Button
                type="primary"
                icon={<SendOutlined />}
                loading={loading}
                onClick={handleSaveAndPublish}
              >
                保存并发布
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default AssignmentCreate;
