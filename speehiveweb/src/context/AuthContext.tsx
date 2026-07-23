import React, { createContext, useContext, useState, useEffect } from 'react';
import { UserRole } from '../types';

interface AuthContextType {
  token: string | null;
  username: string | null;
  role: UserRole | null;
  isAuthenticated: boolean;
  login: (token: string, username: string, role: UserRole) => void;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(localStorage.getItem('hive_auth_token'));
  const [username, setUsername] = useState<string | null>(localStorage.getItem('hive_auth_user'));
  const [role, setRole] = useState<UserRole | null>(
    (localStorage.getItem('hive_auth_role') as UserRole) || null
  );

  useEffect(() => {
    if (token) {
      localStorage.setItem('hive_auth_token', token);
    } else {
      localStorage.removeItem('hive_auth_token');
    }
  }, [token]);

  const login = (newToken: string, newUsername: string, newRole: UserRole) => {
    setToken(newToken);
    setUsername(newUsername);
    setRole(newRole);
    localStorage.setItem('hive_auth_token', newToken);
    localStorage.setItem('hive_auth_user', newUsername);
    localStorage.setItem('hive_auth_role', newRole);
  };

  const logout = () => {
    setToken(null);
    setUsername(null);
    setRole(null);
    localStorage.removeItem('hive_auth_token');
    localStorage.removeItem('hive_auth_user');
    localStorage.removeItem('hive_auth_role');
  };

  return (
    <AuthContext.Provider
      value={{
        token,
        username,
        role,
        isAuthenticated: !!token,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
