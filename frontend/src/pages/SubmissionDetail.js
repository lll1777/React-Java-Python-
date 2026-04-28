import React, { useState, useEffect } from 'react';
import {
  Card,
  Descriptions,
  Button,
  Space,
  Tag,
  Divider,
  List,
  Form,
  InputNumber,
  Input,
  Row,
  Col,
  Statistic,
  message,
  Spin,
  Empty,
  Steps,
} from 'antd';
import {
  ArrowLeftOutlined,
  CheckCircleOutlined,
  PlayCircleOutlined,
  SaveOutlined,
  SendOutlined,
} from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { useUserStore } from '../store/userStore';
import dayjs from 'dayjs';

const { TextArea } = Input;

const SubmissionDetail = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(true);
  const [submission, setSubmission] = useState(null);
  const [form] = Form.useForm();
  const [isAutoGrading, setIsAutoGrading] = useState(false);

  const mockSubmission = {
    id: parseInt(id),
    studentId: 1,
    studentName: '张三',
    assignmentId: 1,
    assignmentTitle: '高等数学 - 第三章练习',
    status: 'SUBMITTED',
    submittedAt: '2024-01-15 10:30',
    autoScore: null,
    manualScore: null,
    totalScore: null,
    teacherComments: '',
    isLate: false,
    answers: [
      {
        id: 1,
        questionId: 1,
        question: {
          id: 1,
          orderIndex: 1,
          type: 'SINGLE_CHOICE',
          content: '求极限 lim(x→0) sinx/x = ?',
          options: 'A. 0\nB. 1\nC. ∞\nD. 不存在',
          correctAnswer: 'B',
          score: 10,
          knowledgePoints: '函数极限',
        },
        studentAnswer: 'B',
        autoScore: 10,
        manualScore: null,
        isCorrect: true,
      },
      {
        id: 2,
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
        },
        studentAnswer: 'A,B',
        autoScore: 0,
        manualScore: null,
        isCorrect: false,
      },
      {
        id: 3,
        questionId: 3,
        question: {
          id: 3,
          orderIndex: 3,
          type: 'TRUE_FALSE',
          content: '若函数f(x)在x=a处可导，则f(x)在x=a处一定连续。',
          correctAnswer: 'TRUE',
          score: 5,
          knowledgePoints: '导数与连续',
        },
        studentAnswer: 'TRUE',
        autoScore: 5,
        manualScore: null,
        isCorrect: true,
      },
      {
        id: 4,
        questionId: 4,
        question: {
          id: 4,
          orderIndex: 4,
          type: 'FILL_BLANK',
          content: 'lim(x→∞) (1 + 1/x)^x = ______',
          correctAnswer: 'e',
          score: 10,
          knowledgePoints: '重要极限',
        },
        studentAnswer: 'E',
        autoScore: 10,
        manualScore: null,
        isCorrect: true,
      },
      {
        id: 5,
        questionId: 5,
        question: {
          id: 5,
          orderIndex: 5,
          type: 'SHORT_ANSWER',
          content: '请简述函数f(x)在点x0处连续的定义，并说明连续的三个条件。',
          score: 20,
          knowledgePoints: '函数连续性',
        },
        studentAnswer: '函数f(x)在点x0处连续的定义是：当x趋近于x0时，f(x)的极限等于f(x0)。\n连续的三个条件是：\n1. f(x0)存在\n2. lim(x→x0)f(x)存在\n3. 极限值等于函数值',
        autoScore: null,
        manualScore: null,
        isCorrect: null,
      },
      {
        id: 6,
        questionId: 6,
        question: {
          id: 6,
          orderIndex: 6,
          type: 'ESSAY',
          content: '试论述极限、连续、可导、可微这四个概念之间的关系，并举例说明。',
          score: 40,
          knowledgePoints: '微积分基础概念',
        },
        studentAnswer: '极限是微积分的基础概念，连续是极限的一种特殊情况，可导是连续的一种特殊情况。\n\n具体关系如下：\n1. 可微 ⇨ 可导 ⇨ 连续 ⇨ 极限存在\n2. 反向不一定成立\n\n例如：f(x) = |x|在x=0处连续但不可导；f(x) = sin(1/x)在x=0处极限不存在，所以不连续。',
        autoScore: null,
        manualScore: null,
        isCorrect: null,
      },
    ],
  };

  const statusSteps = [
    { title: '草稿', status: 'finish' },
    { title: '已发布', status: 'finish' },
    { title: '进行中', status: 'finish' },
    { title: '已提交', status: 'process' },
    { title: '自动批改', status: 'wait' },
    { title: '人工批改', status: 'wait' },
    { title: '已完成', status: 'wait' },
  ];

  useEffect(() => {
    const timer = setTimeout(() => {
      setSubmission(mockSubmission);
      setLoading(false);
    }, 500);
    return () => clearTimeout(timer);
  }, [id]);

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

  const getAnswerDisplay = (answer) => {
    if (!answer.question) return null;
    const q = answer.question;
    
    let displayClass = '';
    if (answer.isCorrect === true) {
      displayClass = 'correct-answer';
    } else if (answer.isCorrect === false) {
      displayClass = 'wrong-answer';
    }

    return (
      <div>
        <div style={{ marginBottom: 8 }}>
          <span style={{ fontWeight: 500 }}>学生答案：</span>
          <span className={displayClass}>
            {answer.studentAnswer || '(未作答)'}
          </span>
        </div>
        
        {isTeacher() && q.correctAnswer && (
          <div style={{ marginBottom: 8, color: '#52c41a' }}>
            <span style={{ fontWeight: 500 }}>正确答案：</span>
            {q.correctAnswer}
          </div>
        )}
        
        {answer.autoScore !== null && (
          <div style={{ marginBottom: 4 }}>
            <Tag color={answer.autoScore === q.score ? 'green' : 'red'}>
              自动评分: {answer.autoScore}/{q.score}分
            </Tag>
          </div>
        )}
        
        {answer.manualScore !== null && (
          <div style={{ marginBottom: 4 }}>
            <Tag color="blue">
              人工评分: {answer.manualScore}/{q.score}分
            </Tag>
          </div>
        )}
        
        {answer.teacherFeedback && (
          <div style={{ marginTop: 8, color: '#666', fontStyle: 'italic' }}>
            教师评语：{answer.teacherFeedback}
          </div>
        )}
      </div>
    );
  };

  const handleStartAutoGrading = async () => {
    setIsAutoGrading(true);
    message.loading('正在进行自动批改...', 2).then(() => {
      const newAnswers = submission.answers.map(answer => {
        if (answer.question.autoGradable && answer.autoScore === null) {
          const isCorrect = answer.studentAnswer?.toUpperCase() === answer.question.correctAnswer?.toUpperCase();
          return {
            ...answer,
            autoScore: isCorrect ? answer.question.score : 0,
            isCorrect: isCorrect,
          };
        }
        return answer;
      });

      const autoTotal = newAnswers.reduce((sum, a) => sum + (a.autoScore || 0), 0);

      setSubmission({
        ...submission,
        status: 'AUTO_GRADED',
        autoScore: autoTotal,
        answers: newAnswers,
      });
      message.success('自动批改完成！');
      setIsAutoGrading(false);
    });
  };

  const handleStartManualGrading = async () => {
    setSubmission({
      ...submission,
      status: 'MANUAL_GRADING',
    });
    message.success('已进入人工批改模式');
  };

  const handleSaveGrading = async (values) => {
    message.loading('保存批改结果...', 1).then(() => {
      message.success('批改结果已保存！');
    });
  };

  const handleCompleteGrading = async () => {
    message.confirm({
      title: '确认完成批改',
      content: '确定要完成批改吗？完成后将无法修改分数。',
      onOk: () => {
        const totalAuto = submission.answers.reduce((sum, a) => sum + (a.autoScore || 0), 0);
        const totalManual = submission.answers.reduce((sum, a) => sum + (a.manualScore || 0), 0);

        setSubmission({
          ...submission,
          status: 'GRADED',
          manualScore: totalManual,
          totalScore: totalAuto + totalManual,
        });
        message.success('批改完成！');
      },
    });
  };

  const handleReturnToStudent = async () => {
    message.confirm({
      title: '确认返回作业',
      content: '确定要将作业返回给学生吗？学生将能看到批改结果。',
      onOk: () => {
        setSubmission({
          ...submission,
          status: 'RETURNED',
        });
        message.success('作业已返回给学生！');
      },
    });
  };

  if (loading) {
    return (
      <div className="loading-container">
        <Spin size="large" />
      </div>
    );
  }

  if (!submission) {
    return (
      <div className="empty-container">
        <Empty description="提交记录不存在" />
      </div>
    );
  }

  const isGraded = ['GRADED', 'RETURNED', 'ARCHIVED'].includes(submission.status);
  const canAutoGrade = submission.status === 'SUBMITTED';
  const canManualGrade = submission.status === 'AUTO_GRADED' || submission.status === 'MANUAL_GRADING';

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/assignments')}
          style={{ marginBottom: 16 }}
        >
          返回
        </Button>
      </div>

      <Card
        className="card-shadow"
        title={
          <Space>
            <span>{submission.assignmentTitle}</span>
            {getStatusTag(submission.status)}
            {submission.isLate && <Tag color="red">迟交</Tag>}
          </Space>
        }
        extra={
          isTeacher() ? (
            <Space>
              {canAutoGrade && (
                <Button
                  type="primary"
                  icon={<PlayCircleOutlined />}
                  onClick={handleStartAutoGrading}
                  loading={isAutoGrading}
                >
                  开始自动批改
                </Button>
              )}
              {canManualGrade && !isGraded && (
                <>
                  <Button
                    icon={<SaveOutlined />}
                    onClick={handleSaveGrading}
                  >
                    保存批改
                  </Button>
                  <Button
                    type="primary"
                    icon={<CheckCircleOutlined />}
                    onClick={handleCompleteGrading}
                  >
                    完成批改
                  </Button>
                </>
              )}
              {isGraded && submission.status !== 'RETURNED' && (
                <Button
                  icon={<SendOutlined />}
                  onClick={handleReturnToStudent}
                >
                  返回给学生
                </Button>
              )}
            </Space>
          ) : null
        }
      >
        <Descriptions column={3} bordered>
          <Descriptions.Item label="学生">{submission.studentName}</Descriptions.Item>
          <Descriptions.Item label="提交时间">
            {dayjs(submission.submittedAt).format('YYYY-MM-DD HH:mm')}
          </Descriptions.Item>
          <Descriptions.Item label="状态">
            {getStatusTag(submission.status)}
          </Descriptions.Item>
          
          {submission.autoScore !== null && (
            <Descriptions.Item label="自动评分">
              <span style={{ color: '#1890ff', fontWeight: 600 }}>
                {submission.autoScore}分
              </span>
            </Descriptions.Item>
          )}
          {submission.manualScore !== null && (
            <Descriptions.Item label="人工评分">
              <span style={{ color: '#52c41a', fontWeight: 600 }}>
                {submission.manualScore}分
              </span>
            </Descriptions.Item>
          )}
          {submission.totalScore !== null && (
            <Descriptions.Item label="总分">
              <span className="score-display">{submission.totalScore}分</span>
            </Descriptions.Item>
          )}
        </Descriptions>

        {submission.teacherComments && (
          <div style={{ marginTop: 16, padding: 12, background: '#fafafa', borderRadius: 4 }}>
            <strong>教师评语：</strong>{submission.teacherComments}
          </div>
        )}
      </Card>

      {isTeacher() && (
        <Card title="批改进度" className="card-shadow" style={{ marginTop: 16 }}>
          <Steps
            current={
              submission.status === 'SUBMITTED' ? 3 :
              submission.status === 'AUTO_GRADING' ? 4 :
              submission.status === 'AUTO_GRADED' ? 5 :
              submission.status === 'MANUAL_GRADING' ? 5 :
              submission.status === 'GRADED' ? 6 :
              submission.status === 'RETURNED' ? 6 : 3
            }
            items={statusSteps}
          />
        </Card>
      )}

      {isTeacher() && (
        <Card title="分数统计" className="card-shadow" style={{ marginTop: 16 }}>
          <Row gutter={16}>
            <Col xs={6}>
              <Statistic
                title="自动评分"
                value={submission.autoScore || 0}
                suffix="分"
              />
            </Col>
            <Col xs={6}>
              <Statistic
                title="人工评分"
                value={submission.manualScore || 0}
                suffix="分"
              />
            </Col>
            <Col xs={6}>
              <Statistic
                title="总分"
                value={submission.totalScore || (submission.autoScore || 0) + (submission.manualScore || 0)}
                suffix="分"
                valueStyle={{ color: '#1890ff' }}
              />
            </Col>
            <Col xs={6}>
              <Statistic
                title="总分值"
                value={submission.answers.reduce((sum, a) => sum + (a.question?.score || 0), 0)}
                suffix="分"
              />
            </Col>
          </Row>
        </Card>
      )}

      <Card
        title="答题详情"
        className="card-shadow"
        style={{ marginTop: 16 }}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSaveGrading}
        >
          <List
            dataSource={submission.answers}
            renderItem={(answer, index) => (
              <List.Item className="question-card">
                <div style={{ width: '100%' }}>
                  <div style={{ marginBottom: 12, display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span className="question-number">第{answer.question.orderIndex}题</span>
                    <Tag>{getQuestionTypeText(answer.question.type)}</Tag>
                    <Tag color="blue">{answer.question.score}分</Tag>
                    {answer.question.autoGradable && <Tag color="cyan">可自动批改</Tag>}
                    {answer.isCorrect === true && <Tag color="green">正确</Tag>}
                    {answer.isCorrect === false && <Tag color="red">错误</Tag>}
                  </div>

                  <div style={{ marginBottom: 12, fontWeight: 500 }}>
                    {answer.question.content}
                  </div>

                  {answer.question.options && (
                    <div style={{ marginBottom: 12, whiteSpace: 'pre-wrap', color: '#666' }}>
                      {answer.question.options}
                    </div>
                  )}

                  <Divider style={{ margin: '12px 0' }} />

                  {getAnswerDisplay(answer)}

                  {isTeacher() && canManualGrade && !isGraded && (
                    <div style={{ marginTop: 16, padding: 12, background: '#fafafa', borderRadius: 4 }}>
                      <Row gutter={16}>
                        <Col xs={12} sm={6}>
                          <Form.Item
                            name={`score_${answer.id}`}
                            label="人工评分"
                            initialValue={answer.manualScore}
                          >
                            <InputNumber
                              min={0}
                              max={answer.question.score}
                              placeholder={answer.question.score}
                              style={{ width: '100%' }}
                            />
                          </Form.Item>
                        </Col>
                        <Col xs={12} sm={18}>
                          <Form.Item
                            name={`feedback_${answer.id}`}
                            label="评语"
                            initialValue={answer.teacherFeedback}
                          >
                            <TextArea
                              rows={2}
                              placeholder="请输入评语（可选）"
                            />
                          </Form.Item>
                        </Col>
                      </Row>
                    </div>
                  )}
                </div>
              </List.Item>
            )}
          />

          {isTeacher() && canManualGrade && !isGraded && (
            <div style={{ marginTop: 24, padding: 16, background: '#fafafa', borderRadius: 4 }}>
              <Form.Item
                name="teacherComments"
                label="总体评语"
                initialValue={submission.teacherComments}
              >
                <TextArea
                  rows={3}
                  placeholder="请输入总体评语（可选）"
                />
              </Form.Item>
            </div>
          )}
        </Form>
      </Card>
    </div>
  );
};

export default SubmissionDetail;
