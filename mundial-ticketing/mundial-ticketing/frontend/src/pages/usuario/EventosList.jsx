import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';
import styles from './EventosList.module.css';

export default function EventosList() {
  const [eventos, setEventos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');

  const navigate = useNavigate();

  useEffect(() => {
    cargarEventos();
  }, []);

  async function cargarEventos() {
    setCargando(true);
    setError('');
    try {
      const response = await api.get('/eventos');
      setEventos(response.data);
    } catch (err) {
      setError('No se pudieron cargar los eventos');
    } finally {
      setCargando(false);
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando eventos...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Próximos eventos</h1>

      {error && <div className={styles.error}>{error}</div>}

      {eventos.length === 0 ? (
        <div className={styles.vacio}>No hay eventos disponibles por el momento</div>
      ) : (
        <div className={styles.grid}>
          {eventos.map((evento) => (
            <div
              key={evento.idEvento}
              className={styles.card}
              onClick={() => navigate(`/eventos/${evento.idEvento}`)}
            >
              <div className={styles.equipos}>
                {evento.equipoLocal} vs {evento.equipoVisitante}
              </div>
              <div className={styles.detalle}>📍 {evento.nombreEstadio}, {evento.nombrePaisSede}</div>
              <div className={styles.detalle}>📅 {evento.fechaEvento} - 🕐 {evento.horaEvento}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}