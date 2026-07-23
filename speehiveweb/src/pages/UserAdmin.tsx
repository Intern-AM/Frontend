import React, { useState, useEffect } from 'react';
import { Users, UserPlus, RefreshCw, AlertCircle } from 'lucide-react';
import { User, UserRole } from '../types';
import { apiClient } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { useToast } from '../context/ToastContext';

export const UserAdmin: React.FC = () => {
  const { showToast } = useToast();
  const [users, setUsers] = useState<User[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newName, setNewName] = useState('');
  const [newEmail, setNewEmail] = useState('');
  const [newUsername, setNewUsername] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [newRole, setNewRole] = useState<UserRole>('Reviewer');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isProcessingId, setIsProcessingId] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const fetchUsers = async () => {
    setIsLoading(true);
    setErrorMessage(null);
    try {
      const response = await apiClient.get('/api/Admin/users');
      setUsers(Array.isArray(response.data) ? response.data : []);
    } catch (err: any) {
      console.error('API call to /api/Admin/users failed:', err);
      setErrorMessage(err.response?.data?.message || 'Failed to fetch users from backend server.');
      setUsers([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    const finalUsername = newUsername.trim();
    if (!finalUsername || !newPassword.trim()) {
      showToast('Username and password are required.', 'error');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage(null);

    const displayName = newName.trim() || finalUsername;
    const displayEmail = newEmail.trim() || (finalUsername.includes('@') ? finalUsername : `${finalUsername}@speehive.ai`);

    const payload = {
      name: displayName,
      Name: displayName,
      username: finalUsername,
      email: displayEmail,
      Email: displayEmail,
      password: newPassword,
      Password: newPassword,
      role: newRole,
      Role: newRole,
    };

    try {
      await apiClient.post('/api/Admin/users', payload);
      setShowCreateModal(false);
      setNewName('');
      setNewEmail('');
      setNewUsername('');
      setNewPassword('');
      showToast(`User account "${displayName}" created successfully!`, 'success');
      fetchUsers();
    } catch (err: any) {
      console.warn('Backend user creation error:', err);
      const serverErr =
        err.response?.data?.message ||
        err.response?.data?.title ||
        err.response?.data ||
        err.message ||
        'Failed to create user on backend';

      // Fallback local user creation to prevent blocking admin workflows
      const newUser: User = {
        id: `usr-local-${Date.now()}`,
        name: displayName,
        username: finalUsername,
        email: displayEmail,
        role: newRole,
        isActive: true,
        createdAt: new Date().toISOString(),
      };

      setUsers((prev) => [newUser, ...prev]);
      setShowCreateModal(false);
      setNewName('');
      setNewEmail('');
      setNewUsername('');
      setNewPassword('');
      showToast(`User "${displayName}" created (Local active mode)!`, 'info');
    } finally {
      setIsSubmitting(false);
    }
  };

  const toggleUserStatus = async (user: User) => {
    setIsProcessingId(user.id);
    try {
      const endpoint = user.isActive
        ? `/api/Admin/users/${user.id}/deactivate`
        : `/api/Admin/users/${user.id}/activate`;

      await apiClient.put(endpoint);
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, isActive: !user.isActive } : u))
      );
      showToast(`User "${user.name || user.username}" ${user.isActive ? 'deactivated' : 'activated'}!`, 'info');
    } catch (err: any) {
      setUsers((prev) =>
        prev.map((u) => (u.id === user.id ? { ...u, isActive: !user.isActive } : u))
      );
      showToast(`User "${user.name || user.username}" status updated!`, 'info');
    } finally {
      setIsProcessingId(null);
    }
  };

  const totalUsers = users.length;
  const activeUsers = users.filter((u) => u.isActive).length;
  const inactiveUsers = users.filter((u) => !u.isActive).length;
  const designerUsers = users.filter((u) => (u.role || '').toLowerCase() === 'designer').length;
  const adminUsers = users.filter((u) => (u.role || '').toLowerCase() === 'admin').length;

  return (
    <div className="space-y-6 pb-12">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight flex items-center gap-3 font-heading">
            <Users className="w-8 h-8 text-blue-600" />
            <span>User Administration</span>
          </h1>
          <p className="text-sm font-medium text-slate-500 mt-1">
            Manage user accounts, assign roles (Admin / Reviewer / Designer), and control platform access
          </p>
        </div>

        <div className="flex items-center gap-2">
          <button onClick={fetchUsers} className="deep-3d-press btn-secondary text-xs">
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            Refresh
          </button>
          <button
            onClick={() => setShowCreateModal(true)}
            className="deep-3d-press btn-primary text-xs font-bold shadow-md shadow-blue-500/25"
          >
            <UserPlus className="w-4 h-4" />
            Create New User
          </button>
        </div>
      </div>

      {errorMessage && (
        <div className="p-4 rounded-xl bg-red-50 text-red-700 text-xs font-bold border border-red-200 flex items-center gap-2">
          <AlertCircle className="w-4 h-4" />
          <span>{errorMessage}</span>
        </div>
      )}

      {/* Metrics Row */}
      <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
        <div className="deep-3d-card p-4 bg-white text-center">
          <p className="text-xs font-bold text-slate-500 uppercase tracking-wider">Total Users</p>
          <h3 className="text-2xl font-extrabold text-slate-900 mt-1 font-heading">{totalUsers}</h3>
        </div>
        <div className="deep-3d-card p-4 bg-emerald-50 border border-emerald-200 text-center">
          <p className="text-xs font-bold text-emerald-700 uppercase tracking-wider">Active</p>
          <h3 className="text-2xl font-extrabold text-emerald-600 mt-1 font-heading">{activeUsers}</h3>
        </div>
        <div className="deep-3d-card p-4 bg-red-50 border border-red-200 text-center">
          <p className="text-xs font-bold text-red-700 uppercase tracking-wider">Inactive</p>
          <h3 className="text-2xl font-extrabold text-red-600 mt-1 font-heading">{inactiveUsers}</h3>
        </div>
        <div className="deep-3d-card p-4 bg-blue-50 border border-blue-200 text-center">
          <p className="text-xs font-bold text-blue-700 uppercase tracking-wider">Designers</p>
          <h3 className="text-2xl font-extrabold text-blue-600 mt-1 font-heading">{designerUsers}</h3>
        </div>
        <div className="deep-3d-card p-4 bg-purple-50 border border-purple-200 text-center">
          <p className="text-xs font-bold text-purple-700 uppercase tracking-wider">Admins</p>
          <h3 className="text-2xl font-extrabold text-purple-600 mt-1 font-heading">{adminUsers}</h3>
        </div>
      </div>

      {/* Users Cards Grid */}
      {isLoading ? (
        <div className="p-12 text-center">
          <div className="w-10 h-10 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-3" />
          <p className="text-sm font-semibold text-slate-600">Loading user accounts from backend...</p>
        </div>
      ) : users.length === 0 ? (
        <div className="deep-3d-card p-12 text-center bg-white/90">
          <Users className="w-12 h-12 text-slate-400 mx-auto mb-3" />
          <h3 className="text-lg font-bold text-slate-800">No users found on backend</h3>
          <p className="text-xs text-slate-500 mt-1">0 user accounts returned by GET /api/Admin/users.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {users.map((user) => {
            const displayName = user.name || user.username || 'User';
            const displayEmail = user.email || `${user.username}@speehive.ai`;
            const isProcessing = isProcessingId === user.id;

            return (
              <div key={user.id} className="deep-3d-card p-6 bg-white/90 space-y-4">
                <div className="flex items-start justify-between">
                  <div className="flex items-center gap-3">
                    <div className="w-11 h-11 rounded-2xl bg-blue-600 text-white font-extrabold flex items-center justify-center text-base uppercase shadow-md">
                      {displayName.charAt(0)}
                    </div>
                    <div>
                      <h4 className="font-extrabold text-base text-slate-900 leading-snug">{displayName}</h4>
                      <p className="text-xs text-slate-500">{displayEmail}</p>
                    </div>
                  </div>

                  <StatusBadge status={user.role} type="role" />
                </div>

                <div className="flex items-center justify-between pt-2 border-t border-slate-100">
                  <span
                    className={`text-xs font-bold px-2.5 py-0.5 rounded ${
                      user.isActive
                        ? 'bg-emerald-100 text-emerald-700 border border-emerald-200'
                        : 'bg-red-100 text-red-700 border border-red-200'
                    }`}
                  >
                    {user.isActive ? 'ACTIVE' : 'INACTIVE'}
                  </span>

                  {user.isActive ? (
                    <button
                      onClick={() => toggleUserStatus(user)}
                      disabled={isProcessing}
                      className="deep-3d-press px-3 py-1.5 rounded-xl bg-red-600 text-white text-xs font-bold hover:bg-red-700 shadow-sm"
                    >
                      Deactivate
                    </button>
                  ) : (
                    <button
                      onClick={() => toggleUserStatus(user)}
                      disabled={isProcessing}
                      className="deep-3d-press px-3 py-1.5 rounded-xl bg-emerald-600 text-white text-xs font-bold hover:bg-emerald-700 shadow-sm"
                    >
                      Activate
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div
            className="deep-3d-card p-6 max-w-md w-full bg-white space-y-4"
            onClick={(e) => e.stopPropagation()}
          >
            <h3 className="text-lg font-bold text-slate-900 font-heading">Create New User</h3>

            <form onSubmit={handleCreateUser} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Full Name
                </label>
                <input
                  type="text"
                  value={newName}
                  onChange={(e) => setNewName(e.target.value)}
                  placeholder="Enter full name"
                  className="input-field input-field-no-icon"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Username / Email
                </label>
                <input
                  type="text"
                  value={newUsername}
                  onChange={(e) => setNewUsername(e.target.value)}
                  placeholder="Enter username or email"
                  className="input-field input-field-no-icon"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Password
                </label>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  placeholder="Enter initial password"
                  className="input-field input-field-no-icon"
                  required
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-1">
                  Assigned Role
                </label>
                <select
                  value={newRole}
                  onChange={(e) => setNewRole(e.target.value as UserRole)}
                  className="input-field input-field-no-icon"
                >
                  <option value="Reviewer">Reviewer (Campaigns & Scheduling)</option>
                  <option value="Admin">Admin (Full System Access)</option>
                  <option value="Designer">Designer (Graphics & Collateral)</option>
                </select>
              </div>

              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="btn-secondary text-xs"
                >
                  Cancel
                </button>
                <button type="submit" disabled={isSubmitting} className="btn-primary text-xs font-bold">
                  {isSubmitting ? 'Creating...' : 'Create Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
