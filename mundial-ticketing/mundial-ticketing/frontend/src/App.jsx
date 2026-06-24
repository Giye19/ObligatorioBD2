import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './pages/Login';
import Register from './pages/Register';
import EventosList from './pages/usuario/EventosList';
import EventoDetalle from './pages/usuario/EventoDetalle';
import MisEntradas from './pages/usuario/MisEntradas';
import MisCompras from './pages/usuario/MisCompras';
import MisTransferencias from './pages/usuario/MisTransferencias';
import Estadios from './pages/admin/Estadios';
import Eventos from './pages/admin/Eventos';
import Reportes from './pages/admin/Reportes';
import Escanear from './pages/funcionario/Escanear';



function Placeholder({ texto }) {
  return <div style={{ padding: '2rem' }}>{texto}</div>;
}

function App() {
  return (
    <>
      <Navbar />
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        <Route
          path="/"
          element={
            <ProtectedRoute rolesPermitidos={['USUARIO']}>
              <EventosList />
            </ProtectedRoute>
          }
        />
        <Route
          path="/eventos/:id"
          element={
            <ProtectedRoute rolesPermitidos={['USUARIO']}>
              <EventoDetalle />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mis-entradas"
          element={
            <ProtectedRoute rolesPermitidos={['USUARIO']}>
              <MisEntradas />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mis-compras"
          element={
            <ProtectedRoute rolesPermitidos={['USUARIO']}>
              <MisCompras />
            </ProtectedRoute>
          }
        />
        <Route
          path="/mis-transferencias"
          element={
            <ProtectedRoute rolesPermitidos={['USUARIO']}>
              <MisTransferencias />
            </ProtectedRoute>
          }
        />

        <Route
          path="/admin/estadios"
          element={
            <ProtectedRoute rolesPermitidos={['ADMIN']}>
              <Estadios />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/eventos"
          element={
            <ProtectedRoute rolesPermitidos={['ADMIN']}>
              <Eventos />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin/reportes"
          element={
            <ProtectedRoute rolesPermitidos={['ADMIN']}>
              <Reportes />
            </ProtectedRoute>
          }
        />

        <Route
          path="/funcionario/escanear"
          element={
            <ProtectedRoute rolesPermitidos={['FUNCIONARIO']}>
              <Escanear />
            </ProtectedRoute>
          }
        />
      </Routes>
    </>
  );
}

export default App;