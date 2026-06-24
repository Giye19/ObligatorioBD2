import { useEffect, useState } from 'react';
import api from '../../api/axiosConfig';
import styles from './Eventos.module.css';

export default function Eventos() {
  const [eventos, setEventos] = useState([]);
  const [estadios, setEstadios] = useState([]);
  const [equipos, setEquipos] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [modalAbierto, setModalAbierto] = useState(false);
  const [error, setError] = useState('');

  const [idEstadio, setIdEstadio] = useState('');
  const [equipoLocal, setEquipoLocal] = useState('');
  const [equipoVisitante, setEquipoVisitante] = useState('');
  const [fechaEvento, setFechaEvento] = useState('');
  const [horaEvento, setHoraEvento] = useState('');
  const [sectoresForm, setSectoresForm] = useState([]);

  useEffect(() => {
    cargarDatos();
  }, []);

  async function cargarDatos() {
    setCargando(true);
    try {
      const [resEventos, resEstadios, resEquipos] = await Promise.all([
        api.get('/eventos'),
        api.get('/estadios'),
        api.get('/equipos'),
      ]);
      setEventos(resEventos.data);
      setEstadios(resEstadios.data);
      setEquipos(resEquipos.data);
    } catch (err) {
      console.error(err);
    } finally {
      setCargando(false);
    }
  }

  function abrirModal() {
    setIdEstadio('');
    setEquipoLocal('');
    setEquipoVisitante('');
    setFechaEvento('');
    setHoraEvento('');
    setSectoresForm([]);
    setError('');
    setModalAbierto(true);
  }

  function handleEstadioChange(value) {
    setIdEstadio(value);
    const estadio = estadios.find((e) => e.idEstadio === parseInt(value, 10));
    if (estadio) {
      setSectoresForm(
        estadio.sectores.map((s) => ({ letra: s.letra, habilitado: false, costo: '' }))
      );
    } else {
      setSectoresForm([]);
    }
  }

  function toggleSector(letra) {
    setSectoresForm(
      sectoresForm.map((s) =>
        s.letra === letra ? { ...s, habilitado: !s.habilitado } : s
      )
    );
  }

  function cambiarCosto(letra, valor) {
    setSectoresForm(
      sectoresForm.map((s) => (s.letra === letra ? { ...s, costo: valor } : s))
    );
  }

  async function handleCrear() {
    setError('');

    const sectoresHabilitados = sectoresForm
      .filter((s) => s.habilitado)
      .map((s) => ({ letraSector: s.letra, costoEntrada: parseFloat(s.costo) }));

    if (sectoresHabilitados.length === 0) {
      setError('Debe habilitar al menos un sector');
      return;
    }

    if (sectoresHabilitados.some((s) => isNaN(s.costoEntrada) || s.costoEntrada <= 0)) {
      setError('Todos los sectores habilitados deben tener un costo válido');
      return;
    }

    try {
      await api.post('/eventos', {
        idEstadio: parseInt(idEstadio, 10),
        equipoLocal,
        equipoVisitante,
        fechaEvento,
        horaEvento: horaEvento + ':00',
        sectoresHabilitados,
      });
      setModalAbierto(false);
      cargarDatos();
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear el evento');
    }
  }

  if (cargando) {
    return <div className={styles.container}>Cargando...</div>;
  }

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <h1 className={styles.title}>Eventos</h1>
        <button className={styles.addButton} onClick={abrirModal}>
          + Nuevo evento
        </button>
      </div>

      {eventos.length === 0 ? (
        <div className={styles.vacio}>No hay eventos programados todavía</div>
      ) : (
        eventos.map((evento) => (
          <div key={evento.idEvento} className={styles.card}>
            <div className={styles.equipos}>
              {evento.equipoLocal} vs {evento.equipoVisitante}
            </div>
            <div className={styles.detalle}>📍 {evento.nombreEstadio}, {evento.nombrePaisSede}</div>
            <div className={styles.detalle}>📅 {evento.fechaEvento} - 🕐 {evento.horaEvento}</div>
            <div className={styles.sectoresRow}>
              {evento.sectoresHabilitados.map((s) => (
                <span key={s.letraSector} className={styles.sectorTag}>
                  {s.letraSector}: ${s.costoEntrada.toFixed(2)} ({s.entradasDisponibles} disp.)
                </span>
              ))}
            </div>
          </div>
        ))
      )}

      {modalAbierto && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>Nuevo evento</h3>

            {error && <div className={styles.error}>{error}</div>}

            <div className={styles.field}>
              <label>Estadio</label>
              <select value={idEstadio} onChange={(e) => handleEstadioChange(e.target.value)}>
                <option value="">Seleccionar...</option>
                {estadios.map((e) => (
                  <option key={e.idEstadio} value={e.idEstadio}>
                    {e.nombre} ({e.nombrePais})
                  </option>
                ))}
              </select>
            </div>

            <div className={styles.row}>
              <div className={styles.field}>
                <label>Equipo local</label>
                <select value={equipoLocal} onChange={(e) => setEquipoLocal(e.target.value)}>
                  <option value="">Seleccionar...</option>
                  {equipos.map((eq) => (
                    <option key={eq.nombre} value={eq.nombre}>{eq.nombre}</option>
                  ))}
                </select>
              </div>
              <div className={styles.field}>
                <label>Equipo visitante</label>
                <select value={equipoVisitante} onChange={(e) => setEquipoVisitante(e.target.value)}>
                  <option value="">Seleccionar...</option>
                  {equipos.map((eq) => (
                    <option key={eq.nombre} value={eq.nombre}>{eq.nombre}</option>
                  ))}
                </select>
              </div>
            </div>

            <div className={styles.row}>
              <div className={styles.field}>
                <label>Fecha</label>
                <input type="date" value={fechaEvento} onChange={(e) => setFechaEvento(e.target.value)} />
              </div>
              <div className={styles.field}>
                <label>Hora</label>
                <input type="time" value={horaEvento} onChange={(e) => setHoraEvento(e.target.value)} />
              </div>
            </div>

            {sectoresForm.length > 0 && (
              <div className={styles.sectorSelector}>
                <label>Sectores a habilitar</label>
                {sectoresForm.map((s) => (
                  <div key={s.letra} className={styles.sectorCheckRow}>
                    <input
                      type="checkbox"
                      checked={s.habilitado}
                      onChange={() => toggleSector(s.letra)}
                    />
                    <span>Sector {s.letra}</span>
                    {s.habilitado && (
                      <input
                        type="number"
                        placeholder="Precio"
                        value={s.costo}
                        onChange={(e) => cambiarCosto(s.letra, e.target.value)}
                      />
                    )}
                  </div>
                ))}
              </div>
            )}

            <div className={styles.modalButtons}>
              <button className={styles.cancelButton} onClick={() => setModalAbierto(false)}>
                Cancelar
              </button>
              <button className={styles.confirmButton} onClick={handleCrear}>
                Crear
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}