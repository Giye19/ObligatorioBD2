import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../../api/axiosConfig';
import styles from './EventoDetalle.module.css';

const MAXIMO_ENTRADAS = 5;

export default function EventoDetalle() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [evento, setEvento] = useState(null);
  const [cantidades, setCantidades] = useState({});
  const [cargando, setCargando] = useState(true);
  const [comprando, setComprando] = useState(false);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');

  useEffect(() => {
    cargarEvento();
  }, [id]);

  async function cargarEvento() {
    setCargando(true);
    try {
      const response = await api.get(`/eventos/${id}`);
      setEvento(response.data);

      const inicial = {};
      response.data.sectoresHabilitados.forEach((s) => {
        inicial[s.letraSector] = 0;
      });
      setCantidades(inicial);
    } catch (err) {
      setError('No se pudo cargar el evento');
    } finally {
      setCargando(false);
    }
  }

  function handleCantidadChange(letra, valor, disponibles) {
    let cantidad = parseInt(valor, 10);
    if (isNaN(cantidad) || cantidad < 0) cantidad = 0;
    if (cantidad > disponibles) cantidad = disponibles;

    setCantidades({ ...cantidades, [letra]: cantidad });
  }

  function totalEntradas() {
    return Object.values(cantidades).reduce((acc, c) => acc + c, 0);
  }

  function montoEstimado() {
    if (!evento) return 0;
    return evento.sectoresHabilitados.reduce((acc, s) => {
      return acc + (cantidades[s.letraSector] || 0) * s.costoEntrada;
    }, 0);
  }

  async function handleComprar() {
    setError('');
    setExito('');

    const itemsEntrada = Object.entries(cantidades)
      .filter(([, cantidad]) => cantidad > 0)
      .map(([letraSector, cantidad]) => ({ letraSector, cantidad }));

    if (itemsEntrada.length === 0) {
      setError('Debe seleccionar al menos una entrada');
      return;
    }

    if (totalEntradas() > MAXIMO_ENTRADAS) {
      setError(`No se pueden comprar más de ${MAXIMO_ENTRADAS} entradas en una transacción`);
      return;
    }

    setComprando(true);
    try {
      await api.post('/ventas', {
        idEvento: parseInt(id, 10),
        itemsEntrada,
      });
      setExito('¡Compra realizada con éxito!');
      setTimeout(() => navigate('/mis-entradas'), 1500);
    } catch (err) {
      const mensaje = err.response?.data?.message || 'Error al procesar la compra';
      setError(mensaje);
    } finally {
      setComprando(false);
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  if (!evento) {
    return <div className={styles.container}>Evento no encontrado</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.equipos}>{evento.equipoLocal} vs {evento.equipoVisitante}</div>
        <div className={styles.detalle}>📍 {evento.nombreEstadio}, {evento.nombrePaisSede}</div>
        <div className={styles.detalle}>📅 {evento.fechaEvento} - 🕐 {evento.horaEvento}</div>
      </div>

      {error && <div className={styles.error}>{error}</div>}
      {exito && <div className={styles.success}>{exito}</div>}

      <h2 className={styles.sectoresTitle}>Seleccioná tus entradas</h2>

      {evento.sectoresHabilitados.map((sector) => (
        <div key={sector.letraSector} className={styles.sectorRow}>
          <div className={styles.sectorInfo}>
            <div className={styles.sectorLetra}>Sector {sector.letraSector}</div>
            <div className={styles.sectorPrecio}>${sector.costoEntrada.toFixed(2)}</div>
            <div className={styles.sectorDisponibles}>
              {sector.entradasDisponibles} disponibles
            </div>
          </div>
          <input
            type="number"
            min="0"
            max={sector.entradasDisponibles}
            className={styles.cantidadInput}
            value={cantidades[sector.letraSector] || 0}
            onChange={(e) =>
              handleCantidadChange(sector.letraSector, e.target.value, sector.entradasDisponibles)
            }
          />
        </div>
      ))}

      <div className={styles.resumen}>
        <div className={styles.totalRow}>
          <span>Total ({totalEntradas()} entradas)</span>
          <span>${montoEstimado().toFixed(2)}</span>
        </div>
        <button
          className={styles.comprarButton}
          onClick={handleComprar}
          disabled={comprando || totalEntradas() === 0}
        >
          {comprando ? 'Procesando...' : 'Comprar'}
        </button>
      </div>
    </div>
  );
}