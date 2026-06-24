import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Escanear.module.css';

export default function Escanear() {
  const [tokenQr, setTokenQr] = useState('');
  const [idDispositivo, setIdDispositivo] = useState('');
  const [puertaIngreso, setPuertaIngreso] = useState('');
  const [resultado, setResultado] = useState(null);
  const [escaneando, setEscaneando] = useState(false);
  const [errorDispositivo, setErrorDispositivo] = useState('');

  useEffect(() => {
    cargarDispositivo();
  }, []);

  async function cargarDispositivo() {
    try {
      const response = await api.get('/dispositivos/mi-dispositivo');
      setIdDispositivo(response.data.idDispositivo);
    } catch (err) {
      setErrorDispositivo('No tiene un dispositivo autorizado asignado. Contacte a un administrador.');
    }
  }

  async function handleEscanear() {
    setResultado(null);
    setEscaneando(true);

    try {
      const response = await api.post('/validacion/escanear', {
        tokenQr,
        idDispositivo,
        puertaIngreso,
      });
      setResultado(response.data);
    } catch (err) {
      setResultado({
        accesoPermitido: false,
        mensaje: err.response?.data?.message || 'Error al validar el acceso',
      });
    } finally {
      setEscaneando(false);
      setTokenQr('');
    }
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Validación de acceso</h1>

      <div className={styles.card}>
        {errorDispositivo ? (
          <div className={styles.resultado + ' ' + styles.resultadoError}>
            {errorDispositivo}
          </div>
        ) : (
          <>
            <div className={styles.field}>
              <label>Dispositivo</label>
              <input value={idDispositivo} disabled />
            </div>

            <div className={styles.field}>
              <label>Puerta de ingreso (opcional)</label>
              <input
                value={puertaIngreso}
                onChange={(e) => setPuertaIngreso(e.target.value)}
                placeholder="Puerta Norte"
              />
            </div>

            <div className={styles.field}>
              <label>Token del QR escaneado</label>
              <input
                value={tokenQr}
                onChange={(e) => setTokenQr(e.target.value)}
                placeholder="Pegar token aquí"
              />
            </div>

            <button
              className={styles.scanButton}
              onClick={handleEscanear}
              disabled={escaneando || !tokenQr}
            >
              {escaneando ? 'Validando...' : 'Validar acceso'}
            </button>
          </>
        )}

        {resultado && (
          <div
            className={`${styles.resultado} ${
              resultado.accesoPermitido ? styles.resultadoExito : styles.resultadoError
            }`}
          >
            <div className={styles.resultadoIcono}>
              {resultado.accesoPermitido ? '✅' : '❌'}
            </div>
            <div className={styles.resultadoMensaje}>{resultado.mensaje}</div>
            {resultado.accesoPermitido && (
              <div className={styles.resultadoDetalle}>
                Entrada #{resultado.idEntrada} - Sector {resultado.letraSector}
                <br />
                Propietario: {resultado.mailPropietario}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}