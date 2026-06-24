import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import { useAuth } from '../../context/AuthContext';
import styles from './MisTransferencias.module.css';

function badgeClase(estado) {
  if (estado === 'PENDIENTE') return styles.estadoPendiente;
  if (estado === 'ACEPTADA') return styles.estadoAceptada;
  return styles.estadoRechazada;
}

export default function MisTransferencias() {
  const [transferencias, setTransferencias] = useState([]);
  const [cargando, setCargando] = useState(true);

  const { usuario } = useAuth();

  useEffect(() => {
    cargarTransferencias();
  }, []);

  async function cargarTransferencias() {
    setCargando(true);
    try {
      const response = await api.get('/transferencias/mis-transferencias');
      setTransferencias(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  async function aceptar(idTransferencia) {
    try {
      await api.post(`/transferencias/${idTransferencia}/aceptar`);
      cargarTransferencias();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al aceptar');
    }
  }

  async function rechazar(idTransferencia) {
    try {
      await api.post(`/transferencias/${idTransferencia}/rechazar`);
      cargarTransferencias();
    } catch (err) {
      alert(err.response?.data?.message || 'Error al rechazar');
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Mis transferencias</h1>

      {transferencias.length === 0 ? (
        <div className={styles.vacio}>No tenés transferencias registradas</div>
      ) : (
        transferencias.map((t) => {
          const esDestinatario = t.mailDestino === usuario.mail;
          const puedeResponder = esDestinatario && t.estado === 'PENDIENTE';

          return (
            <div key={t.idTransferencia} className={styles.card}>
              <div>
                <div className={styles.info}>
                  Entrada #{t.idEntrada} - {t.mailOrigen} → {t.mailDestino}
                </div>
                <div className={styles.fecha}>{new Date(t.fechaTransferencia).toLocaleString()}</div>
              </div>

              {puedeResponder ? (
                <div className={styles.acciones}>
                  <button className={styles.aceptarButton} onClick={() => aceptar(t.idTransferencia)}>
                    Aceptar
                  </button>
                  <button className={styles.rechazarButton} onClick={() => rechazar(t.idTransferencia)}>
                    Rechazar
                  </button>
                </div>
              ) : (
                <span className={`${styles.estadoBadge} ${badgeClase(t.estado)}`}>{t.estado}</span>
              )}
            </div>
          );
        })
      )}
    </div>
  );
}