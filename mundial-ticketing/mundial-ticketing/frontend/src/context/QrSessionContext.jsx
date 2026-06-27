import { createContext, useContext, useRef, useState } from 'react';
import api from '../api/axiosConfig';

const QrSessionContext = createContext(null);

export function useQrSession() {
  return useContext(QrSessionContext);
}

const INTERVALO_QR_MS = 30000;

export function QrSessionProvider({ children }) {
  const [entradaActiva, setEntradaActiva] = useState(null);
  const [qrData, setQrData] = useState(null);
  const [segundosRestantes, setSegundosRestantes] = useState(30);

  const intervaloRef = useRef(null);
  const cuentaAtrasRef = useRef(null);

  function limpiarIntervalos() {
    if (intervaloRef.current) clearInterval(intervaloRef.current);
    if (cuentaAtrasRef.current) clearInterval(cuentaAtrasRef.current);
    intervaloRef.current = null;
    cuentaAtrasRef.current = null;
  }

  async function abrirQr(idEntrada) {
    limpiarIntervalos();
    setEntradaActiva(idEntrada);

    try {
      await api.post(`/validacion/qr/${idEntrada}/abrir`);
      await obtenerQrActual(idEntrada);
    } catch (err) {
      console.error(err);
      return;
    }

    intervaloRef.current = setInterval(() => obtenerQrActual(idEntrada), INTERVALO_QR_MS);

    cuentaAtrasRef.current = setInterval(() => {
      setSegundosRestantes((prev) => (prev > 0 ? prev - 1 : 30));
    }, 1000);
  }

  async function obtenerQrActual(idEntrada) {
    try {
      const response = await api.get(`/validacion/qr/${idEntrada}`);
      setQrData(response.data);
      setSegundosRestantes(30);
    } catch (err) {
      console.error(err);
    }
  }

  function cerrarQr() {
    limpiarIntervalos();
    if (qrData?.idQr) {
      api.post(`/validacion/qr/${qrData.idQr}/cerrar`).catch((err) => console.error(err));
    }
    setEntradaActiva(null);
    setQrData(null);
  }

  const value = {
    entradaActiva,
    qrData,
    segundosRestantes,
    abrirQr,
    cerrarQr,
  };

  return (
    <QrSessionContext.Provider value={value}>
      {children}
    </QrSessionContext.Provider>
  );
}