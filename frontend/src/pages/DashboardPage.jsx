import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { getDashboard } from '../api/dashboard';
import { useAuth } from '../context/AuthContext';
import Spinner from '../components/common/Spinner';

const COLORS = { TODO: '#6366f1', IN_PROGRESS: '#f59e0b', DONE: '#22c55e' };

function StatsCard({ label, value, icon, color }) {
  return (
    <div className={`bg-white rounded-xl border border-gray-200 p-5 shadow-sm flex items-center gap-4`}>
      <div className={`w-12 h-12 rounded-full flex items-center justify-center text-2xl ${color}`}>{icon}</div>
      <div>
        <p className="text-2xl font-bold text-gray-800">{value}</p>
        <p className="text-sm text-gray-500">{label}</p>
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getDashboard().then((res) => setData(res.data)).finally(() => setLoading(false));
  }, []);

  if (loading) return <Spinner />;

  const chartData = data
    ? [
        { name: 'To Do', value: data.statusBreakdown?.TODO || 0 },
        { name: 'In Progress', value: data.statusBreakdown?.IN_PROGRESS || 0 },
        { name: 'Done', value: data.statusBreakdown?.DONE || 0 },
      ]
    : [];

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-800 mb-6">
        Welcome back, {user?.name} 👋
      </h1>

      {/* Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <StatsCard label="Total Projects" value={data?.totalProjects ?? 0} icon="📁" color="bg-indigo-50" />
        <StatsCard label="Total Tasks" value={data?.totalTasks ?? 0} icon="✅" color="bg-blue-50" />
        <StatsCard label="Assigned to Me" value={data?.myAssignedTasks ?? 0} icon="👤" color="bg-yellow-50" />
        <StatsCard label="Completion Rate" value={`${data?.completionRate ?? 0}%`} icon="📈" color="bg-green-50" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Chart */}
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-base font-semibold text-gray-700 mb-4">Task Status Breakdown</h2>
          {data?.totalTasks === 0 ? (
            <p className="text-sm text-gray-400 text-center py-8">No tasks yet</p>
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={chartData} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={80} label>
                  {chartData.map((entry) => (
                    <Cell key={entry.name} fill={COLORS[Object.keys(COLORS).find((k) => entry.name.replace(' ', '_').toUpperCase().includes(k))] || '#94a3b8'} />
                  ))}
                </Pie>
                <Tooltip />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Overdue Tasks */}
        <div className="bg-white rounded-xl border border-gray-200 p-5 shadow-sm">
          <h2 className="text-base font-semibold text-gray-700 mb-4">
            Overdue Tasks
            {data?.overdueTasks?.length > 0 && (
              <span className="ml-2 text-xs bg-red-100 text-red-600 px-2 py-0.5 rounded-full">{data.overdueTasks.length}</span>
            )}
          </h2>
          {!data?.overdueTasks?.length ? (
            <p className="text-sm text-gray-400 text-center py-8">No overdue tasks 🎉</p>
          ) : (
            <ul className="space-y-2">
              {data.overdueTasks.map((t) => (
                <li key={t.id}>
                  <Link to={`/projects/${t.projectId}`}
                    className="flex items-start gap-2 p-3 rounded-lg border border-red-100 bg-red-50 hover:bg-red-100 transition">
                    <span className="text-red-500 text-sm">⚠️</span>
                    <div>
                      <p className="text-sm font-medium text-gray-800">{t.title}</p>
                      <p className="text-xs text-gray-500">{t.projectName} · Due {new Date(t.dueDate).toLocaleDateString()}</p>
                    </div>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
