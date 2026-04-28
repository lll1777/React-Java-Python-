import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Layout, Spin } from 'antd';
import { useUserStore } from './store/userStore';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import AssignmentList from './pages/AssignmentList';
import AssignmentDetail from './pages/AssignmentDetail';
import AssignmentCreate from './pages/AssignmentCreate';
import SubmissionDetail from './pages/SubmissionDetail';
import WrongQuestionList from './pages/WrongQuestionList';
import LearningReportList from './pages/LearningReportList';
import LearningReportDetail from './pages/LearningReportDetail';
import Statistics from './pages/Statistics';
import AppHeader from './components/AppHeader';
import AppSider from './components/AppSider';

const { Content } = Layout;

function App() {
  const { user, loading } = useUserStore();

  if (loading) {
    return (
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!user) {
    return <Login />;
  }

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <AppSider />
      <Layout>
        <AppHeader />
        <Content
          style={{
            margin: '24px 16px',
            padding: 24,
            minHeight: 280,
            background: '#fff',
            borderRadius: 6,
          }}
        >
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<Dashboard />} />
            
            <Route path="/assignments" element={<AssignmentList />} />
            <Route path="/assignments/create" element={<AssignmentCreate />} />
            <Route path="/assignments/:id" element={<AssignmentDetail />} />
            <Route path="/assignments/:id/edit" element={<AssignmentCreate />} />
            
            <Route path="/submissions/:id" element={<SubmissionDetail />} />
            
            <Route path="/wrong-questions" element={<WrongQuestionList />} />
            
            <Route path="/reports" element={<LearningReportList />} />
            <Route path="/reports/:id" element={<LearningReportDetail />} />
            
            <Route path="/statistics" element={<Statistics />} />
            
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Content>
      </Layout>
    </Layout>
  );
}

export default App;
