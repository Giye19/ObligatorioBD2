import { createContext, useContext, useState, useEffect } from 'react';
import api from '../api/axiosConfig';


const AuthContext = createContext(null);


export function useAuth() {
  return useContext(AuthContext);
}


export function AuthProvider({ children }) {

  const [usuario, setUsuario] = useState(() => {
    const mail = localStorage.getItem('mail');
    const rol = localStorage.getItem('rol');
    const token = localStorage.getItem('token');

    if (mail && rol && token) {
      return { mail, rol };
    }
    return null;
  });


  const [cargando, setCargando] = useState(false);

  // realiza el login contra el backend y guarda la sesion
  async function login(mail, password) {
    const response = await api.post('/auth/login', { mail, password });
    const { token, mail: mailRespuesta, rol } = response.data;

    localStorage.setItem('token', token);
    localStorage.setItem('mail', mailRespuesta);
    localStorage.setItem('rol', rol);

    setUsuario({ mail: mailRespuesta, rol });
    return rol;
  }

  // realiza el registro de un nuevo usuario general y lo deja logueado
  async function register(datosRegistro) {
    const response = await api.post('/auth/register', datosRegistro);
    const { token, mail, rol } = response.data;

    localStorage.setItem('token', token);
    localStorage.setItem('mail', mail);
    localStorage.setItem('rol', rol);

    setUsuario({ mail, rol });
    return rol;
  }

  // cierra la sesion y limpia todo lo guardado localmente
  function logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('mail');
    localStorage.removeItem('rol');
    setUsuario(null);
  }

  const value = {
    usuario,
    cargando,
    login,
    register,
    logout,
    estaAutenticado: usuario !== null,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}