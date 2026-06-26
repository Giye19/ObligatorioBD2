import { useEffect, useState, useRef } from 'react';
import api from '../../api/axiosConfig';
import styles from './MisEntradas.module.css';

const INTERVALO_QR_MS = 30000;

function badgeClase(estado) {
  if (estado === 'ACTIVA') return styles.estadoActiva;
  if (estado === 'TRANSFERIDA') return styles.estadoTransferida;
  return styles.estadoConsumida;
}

export default function MisEntradas() {
  const [entradas, setEntradas] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [qrAbierto, setQrAbierto] = useState(null);
  const [qrData, setQrData] = useState(null);
  const [segundosRestantes, setSegundosRestantes] = useState(30);
  const [modalTransferir, setModalTransferir] = useState(null);
  const [mailDestino, setMailDestino] = useState('');
  const [errorTransferencia, setErrorTransferencia] = useState('');

  const intervaloRef = useRef(null);
  const cuentaAtrasRef = useRef(null);

  useEffect(() => {
    cargarEntradas();
    return () => limpiarIntervalos();
  }, []);

  async function cargarEntradas() {
    setCargando(true);
    try {
      const response = await api.get('/entradas/mis-entradas');
      setEntradas(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  function limpiarIntervalos() {
    if (intervaloRef.current) clearInterval(intervaloRef.current);
    if (cuentaAtrasRef.current) clearInterval(cuentaAtrasRef.current);
  }

  async function abrirQr(idEntrada) {
    limpiarIntervalos();
    setQrAbierto(idEntrada);
    await generarQr(idEntrada);

    intervaloRef.current = setInterval(() => generarQr(idEntrada), INTERVALO_QR_MS);

    setSegundosRestantes(30);
    cuentaAtrasRef.current = setInterval(() => {
      setSegundosRestantes((prev) => (prev > 0 ? prev - 1 : 30));
    }, 1000);
  }

  async function generarQr(idEntrada) {
    try {
      const response = await api.post(`/validacion/qr/${idEntrada}/generar`);
      setQrData(response.data);
      setSegundosRestantes(30);
    } catch (err) {
      console.error(err);
    }
  }

  function cerrarQr() {
    limpiarIntervalos();
    setQrAbierto(null);
    setQrData(null);
  }

  function abrirModalTransferir(idEntrada) {
    setModalTransferir(idEntrada);
    setMailDestino('');
    setErrorTransferencia('');
  }

  async function confirmarTransferencia() {
    setErrorTransferencia('');
    try {
      await api.post('/transferencias', {
        idEntrada: modalTransferir,
        mailDestino,
      });
      setModalTransferir(null);
      cargarEntradas();
    } catch (err) {
      const mensaje = err.response?.data?.message || 'Error al transferir';
      setErrorTransferencia(mensaje);
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Mis entradas</h1>

      {entradas.length === 0 ? (
        <div className={styles.vacio}>No tenés entradas asignadas</div>
      ) : (
        <div className={styles.grid}>
          {entradas.map((entrada) => (
            <div key={entrada.idEntrada} className={styles.card}>
              <div className={styles.equipos}>
                {entrada.equipoLocal} vs {entrada.equipoVisitante}
              </div>
              <div className={styles.detalle}>📍 {entrada.nombreEstadio}</div>
              <div className={styles.detalle}>Sector {entrada.letraSector} - ${entrada.costoEntrada.toFixed(2)}</div>
              <div className={styles.detalle}>Transferencias: {entrada.cantTransferencias}/3</div>
              <span className={`${styles.estadoBadge} ${badgeClase(entrada.estado)}`}>
                {entrada.estado}
              </span>

              {entrada.estado !== 'CONSUMIDA' && (
                <>
                  {qrAbierto === entrada.idEntrada ? (
                    <div className={styles.qrContainer}>
                      <div className={styles.qrCode}>{qrData?.token}</div>
                      <div className={styles.qrTimer}>Se renueva en {segundosRestantes}s</div>
                      <button className={styles.qrButton} onClick={cerrarQr}>
                        Cerrar QR
                      </button>
                    </div>
                  ) : (
                    <button className={styles.qrButton} onClick={() => abrirQr(entrada.idEntrada)}>
                      Mostrar QR
                    </button>
                  )}

                  <button
                  className={styles.transferirButton}
                  onClick={() => abrirModalTransferir(entrada.idEntrada)}
                  >
                    Transferir
                  </button>
                </>
              )}
            </div>
          ))}
        </div>
      )}

      {modalTransferir && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>Transferir entrada</h3>
            <input
              type="email"
              placeholder="Mail del destinatario"
              value={mailDestino}
              onChange={(e) => setMailDestino(e.target.value)}
            />
            {errorTransferencia && <div className={styles.error}>{errorTransferencia}</div>}
            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setModalTransferir(null)}>
                Cancelar
              </button>
              <button className={styles.confirmButton} onClick={confirmarTransferencia}>
                Confirmar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}