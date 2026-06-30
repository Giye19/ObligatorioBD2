import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import api from '../api/axiosConfig';
import styles from './Login.module.css';

export default function Register() {
  const [form, setForm] = useState({
    mail: '',
    password: '',
    docPais: '',
    docTipo: '',
    docNumero: '',
    dirPais: '',
    dirLocalidad: '',
    dirCalle: '',
    dirNumero: '',
    dirCodPostal: '',
  });

  const [paises, setPaises] = useState([]);
  const [telefonos, setTelefonos] = useState(['']);
  const [error, setError] = useState('');
  const [cargando, setCargando] = useState(false);

  const { register } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    cargarPaises();
  }, []);

  async function cargarPaises() {
    try {
      const response = await api.get('/paises');
      setPaises(response.data);
    } catch (err) {
      console.error(err);
    }
  }

  function handleChange(e) {
    setForm({ ...form, [e.target.name]: e.target.value });
  }

  function handleTelefonoChange(index, valor) {
    const nuevos = [...telefonos];
    nuevos[index] = valor;
    setTelefonos(nuevos);
  }

  function agregarTelefono() {
    setTelefonos([...telefonos, '']);
  }

  function quitarTelefono(index) {
    setTelefonos(telefonos.filter((_, i) => i !== index));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setCargando(true);

    const telefonosLimpios = telefonos.filter((t) => t.trim() !== '');

    if (telefonosLimpios.length === 0) {
      setError('Debe ingresar al menos un teléfono');
      setCargando(false);
      return;
    }

    try {
      await register({ ...form, telefonos: telefonosLimpios });
      navigate('/');
    } catch (err) {
      const mensaje = err.response?.data?.message || 'Error al registrarse';
      setError(mensaje);
    } finally {
      setCargando(false);
    }
  }

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <h1 className={styles.title}>Crear cuenta</h1>

        {error && <div className={styles.error}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className={styles.field}>
            <label htmlFor="mail">Mail</label>
            <input id="mail" name="mail" type="email" value={form.mail} onChange={handleChange} required />
          </div>

          <div className={styles.field}>
            <label htmlFor="password">Contraseña</label>
            <input id="password" name="password" type="password" value={form.password} onChange={handleChange} required />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="docPais">País documento</label>
              <select id="docPais" name="docPais" value={form.docPais} onChange={handleChange} required>
                <option value="">Seleccionar...</option>
                {paises.map((p) => (
                  <option key={p.idPais} value={p.nombrePais}>{p.nombrePais}</option>
                ))}
              </select>
            </div>
            <div className={styles.field}>
              <label htmlFor="docTipo">Tipo documento</label>
              <select id="docTipo" name="docTipo" value={form.docTipo} onChange={handleChange} required>
                <option value="">Seleccionar...</option>
                <option value="CI">CI</option>
                <option value="PASAPORTE">Pasaporte</option>
                <option value="OTRO">Otro</option>
              </select>
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="docNumero">Número de documento</label>
            <input id="docNumero" name="docNumero" value={form.docNumero} onChange={handleChange} required />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="dirPais">País</label>
              <select id="dirPais" name="dirPais" value={form.dirPais} onChange={handleChange} required>
                <option value="">Seleccionar...</option>
                {paises.map((p) => (
                  <option key={p.idPais} value={p.nombrePais}>{p.nombrePais}</option>
                ))}
              </select>
            </div>
            <div className={styles.field}>
              <label htmlFor="dirLocalidad">Localidad</label>
              <input id="dirLocalidad" name="dirLocalidad" value={form.dirLocalidad} onChange={handleChange} required />
            </div>
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="dirCalle">Calle</label>
              <input id="dirCalle" name="dirCalle" value={form.dirCalle} onChange={handleChange} required />
            </div>
            <div className={styles.field}>
              <label htmlFor="dirNumero">Número</label>
              <input id="dirNumero" name="dirNumero" value={form.dirNumero} onChange={handleChange} required />
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="dirCodPostal">Código postal</label>
            <input id="dirCodPostal" name="dirCodPostal" value={form.dirCodPostal} onChange={handleChange} required />
          </div>

          <label>Teléfonos</label>
          {telefonos.map((telefono, index) => (
            <div className={styles.telefonosRow} key={index}>
              <input
                value={telefono}
                onChange={(e) => handleTelefonoChange(index, e.target.value)}
                placeholder="099123456"
              />
              {telefonos.length > 1 && (
                <button type="button" className={styles.removeButton} onClick={() => quitarTelefono(index)}>
                  ✕
                </button>
              )}
            </div>
          ))}
          <button type="button" className={styles.addButton} onClick={agregarTelefono}>
            + Agregar teléfono
          </button>

          <button type="submit" className={styles.submitButton} disabled={cargando}>
            {cargando ? 'Creando cuenta...' : 'Registrarme'}
          </button>
        </form>

        <p className={styles.linkText}>
          ¿Ya tenés cuenta? <Link to="/login">Iniciá sesión</Link>
        </p>
      </div>
    </div>
  );
}