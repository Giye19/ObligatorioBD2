import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Dispositivos.module.css';

export default function Dispositivos() {
  const [funcionarios, setFuncionarios] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mailFuncionario, setMailFuncionario] = useState('');
  const [codigoDispositivo, setCodigoDispositivo] = useState('');
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    cargarFuncionarios();
  }, []);

  async function cargarFuncionarios() {
    setCargando(true);
    try {
      const response = await api.get('/dispositivos/funcionarios-disponibles');
      setFuncionarios(response.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  async function handleAsignar() {
    setError('');
    setExito('');

    if (!mailFuncionario || !codigoDispositivo) {
      setError('Debe seleccionar un funcionario y un código de dispositivo');
      return;
    }

    setEnviando(true);
    try {
      await api.post('/dispositivos', { mailFuncionario, codigoDispositivo });
      setExito('Dispositivo asignado correctamente');
      setMailFuncionario('');
      setCodigoDispositivo('');
      cargarFuncionarios();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al asignar el dispositivo');
    } finally {
      setEnviando(false);
    }
  }

  const funcionariosSinDispositivo = funcionarios.filter((f) => !f.tieneDispositivo);

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <h1 className={styles.title}>Dispositivos de validación</h1>

      <div className={styles.card}>
        {error && <div className={styles.error}>{error}</div>}
        {exito && <div className={styles.success}>{exito}</div>}

        <div className={styles.field}>
          <label>Funcionario</label>
          <select value={mailFuncionario} onChange={(e) => setMailFuncionario(e.target.value)}>
            <option value="">Seleccionar...</option>
            {funcionariosSinDispositivo.map((f) => (
              <option key={f.idFuncionario} value={f.mail}>
                {f.mail} (legajo {f.nroLegajo})
              </option>
            ))}
          </select>
        </div>

        <div className={styles.field}>
          <label>Código del dispositivo</label>
          <input
            value={codigoDispositivo}
            onChange={(e) => setCodigoDispositivo(e.target.value)}
            placeholder="DISP-001"
          />
        </div>

        <button className={styles.submitButton} onClick={handleAsignar} disabled={enviando}>
          {enviando ? 'Asignando...' : 'Asignar dispositivo'}
        </button>
      </div>

      <div className={styles.card}>
        <h2 className={styles.listaTitle}>Funcionarios registrados</h2>

        {funcionarios.length === 0 ? (
          <div className={styles.vacio}>No hay funcionarios registrados</div>
        ) : (
          funcionarios.map((f) => (
            <div key={f.idFuncionario} className={styles.funcionarioRow}>
              <span>{f.mail} — legajo {f.nroLegajo}</span>
              <span
                className={`${styles.badge} ${
                  f.tieneDispositivo ? styles.badgeConDispositivo : styles.badgeSinDispositivo
                }`}
              >
                {f.tieneDispositivo ? 'Con dispositivo' : 'Sin dispositivo'}
              </span>
            </div>
          ))
        )}
      </div>
    </div>
  );
}