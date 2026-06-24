import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { usuario, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  if (!usuario) {
    return null;
  }

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo}>Mundial Ticketing 2026</div>
      <div className={styles.links}>
        {usuario.rol === 'USUARIO' && (
          <>
            <Link to="/">Eventos</Link>
            <Link to="/mis-entradas">Mis Entradas</Link>
            <Link to="/mis-compras">Mis Compras</Link>
            <Link to="/mis-transferencias">Transferencias</Link>
          </>
        )}
        {usuario.rol === 'ADMIN' && (
          <>
            <Link to="/admin/estadios">Estadios</Link>
            <Link to="/admin/eventos">Eventos</Link>
            <Link to="/admin/reportes">Reportes</Link>
          </>
        )}
        {usuario.rol === 'FUNCIONARIO' && (
          <Link to="/funcionario/escanear">Escanear</Link>
        )}
        <span>{usuario.mail}</span>
        <button className={styles.logoutButton} onClick={handleLogout}>
          Cerrar sesión
        </button>
      </div>
    </nav>
  );
}