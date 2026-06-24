import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './MisCompras.module.css';

export default function MisCompras() {
  const [ventas, setVentas] = useState([]);
  const [cargando, setCargando] = useState(true);

  useEffect(() => {
    cargarVentas();
  }, []);

  async function cargarVentas() {
    setCargando(true);
    try {
      const response = await api.get('/ventas/mis-compras');
      setVentas(response.data);
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
      <h1 className={styles.title}>Mis compras</h1>

      {ventas.length === 0 ? (
        <div className={styles.vacio}>Todavía no realizaste ninguna compra</div>
      ) : (
        ventas.map((venta) => (
          <div key={venta.idVenta} className={styles.ventaCard}>
            <div className={styles.ventaHeader}>
              <div>
                <div className={styles.ventaInfo}>
                  Venta #{venta.idVenta} - {new Date(venta.fechaVenta).toLocaleString()}
                </div>
                <span className={styles.estadoBadge}>{venta.estado}</span>
              </div>
              <div className={styles.ventaMonto}>${venta.montoTotal.toFixed(2)}</div>
            </div>

            {venta.entradas.map((entrada) => (
              <div key={entrada.idEntrada} className={styles.entradaRow}>
                <span>
                  {entrada.equipoLocal} vs {entrada.equipoVisitante} - Sector {entrada.letraSector}
                </span>
                <span>${entrada.costoEntrada.toFixed(2)}</span>
              </div>
            ))}
          </div>
        ))
      )}
    </div>
  );
}