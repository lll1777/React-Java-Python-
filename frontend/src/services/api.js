import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const PYTHON_API_URL = process.env.REACT_APP_PYTHON_API_URL || 'http://localhost:5000';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

const pythonApi = axios.create({
  baseURL: PYTHON_API_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const assignmentApi = {
  create: (data, creatorId) => 
    api.post(`/api/assignments?creatorId=${creatorId}`, data),
  
  getById: (id) => 
    api.get(`/api/assignments/${id}`),
  
  getByClassId: (classId) => 
    api.get(`/api/assignments/class/${classId}`),
  
  getByStudentId: (studentId) => 
    api.get(`/api/assignments/student/${studentId}`),
  
  getByCreatorId: (creatorId) => 
    api.get(`/api/assignments/creator/${creatorId}`),
  
  publish: (id) => 
    api.post(`/api/assignments/${id}/publish`),
  
  open: (id) => 
    api.post(`/api/assignments/${id}/open`),
  
  archive: (id) => 
    api.post(`/api/assignments/${id}/archive`),
  
  update: (id, data) => 
    api.put(`/api/assignments/${id}`, data),
  
  delete: (id) => 
    api.delete(`/api/assignments/${id}`),
};

export const submissionApi = {
  submit: (data, studentId) => 
    api.post(`/api/submissions?studentId=${studentId}`, data),
  
  getById: (id) => 
    api.get(`/api/submissions/${id}`),
  
  getByAssignmentId: (assignmentId) => 
    api.get(`/api/submissions/assignment/${assignmentId}`),
  
  getByStudentId: (studentId) => 
    api.get(`/api/submissions/student/${studentId}`),
  
  getGradedByStudentId: (studentId) => 
    api.get(`/api/submissions/student/${studentId}/graded`),
  
  getByAssignmentAndStudent: (assignmentId, studentId) => 
    api.get(`/api/submissions/assignment/${assignmentId}/student/${studentId}`),
  
  returnSubmission: (id) => 
    api.post(`/api/submissions/${id}/return`),
  
  getStatistics: (assignmentId) => 
    api.get(`/api/submissions/assignment/${assignmentId}/statistics`),
};

export const gradingApi = {
  startAutoGrading: (submissionId) => 
    api.post(`/api/grading/auto/start/${submissionId}`),
  
  performAutoGrading: (submissionId) => 
    api.post(`/api/grading/auto/perform/${submissionId}`),
  
  startManualGrading: (submissionId) => 
    api.post(`/api/grading/manual/start/${submissionId}`),
  
  performManualGrading: (submissionId, data) => 
    api.post(`/api/grading/manual/perform/${submissionId}`, data),
  
  completeGrading: (submissionId) => 
    api.post(`/api/grading/complete/${submissionId}`),
};

export const pythonGradingApi = {
  autoGrade: (submissionId, data) => 
    pythonApi.post(`/api/auto-grade/${submissionId}`, data),
  
  batchGrade: (data) => 
    pythonApi.post('/api/auto-grade/batch', data),
  
  analyzeKnowledge: (studentId, data) => 
    pythonApi.post(`/api/knowledge-analysis/${studentId}`, data),
  
  compareKnowledge: (data) => 
    pythonApi.post('/api/knowledge-analysis/compare', data),
  
  gradeSingleChoice: (data) => 
    pythonApi.post('/api/grade/single-choice', data),
  
  gradeMultipleChoice: (data) => 
    pythonApi.post('/api/grade/multiple-choice', data),
  
  gradeTrueFalse: (data) => 
    pythonApi.post('/api/grade/true-false', data),
  
  gradeFillBlank: (data) => 
    pythonApi.post('/api/grade/fill-blank', data),
};

export const statisticsApi = {
  getAssignmentStatistics: (assignmentId) => 
    api.get(`/api/statistics/assignment/${assignmentId}`),
  
  getStudentStatistics: (studentId) => 
    api.get(`/api/statistics/student/${studentId}`),
  
  getClassStatistics: (classId) => 
    api.get(`/api/statistics/class/${classId}`),
  
  getKnowledgePointStatistics: (studentId) => 
    api.get(`/api/statistics/knowledge-points/${studentId}`),
};

export const wrongQuestionApi = {
  getByStudentId: (studentId) => 
    api.get(`/api/wrong-questions/student/${studentId}`),
  
  getUnresolved: (studentId) => 
    api.get(`/api/wrong-questions/student/${studentId}/unresolved`),
  
  getResolved: (studentId) => 
    api.get(`/api/wrong-questions/student/${studentId}/resolved`),
  
  getById: (id) => 
    api.get(`/api/wrong-questions/${id}`),
  
  markAsResolved: (id, notes) => 
    api.post(`/api/wrong-questions/${id}/resolve`, notes),
  
  updateNotes: (id, notes) => 
    api.put(`/api/wrong-questions/${id}/notes`, notes),
  
  delete: (id) => 
    api.delete(`/api/wrong-questions/${id}`),
  
  countUnresolved: (studentId) => 
    api.get(`/api/wrong-questions/student/${studentId}/count/unresolved`),
  
  countResolved: (studentId) => 
    api.get(`/api/wrong-questions/student/${studentId}/count/resolved`),
};

export const learningReportApi = {
  generate: (studentId, reportType) => 
    api.post(`/api/reports/generate/${studentId}?reportType=${reportType}`),
  
  getByStudentId: (studentId) => 
    api.get(`/api/reports/student/${studentId}`),
  
  getById: (id) => 
    api.get(`/api/reports/${id}`),
  
  getLatest: (studentId) => 
    api.get(`/api/reports/student/${studentId}/latest`),
  
  delete: (id) => 
    api.delete(`/api/reports/${id}`),
};

export default {
  assignment: assignmentApi,
  submission: submissionApi,
  grading: gradingApi,
  pythonGrading: pythonGradingApi,
  statistics: statisticsApi,
  wrongQuestion: wrongQuestionApi,
  learningReport: learningReportApi,
};
