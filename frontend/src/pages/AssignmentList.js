import React, { useState, useEffect } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Tag,
  Select,
  Input,
  DatePicker,
  Empty,
  Spin,
  Modal,
  message,
} from 'antd';
import {
  PlusOutlined,
  SearchOutlined,
  EditOutlined,
  DeleteOutlined,
  EyeOutlined,
  FileTextOutlined,
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../store/userStore';
import dayjs from 'dayjs';

const { RangePicker } = DatePicker;
const { Option } = Select;

const AssignmentList = () => {
  const navigate = useNavigate();
  const { user, isTeacher } = useUserStore();
  const [loading, setLoading] = useState(false);
  const [assignments, setAssignments] = useState([]);
  const [filteredAssignments, setFilteredAssignments] = useState([]);
  const [deleteModalVisible, setDeleteModalVisible] = useState(false);
  const [deletingAssignment, setDeletingAssignment] = useState(null);
  const [filters, setFilters] = useState({
    status: '',
    keyword: '',
  });

  const mockAssignments = [
    {
      id: 1,
      title: '高等数学 - 第三章练习',
      description: '函数极限与连续性练习',
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
    },
    {
      id: 2,
      title: '线性代数 - 矩阵运算',
      description: '矩阵的加法、乘法、转置等运算',
      classId: 1,
      className: '高数1班',
      creatorId: 3,
      creatorName: '王老师',
      status: 'SUBMITTED',
      publishTime: '2024-01-08 09:00',
      deadline: '2024-01-15 23:59',
      totalScore: 100,
      submittedCount: 42,
      totalStudents: 45,
    },
    {
      id: 3,
      title: '概率论 - 条件概率',
      description: '条件概率与独立事件',
      classId: 1,
      className: '高数1班',
      creatorId: 3,
      creatorName: '王老师',
      status: 'GRADED',
      publishTime: '2024-01-05 09:00',
      deadline: '2024-01-12 23:59',
      totalScore: 100,
      submittedCount: 45,
      totalStudents: 45,
      averageScore: 78.5,
    },
    {
      id: 4,
      title: '微积分 - 导数应用',
      description: '导数在极值问题中的应用',
      classId: 1,
      className: '高数1班',
      creatorId: 3,
      creatorName: '王老师',
      status: 'DRAFT',
      publishTime: null,
      deadline: '2024-01-25 23:59',
      totalScore: 100,
      submittedCount: 0,
      totalStudents: 45,
    },
  ];

  useEffect(() => {
    setLoading(true);
    setTimeout(() => {
      setAssignments(mockAssignments);
      setFilteredAssignments(mockAssignments);
      setLoading(false);
    }, 500);
  }, []);

  useEffect(() => {
    let result = [...assignments];
    
    if (filters.status) {
      result = result.filter(a => a.status === filters.status);
    }
    
    if (filters.keyword) {
      result = result.filter(a => 
        a.title.toLowerCase().includes(filters.keyword.toLowerCase()) ||
        a.description.toLowerCase().includes(filters.keyword.toLowerCase())
      );
    }
    
    setFilteredAssignments(result);
  }, [filters, assignments]);

  const getStatusTag = (status) => {
    const statusMap = {
      DRAFT: { color: 'default', text: '草稿', icon: '📝' },
      PUBLISHED: { color: 'blue', text: '已发布', icon: '📢' },
      OPEN: { color: 'green', text: '进行中', icon: '⏳' },
      SUBMITTED: { color: 'orange', text: '已提交', icon: '📨' },
      AUTO_GRADING: { color: 'processing', text: '自动批改中', icon: '⚙️' },
      AUTO_GRADED: { color: 'cyan', text: '自动批改完成', icon: '✅' },
      MANUAL_GRADING: { color: 'processing', text: '人工批改中', icon: '✏️' },
      GRADED: { color: 'success', text: '已批改', icon: '🎯' },
      RETURNED: { color: 'purple', text: '已返回', icon: '📬' },
      ARCHIVED: { color: 'default', text: '已归档', icon: '📦' },
    };
    const info = statusMap[status] || { color: 'default', text: status, icon: '📄' };
    return (
      <Tag color={info.color}>
        {info.icon} {info.text}
      </Tag>
    );
  };

  const handleDelete = () => {
    if (deletingAssignment) {
      setAssignments(assignments.filter(a => a.id !== deletingAssignment.id));
      setFilteredAssignments(filteredAssignments.filter(a => a.id !== deletingAssignment.id));
      message.success('作业已删除');
      setDeleteModalVisible(false);
      setDeletingAssignment(null);
    }
  };

  const columns = [
    {
      title: '作业标题',
      dataIndex: 'title',
      key: 'title',
      render: (text, record) => (
        <a onClick={() => navigate(`/assignments/${record.id}`)}>
          <FileTextOutlined style={{ marginRight: 8 }} />
          {text}
        </a>
      ),
    },
    {
      title: '班级',
      dataIndex: 'className',
      key: 'className',
    },
    {
      title: '状态',
      dataIndex: 'status',
      key: 'status',
      render: (status) => getStatusTag(status),
    },
    {
      title: '截止时间',
      dataIndex: 'deadline',
      key: 'deadline',
      render: (deadline) => dayjs(deadline).format('YYYY-MM-DD HH:mm'),
    },
    {
      title: '总分',
      dataIndex: 'totalScore',
      key: 'totalScore',
      render: (score) => <span style={{ fontWeight: 600 }}>{score}分</span>,
    },
    ...(isTeacher() ? [
      {
        title: '提交情况',
        dataIndex: 'submittedCount',
        key: 'submission',
        render: (count, record) => (
          <span>
            {count}/{record.totalStudents} 人
            <span style={{ color: '#999', marginLeft: 4 }}>
              ({Math.round(count / record.totalStudents * 100)}%)
            </span>
          </span>
        ),
      },
      {
        title: '平均分',
        dataIndex: 'averageScore',
        key: 'averageScore',
        render: (score) => score ? `${score}分` : '-',
      },
    ] : []),
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            type="link"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/assignments/${record.id}`)}
          >
            查看
          </Button>
          {isTeacher() && (
            <>
              {record.status === 'DRAFT' && (
                <Button
                  type="link"
                  icon={<EditOutlined />}
                  onClick={() => navigate(`/assignments/${record.id}/edit`)}
                >
                  编辑
                </Button>
              )}
              {record.status === 'DRAFT' && (
                <Button
                  type="link"
                  danger
                  icon={<DeleteOutlined />}
                  onClick={() => {
                    setDeletingAssignment(record);
                    setDeleteModalVisible(true);
                  }}
                >
                  删除
                </Button>
              )}
            </>
          )}
        </Space>
      ),
    },
  ];

  return (
    <div>
      <Card
        title={isTeacher() ? '作业管理' : '我的作业'}
        className="card-shadow"
        extra={
          isTeacher() && (
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={() => navigate('/assignments/create')}
            >
              发布新作业
            </Button>
          )
        }
      >
        <div style={{ marginBottom: 16, display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          <Input
            placeholder="搜索作业标题或描述"
            prefix={<SearchOutlined />}
            style={{ width: 250 }}
            value={filters.keyword}
            onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            allowClear
          />
          <Select
            placeholder="筛选状态"
            style={{ width: 150 }}
            value={filters.status || undefined}
            onChange={(value) => setFilters({ ...filters, status: value })}
            allowClear
          >
            <Option value="DRAFT">草稿</Option>
            <Option value="PUBLISHED">已发布</Option>
            <Option value="OPEN">进行中</Option>
            <Option value="SUBMITTED">已提交</Option>
            <Option value="GRADED">已批改</Option>
          </Select>
          {isTeacher() && (
            <RangePicker style={{ width: 250 }} />
          )}
        </div>

        <Spin spinning={loading}>
          {filteredAssignments.length > 0 ? (
            <Table
              columns={columns}
              dataSource={filteredAssignments}
              rowKey="id"
              pagination={{
                showSizeChanger: true,
                showQuickJumper: true,
                showTotal: (total) => `共 ${total} 条记录`,
              }}
            />
          ) : (
            <Empty description="暂无作业数据" />
          )}
        </Spin>
      </Card>

      <Modal
        title="确认删除"
        open={deleteModalVisible}
        onOk={handleDelete}
        onCancel={() => {
          setDeleteModalVisible(false);
          setDeletingAssignment(null);
        }}
        okText="确认删除"
        cancelText="取消"
        okButtonProps={{ danger: true }}
      >
        <p>确定要删除作业「{deletingAssignment?.title}」吗？</p>
        <p style={{ color: '#ff4d4f' }}>此操作不可撤销！</p>
      </Modal>
    </div>
  );
};

export default AssignmentList;
