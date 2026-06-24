import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Reportes.module.css';

export default function Reportes() {
  const [rankingEventos, setRankingEventos] = useState([]);
  const [rankingCompradores, setRankingCompradores] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    cargarReportes();
  }, []);

  async function cargarReportes() {
    setCargando(true);
    try {
      const [resEventos, resCompradores] = await Promise.all([
        api.get('/reportes/eventos-mas-vendidos'),
        api.get('/reportes/mayores-compradores'),
      ]);
      setRankingEventos(resEventos.data);
      setRankingCompradores(resCompradores.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Reportes</h1>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Eventos con más entradas vendidas</h2>

        {rankingEventos.length === 0 ? (
          <div className={styles.vacio}>Todavía no hay ventas registradas</div>
        ) : (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>#</th>
                <th>Evento</th>
                <th>Estadio</th>
                <th>Fecha</th>
                <th>Entradas vendidas</th>
              </tr>
            </thead>
            <tbody>
              {rankingEventos.map((r, index) => (
                <tr key={r.idEvento}>
                  <td className={styles.posicion}>{index + 1}</td>
                  <td>{r.equipoLocal} vs {r.equipoVisitante}</td>
                  <td>{r.nombreEstadio}</td>
                  <td>{r.fechaEvento}</td>
                  <td className={styles.cantidad}>{r.cantidadEntradasVendidas}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <div className={styles.section}>
        <h2 className={styles.sectionTitle}>Mayores compradores</h2>

        {rankingCompradores.length === 0 ? (
          <div className={styles.vacio}>Todavía no hay compradores registrados</div>
        ) : (
          <table className={styles.table}>
            <thead>
              <tr>
                <th>#</th>
                <th>Usuario</th>
                <th>Entradas compradas</th>
              </tr>
            </thead>
            <tbody>
              {rankingCompradores.map((r, index) => (
                <tr key={r.mailComprador}>
                  <td className={styles.posicion}>{index + 1}</td>
                  <td>{r.mailComprador}</td>
                  <td className={styles.cantidad}>{r.cantidadEntradasCompradas}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}