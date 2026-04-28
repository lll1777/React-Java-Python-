import { create } from 'zustand';
import { persist } from 'zustand/middleware';

const mockStudents = [
  { id: 1, username: 'student1', realName: '张三', role: 'STUDENT', email: 'zhangsan@example.com' },
  { id: 2, username: 'student2', realName: '李四', role: 'STUDENT', email: 'lisi@example.com' },
];

const mockTeachers = [
  { id: 3, username: 'teacher1', realName: '王老师', role: 'TEACHER', email: 'wanglaoshi@example.com' },
];

const useUserStore = create(
  persist(
    (set, get) => ({
      user: null,
      loading: false,
      token: null,
      
      login: async (username, password) => {
        set({ loading: true });
        
        await new Promise((resolve) => setTimeout(resolve, 500));
        
        const allUsers = [...mockStudents, ...mockTeachers];
        const user = allUsers.find(u => u.username === username);
        
        if (user && password === '123456') {
          set({ user, loading: false, token: 'mock-token' });
          return { success: true };
        }
        
        set({ loading: false });
        return { success: false, message: '用户名或密码错误' };
      },
      
      logout: () => {
        set({ user: null, token: null });
        localStorage.removeItem('user-storage');
      },
      
      setUser: (user) => set({ user }),
      
      isTeacher: () => {
        const user = get().user;
        return user?.role === 'TEACHER' || user?.role === 'ADMIN';
      },
      
      isStudent: () => {
        const user = get().user;
        return user?.role === 'STUDENT';
      },
    }),
    {
      name: 'user-storage',
    }
  )
);

export { useUserStore };
