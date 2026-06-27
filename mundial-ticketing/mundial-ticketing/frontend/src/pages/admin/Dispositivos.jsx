import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Dispositivos.module.css';

export default function Dispositivos() {
  const [funcionarios, setFuncionarios] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [mailFuncionario, setMailFuncionario] = useState('');
  const [codigoDispositivo, setCodigoDispositivo] = useState('');
  const [idEvento, setIdEvento] = useState('');
  const [sectoresSeleccionados, setSectoresSeleccionados] = useState([]);
  const [error, setError] = useState('');
  const [exito, setExito] = useState('');
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    cargarFuncionarios();
  }, []);

  async function cargarFuncionarios() {
    setCargando(true);
    try {
      const [resFuncionarios, resEventos] = await Promise.all([
        api.get('/dispositivos/funcionarios-disponibles'),
        api.get('/eventos'),
      ]);
      setFuncionarios(resFuncionarios.data);
      setEventos(resEventos.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  async function handleAsignar() {
    setError('');
    setExito('');

    if (!mailFuncionario || !codigoDispositivo || !idEvento) {
      setError('Debe seleccionar un funcionario, un evento y un código de dispositivo');
      return;
    }

    if (sectoresSeleccionados.length === 0) {
      setError('Debe seleccionar al menos un sector');
      return;
    }

    setEnviando(true);
    try {
      await api.post('/dispositivos', {
        mailFuncionario,
        codigoDispositivo,
        idEvento: parseInt(idEvento, 10),
        sectoresAsignados: sectoresSeleccionados,
      });
      setExito('Dispositivo asignado correctamente, con los sectores seleccionados');
      setMailFuncionario('');
      setCodigoDispositivo('');
      setIdEvento('');
      setSectoresSeleccionados([]);
      cargarFuncionarios();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al asignar el dispositivo');
    } finally {
      setEnviando(false);
    }
  }

  function toggleSector(letra) {
    setSectoresSeleccionados((prev) =>
      prev.includes(letra) ? prev.filter((l) => l !== letra) : [...prev, letra]
    );
  }

  function handleEventoChange(valor) {
    setIdEvento(valor);
    setSectoresSeleccionados([]);
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

        <div className={styles.field}>
          <label>Evento</label>
          <select value={idEvento} onChange={(e) => handleEventoChange(e.target.value)}>
            <option value="">Seleccionar...</option>
            {eventos.map((evento) => (
              <option key={evento.idEvento} value={evento.idEvento}>
                {evento.equipoLocal} vs {evento.equipoVisitante} — {evento.fechaEvento}
              </option>
            ))}
          </select>
        </div>

        {idEvento && (
          <div className={styles.field}>
            <label>Sectores a asignar</label>
            {eventos
              .find((e) => e.idEvento === parseInt(idEvento, 10))
              ?.sectoresHabilitados.map((sector) => (
                <div key={sector.letraSector} className={styles.sectorCheckRow}>
                  <input
                    type="checkbox"
                    checked={sectoresSeleccionados.includes(sector.letraSector)}
                    onChange={() => toggleSector(sector.letraSector)}
                  />
                  <span>Sector {sector.letraSector}</span>
                </div>
              ))}
          </div>
        )}

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