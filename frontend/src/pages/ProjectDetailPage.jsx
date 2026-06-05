import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { getProject, getMembers, inviteMember, removeMember, changeRole } from '../api/projects';
import { getTasks, createTask, updateTask, updateTaskStatus, deleteTask } from '../api/tasks';
import { useAuth } from '../context/AuthContext';
import Modal from '../components/common/Modal';
import Spinner from '../components/common/Spinner';
import EmptyState from '../components/common/EmptyState';

function ConfirmModal({ title, message, confirmLabel = 'Delete', onConfirm, onCancel }) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="bg-white rounded-xl shadow-xl w-full max-w-sm mx-4">
        <div className="px-6 py-5">
          <h2 className="text-base font-semibold text-gray-800 mb-1">{title}</h2>
          <p className="text-sm text-gray-500">{message}</p>
        </div>
        <div className="flex justify-end gap-2 px-6 pb-5">
          <button onClick={onCancel}
            className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800 border border-gray-200 rounded-lg">
            Cancel
          </button>
          <button onClick={onConfirm}
            className="px-4 py-2 text-sm bg-red-600 hover:bg-red-700 text-white rounded-lg">
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

const STATUS_COLUMNS = [
  { key: 'TODO', label: 'To Do', color: 'bg-gray-100 text-gray-700' },
  { key: 'IN_PROGRESS', label: 'In Progress', color: 'bg-blue-100 text-blue-700' },
  { key: 'DONE', label: 'Done', color: 'bg-green-100 text-green-700' },
];

function TaskCard({ task, isAdmin, currentUserId, onStatusChange, onEdit, onDelete }) {
  const isOverdue = task.overdue;
  const canChangeStatus = isAdmin || task.assigneeId === currentUserId;

  return (
    <div className={`bg-white rounded-lg border p-3 shadow-sm mb-2 ${isOverdue ? 'border-l-4 border-l-red-500' : 'border-gray-200'}`}>
      <div className="flex items-start justify-between gap-2">
        <p className="text-sm font-medium text-gray-800 flex-1">{task.title}</p>
        {isAdmin && (
          <div className="flex gap-1 shrink-0">
            <button onClick={() => onEdit(task)} className="text-gray-400 hover:text-indigo-600 text-xs">✏️</button>
            <button onClick={() => onDelete(task.id)} className="text-gray-400 hover:text-red-500 text-xs">🗑️</button>
          </div>
        )}
      </div>
      {task.description && <p className="text-xs text-gray-500 mt-1 line-clamp-2">{task.description}</p>}
      {task.assigneeName && (
        <div className="mt-2 flex items-center gap-1">
          <div className="w-5 h-5 rounded-full bg-indigo-200 flex items-center justify-center text-xs font-bold text-indigo-700">
            {task.assigneeName[0].toUpperCase()}
          </div>
          <span className="text-xs text-gray-500">{task.assigneeName}</span>
        </div>
      )}
      {task.dueDate && (
        <p className={`text-xs mt-1 ${isOverdue ? 'text-red-500 font-medium' : 'text-gray-400'}`}>
          Due: {new Date(task.dueDate).toLocaleDateString()}
          {isOverdue && ' ⚠️ Overdue'}
        </p>
      )}
      {canChangeStatus && (
        <select
          value={task.status}
          onChange={(e) => onStatusChange(task.id, e.target.value)}
          className="mt-2 text-xs border border-gray-200 rounded px-1 py-0.5 w-full focus:outline-none focus:ring-1 focus:ring-indigo-400"
        >
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
      )}
    </div>
  );
}

export default function ProjectDetailPage() {
  const { projectId } = useParams();
  const { user } = useAuth();
  const [project, setProject] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [members, setMembers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('tasks');

  const [showTaskModal, setShowTaskModal] = useState(false);
  const [editingTask, setEditingTask] = useState(null);
  const [taskForm, setTaskForm] = useState({ title: '', description: '', status: 'TODO', assigneeId: '', dueDate: '' });
  const [taskError, setTaskError] = useState('');
  const [savingTask, setSavingTask] = useState(false);

  const [showInviteModal, setShowInviteModal] = useState(false);
  const [inviteForm, setInviteForm] = useState({ email: '', role: 'MEMBER' });
  const [inviteError, setInviteError] = useState('');
  const [inviting, setInviting] = useState(false);

  const [confirmModal, setConfirmModal] = useState(null);

  const myMembership = members.find((m) => m.userId === user?.id);
  const isAdmin = myMembership?.role === 'ADMIN';

  const load = async () => {
    try {
      const [proj, taskList, memberList] = await Promise.all([
        getProject(projectId),
        getTasks(projectId),
        getMembers(projectId),
      ]);
      setProject(proj.data);
      setTasks(taskList.data);
      setMembers(memberList.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, [projectId]);

  const handleStatusChange = async (taskId, status) => {
    try {
      await updateTaskStatus(projectId, taskId, { status });
      setTasks((prev) => prev.map((t) => t.id === taskId ? { ...t, status } : t));
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update status');
    }
  };

  const openCreateTask = () => {
    setEditingTask(null);
    setTaskForm({ title: '', description: '', status: 'TODO', assigneeId: '', dueDate: '' });
    setTaskError('');
    setShowTaskModal(true);
  };

  const openEditTask = (task) => {
    setEditingTask(task);
    setTaskForm({
      title: task.title,
      description: task.description || '',
      status: task.status,
      assigneeId: task.assigneeId || '',
      dueDate: task.dueDate ? task.dueDate.substring(0, 16) : '',
    });
    setTaskError('');
    setShowTaskModal(true);
  };

  const handleSaveTask = async (e) => {
    e.preventDefault();
    setSavingTask(true);
    setTaskError('');
    const payload = {
      title: taskForm.title,
      description: taskForm.description || null,
      status: taskForm.status,
      assigneeId: taskForm.assigneeId || null,
      dueDate: taskForm.dueDate || null,
    };
    try {
      if (editingTask) {
        await updateTask(projectId, editingTask.id, payload);
      } else {
        await createTask(projectId, payload);
      }
      setShowTaskModal(false);
      load();
    } catch (err) {
      setTaskError(err.response?.data?.message || 'Failed to save task');
    } finally {
      setSavingTask(false);
    }
  };

  const handleDeleteTask = (taskId) => {
    setConfirmModal({
      title: 'Delete Task',
      message: 'Are you sure you want to delete this task? This action cannot be undone.',
      confirmLabel: 'Delete',
      onConfirm: async () => {
        setConfirmModal(null);
        try {
          await deleteTask(projectId, taskId);
          setTasks((prev) => prev.filter((t) => t.id !== taskId));
        } catch (err) {
          alert(err.response?.data?.message || 'Failed to delete task');
        }
      },
    });
  };

  const handleInvite = async (e) => {
    e.preventDefault();
    setInviting(true);
    setInviteError('');
    try {
      await inviteMember(projectId, inviteForm);
      setShowInviteModal(false);
      setInviteForm({ email: '', role: 'MEMBER' });
      load();
    } catch (err) {
      setInviteError(err.response?.data?.message || 'Failed to invite member');
    } finally {
      setInviting(false);
    }
  };

  const handleRemoveMember = (userId) => {
    const member = members.find((m) => m.userId === userId);
    setConfirmModal({
      title: 'Remove Member',
      message: `Are you sure you want to remove ${member?.name ?? 'this member'} from the project?`,
      confirmLabel: 'Remove',
      onConfirm: async () => {
        setConfirmModal(null);
        try {
          await removeMember(projectId, userId);
          load();
        } catch (err) {
          alert(err.response?.data?.message || 'Failed to remove member');
        }
      },
    });
  };

  const handleChangeRole = async (userId, role) => {
    try {
      await changeRole(projectId, userId, { role });
      load();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to change role');
    }
  };

  if (loading) return <Spinner />;

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-gray-800">{project?.name}</h1>
        {project?.description && <p className="text-sm text-gray-500 mt-1">{project.description}</p>}
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-gray-200 mb-6">
        {['tasks', 'members'].map((t) => (
          <button key={t} onClick={() => setTab(t)}
            className={`px-4 py-2 text-sm font-medium capitalize border-b-2 transition ${tab === t ? 'border-indigo-600 text-indigo-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}>
            {t}
          </button>
        ))}
      </div>

      {/* Tasks Tab */}
      {tab === 'tasks' && (
        <>
          {isAdmin && (
            <div className="mb-4">
              <button onClick={openCreateTask}
                className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition">
                + Add Task
              </button>
            </div>
          )}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            {STATUS_COLUMNS.map(({ key, label, color }) => {
              const colTasks = tasks.filter((t) => t.status === key);
              return (
                <div key={key} className="bg-gray-50 rounded-xl p-4">
                  <div className="flex items-center gap-2 mb-3">
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${color}`}>{label}</span>
                    <span className="text-xs text-gray-400">{colTasks.length}</span>
                  </div>
                  {colTasks.length === 0 && <p className="text-xs text-gray-400 text-center py-4">No tasks</p>}
                  {colTasks.map((task) => (
                    <TaskCard
                      key={task.id}
                      task={task}
                      isAdmin={isAdmin}
                      currentUserId={user?.id}
                      onStatusChange={handleStatusChange}
                      onEdit={openEditTask}
                      onDelete={handleDeleteTask}
                    />
                  ))}
                </div>
              );
            })}
          </div>
        </>
      )}

      {/* Members Tab */}
      {tab === 'members' && (
        <>
          {isAdmin && (
            <div className="mb-4">
              <button onClick={() => setShowInviteModal(true)}
                className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition">
                + Invite Member
              </button>
            </div>
          )}
          {members.length === 0 ? <EmptyState message="No members yet" /> : (
            <div className="bg-white rounded-xl border border-gray-200 divide-y">
              {members.map((m) => (
                <div key={m.userId} className="flex items-center justify-between px-4 py-3">
                  <div>
                    <p className="text-sm font-medium text-gray-800">{m.name}</p>
                    <p className="text-xs text-gray-400">{m.email}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    {isAdmin && m.userId !== user?.id ? (
                      <select value={m.role} onChange={(e) => handleChangeRole(m.userId, e.target.value)}
                        className="text-xs border border-gray-200 rounded px-2 py-1 focus:outline-none focus:ring-1 focus:ring-indigo-400">
                        <option value="ADMIN">Admin</option>
                        <option value="MEMBER">Member</option>
                      </select>
                    ) : (
                      <span className={`text-xs font-medium px-2 py-0.5 rounded-full ${m.role === 'ADMIN' ? 'bg-indigo-100 text-indigo-700' : 'bg-gray-100 text-gray-600'}`}>
                        {m.role}
                      </span>
                    )}
                    {isAdmin && m.userId !== user?.id && (
                      <button onClick={() => handleRemoveMember(m.userId)}
                        className="text-xs text-red-400 hover:text-red-600">Remove</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </>
      )}

      {/* Task Modal */}
      {showTaskModal && (
        <Modal title={editingTask ? 'Edit Task' : 'New Task'} onClose={() => setShowTaskModal(false)}>
          {taskError && <div className="mb-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded p-2">{taskError}</div>}
          <form onSubmit={handleSaveTask} className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Title *</label>
              <input type="text" required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                value={taskForm.title} onChange={(e) => setTaskForm({ ...taskForm, title: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
              <textarea rows={2}
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                value={taskForm.description} onChange={(e) => setTaskForm({ ...taskForm, description: e.target.value })} />
            </div>
            <div className="grid grid-cols-2 gap-3">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
                <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  value={taskForm.status} onChange={(e) => setTaskForm({ ...taskForm, status: e.target.value })}>
                  <option value="TODO">To Do</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Assignee</label>
                <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  value={taskForm.assigneeId} onChange={(e) => setTaskForm({ ...taskForm, assigneeId: e.target.value })}>
                  <option value="">Unassigned</option>
                  {members.map((m) => <option key={m.userId} value={m.userId}>{m.name}</option>)}
                </select>
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Due Date</label>
              <input type="datetime-local"
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                value={taskForm.dueDate} onChange={(e) => setTaskForm({ ...taskForm, dueDate: e.target.value })} />
            </div>
            <div className="flex gap-2 justify-end pt-1">
              <button type="button" onClick={() => setShowTaskModal(false)}
                className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Cancel</button>
              <button type="submit" disabled={savingTask}
                className="px-4 py-2 text-sm bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg disabled:opacity-60">
                {savingTask ? 'Saving...' : 'Save'}
              </button>
            </div>
          </form>
        </Modal>
      )}

      {/* Confirm Modal */}
      {confirmModal && (
        <ConfirmModal
          title={confirmModal.title}
          message={confirmModal.message}
          confirmLabel={confirmModal.confirmLabel}
          onConfirm={confirmModal.onConfirm}
          onCancel={() => setConfirmModal(null)}
        />
      )}

      {/* Invite Modal */}
      {showInviteModal && (
        <Modal title="Invite Member" onClose={() => setShowInviteModal(false)}>
          {inviteError && <div className="mb-3 text-sm text-red-600 bg-red-50 border border-red-200 rounded p-2">{inviteError}</div>}
          <form onSubmit={handleInvite} className="space-y-3">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Email *</label>
              <input type="email" required
                className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                value={inviteForm.email} onChange={(e) => setInviteForm({ ...inviteForm, email: e.target.value })} />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Role</label>
              <select className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                value={inviteForm.role} onChange={(e) => setInviteForm({ ...inviteForm, role: e.target.value })}>
                <option value="MEMBER">Member</option>
                <option value="ADMIN">Admin</option>
              </select>
            </div>
            <div className="flex gap-2 justify-end pt-1">
              <button type="button" onClick={() => setShowInviteModal(false)}
                className="px-4 py-2 text-sm text-gray-600 hover:text-gray-800">Cancel</button>
              <button type="submit" disabled={inviting}
                className="px-4 py-2 text-sm bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg disabled:opacity-60">
                {inviting ? 'Inviting...' : 'Invite'}
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  );
}
