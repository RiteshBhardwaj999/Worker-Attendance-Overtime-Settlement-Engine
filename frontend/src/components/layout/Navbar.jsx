import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav className="bg-indigo-700 text-white px-6 py-3 flex items-center justify-between shadow-md">
      <Link to="/dashboard" className="text-xl font-bold tracking-tight hover:text-indigo-200">
        TeamTasker
      </Link>
      <div className="flex items-center gap-4">
        <span className="text-sm text-indigo-200">Hi, {user?.name}</span>
        <button
          onClick={handleLogout}
          className="text-sm bg-indigo-600 hover:bg-indigo-500 px-3 py-1.5 rounded transition"
        >
          Logout
        </button>
      </div>
    </nav>
  );
}
