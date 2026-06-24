import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Estadios.module.css';

export default function Estadios() {
  const [estadios, setEstadios] = useState([]);
  const [paises, setPaises] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [nombre, setNombre] = useState('');
  const [nombrePais, setNombrePais] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    cargarEstadios();
  }, []);

  async function cargarEstadios() {
    setCargando(true);
    try {
      const response = await api.get('/estadios');
      setEstadios(response.data);

      const paisesUnicos = [...new Set(response.data.map((e) => e.nombrePais))];
      if (paisesUnicos.length === 0) {
        setPaises(['USA', 'Canada', 'Mexico']);
      } else {
        setPaises(paisesUnicos);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  function abrirModal() {
    setNombre('');
    setNombrePais('');
    setError('');
    setModalAbierto(true);
  }

  async function handleCrear() {
    setError('');
    try {
      await api.post('/estadios', { nombre, nombrePais });
      setModalAbierto(false);
      cargarEstadios();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear el estadio');
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <h1 className={styles.title}>Estadios</h1>
        <button className={styles.addButton} onClick={abrirModal}>
          + Nuevo estadio
        </button>
      </div>

      {estadios.length === 0 ? (
        <div className={styles.vacio}>No hay estadios registrados todavía</div>
      ) : (
        estadios.map((estadio) => (
          <div key={estadio.idEstadio} className={styles.card}>
            <div className={styles.nombreEstadio}>{estadio.nombre}</div>
            <div className={styles.paisSede}>{estadio.nombrePais}</div>
            <div className={styles.sectoresRow}>
              {estadio.sectores.map((sector) => (
                <span key={sector.letra} className={styles.sectorTag}>
                  Sector {sector.letra} - {sector.capacidad} lugares
                </span>
              ))}
            </div>
          </div>
        ))
      )}

      {modalAbierto && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>Nuevo estadio</h3>

            {error && <div className={styles.error}>{error}</div>}

            <div className={styles.field}>
              <label>Nombre del estadio</label>
              <input value={nombre} onChange={(e) => setNombre(e.target.value)} />
            </div>

            <div className={styles.field}>
              <label>País sede</label>
              <select value={nombrePais} onChange={(e) => setNombrePais(e.target.value)}>
                <option value="">Seleccionar...</option>
                {paises.map((pais) => (
                  <option key={pais} value={pais}>{pais}</option>
                ))}
              </select>
            </div>

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setModalAbierto(false)}>
                Cancelar
              </button>
              <button className={styles.confirmButton} onClick={handleCrear}>
                Crear
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}