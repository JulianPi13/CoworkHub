// CoWorkHub - frontend en JavaScript puro que consume la API REST.
(() => {
  const $ = (sel, root = document) => root.querySelector(sel);
  const $$ = (sel, root = document) => [...root.querySelectorAll(sel)];
  const esc = (v) => String(v ?? '').replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
  const money = (v) => v == null ? '—' : Number(v).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
  const dt = (v) => v ? new Date(v).toLocaleString('es-CO', { dateStyle: 'medium', timeStyle: 'short' }) : '—';
  const hm = (v) => (v || '').slice(0, 5);
  const today = () => new Date(Date.now() - new Date().getTimezoneOffset() * 60000).toISOString().slice(0, 10);
  const badge = (s) => `<span class="badge ${esc(s)}">${esc(s)}</span>`;

  const state = { user: null, sedes: [], salas: [], equipos: [], servicios: [], planes: [], miembros: [] };
  const isStaff = () => ['ADMIN', 'RECEPTIONIST'].includes(state.user?.rol);
  const isAdmin = () => state.user?.rol === 'ADMIN';

  // ---------------------------------------------------------------- toasts / modal
  function toast(msg, type = 'ok', detail = '') {
    const el = document.createElement('div');
    el.className = `toast ${type}`;
    el.innerHTML = `${esc(msg)}${detail ? `<small>${esc(detail)}</small>` : ''}`;
    $('#toasts').append(el);
    setTimeout(() => el.remove(), 5000);
  }
  const fail = (e) => toast(e.message, 'error', e.codigo ? `Código: ${e.codigo}` : '');

  function modal(title, html) {
    $('#modal-title').textContent = title;
    $('#modal-body').innerHTML = html;
    $('#modal').showModal();
    return $('#modal-body');
  }
  $('#modal-close').addEventListener('click', () => $('#modal').close());

  const confirmAction = (msg) => window.confirm(msg);
  const formData = (form) => Object.fromEntries(new FormData(form).entries());

  // ---------------------------------------------------------------- sesión
  $('#login-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    try {
      const data = await Api.post('/auth/login', formData(ev.target));
      Api.saveSession(data);
      await start();
    } catch (e) { fail(e); }
  });

  $('#logout').addEventListener('click', logout);
  window.addEventListener('coworkhub:logout', () => { logout(); toast('La sesión venció, inicie sesión de nuevo', 'error'); });

  function logout() {
    Api.clearSession();
    state.user = null;
    $('#app-view').classList.add('hidden');
    $('#login-view').classList.remove('hidden');
  }

  async function start() {
    try {
      state.user = await Api.get('/auth/perfil');
      state.user.rol = state.user.rol || Api.session()?.rol;
    } catch { logout(); return; }
    $('#login-view').classList.add('hidden');
    $('#app-view').classList.remove('hidden');
    $('#user-label').innerHTML = `${esc(state.user.usuario)} · <span class="badge off">${esc(state.user.rol)}</span>`;
    buildNav();
    await loadCatalogs();
    show('inicio');
  }

  // ---------------------------------------------------------------- navegación
  const VIEWS = [
    { id: 'inicio', label: 'Inicio', render: renderHome },
    { id: 'disponibilidad', label: 'Disponibilidad', render: renderAvailability },
    { id: 'reservas', label: 'Reservas', render: renderBookings },
    { id: 'miembros', label: 'Miembros', render: renderMembers, staff: true },
    { id: 'catalogos', label: 'Catálogos', render: renderCatalogs },
  ];

  function buildNav() {
    $('#nav').innerHTML = VIEWS.filter((v) => !v.staff || isStaff())
      .map((v) => `<button data-go="${v.id}">${v.label}</button>`).join('');
    $$('#nav button').forEach((b) => b.addEventListener('click', () => show(b.dataset.go)));
  }

  function show(id) {
    $$('.view').forEach((v) => v.classList.toggle('active', v.dataset.view === id));
    $$('#nav button').forEach((b) => b.classList.toggle('active', b.dataset.go === id));
    VIEWS.find((v) => v.id === id)?.render();
  }

  async function loadCatalogs() {
    try {
      [state.sedes, state.salas, state.equipos, state.servicios, state.planes] = await Promise.all([
        Api.get('/api/sedes'), Api.get('/api/salas'), Api.get('/api/equipamientos'),
        Api.get('/api/servicios-adicionales'), Api.get('/api/planes')]);
      state.miembros = isStaff() ? await Api.get('/api/miembros') : [];
    } catch (e) { fail(e); }
  }

  const options = (list, label, selected, empty) =>
    (empty !== undefined ? `<option value="">${esc(empty)}</option>` : '') +
    list.map((x) => `<option value="${x.id}" ${String(x.id) === String(selected) ? 'selected' : ''}>${esc(label(x))}</option>`).join('');

  // ---------------------------------------------------------------- inicio
  async function renderHome() {
    const cards = [
      ['Sedes', state.sedes.length], ['Salas', state.salas.length],
      ['Servicios', state.servicios.length], ['Planes', state.planes.length],
    ];
    if (isStaff()) cards.push(['Miembros', state.miembros.length]);
    $('#home-cards').innerHTML = cards.map(([k, v]) => `<div class="card"><div class="muted">${k}</div><div class="stat">${v}</div></div>`).join('');

    const box = $('#home-consumo');
    if (state.user.miembroId) {
      box.innerHTML = `<div class="card"><h3>Mi consumo del mes</h3><div id="my-consumo">Cargando...</div></div>`;
      showConsumption(state.user.miembroId, $('#my-consumo'));
    } else {
      box.innerHTML = `<div class="card"><h3>Bienvenido, ${esc(state.user.usuario)}</h3>
        <p class="muted">Rol ${esc(state.user.rol)}. Use el menú para gestionar ${isAdmin() ? 'catálogos, miembros y reservas' : 'miembros y reservas'}.</p></div>`;
    }
  }

  async function showConsumption(memberId, target, month) {
    try {
      const c = await Api.get(`/api/miembros/${memberId}/consumo${Api.qs({ mes: month })}`);
      target.innerHTML = `<dl class="kv">
        <dt>Mes</dt><dd>${esc(c.mes)}</dd><dt>Plan</dt><dd>${esc(c.plan)}</dd>
        <dt>Horas incluidas</dt><dd>${c.horasIncluidas}</dd><dt>Horas usadas</dt><dd>${c.horasUsadas}</dd>
        <dt>Horas restantes</dt><dd>${c.horasRestantes}</dd><dt>Horas excedentes</dt><dd>${c.horasExcedentes}</dd></dl>`;
    } catch (e) { target.textContent = e.message; }
  }

  // ---------------------------------------------------------------- disponibilidad
  function renderAvailability() {
    const f = $('#availability-form');
    f.sedeId.innerHTML = options(state.sedes, (s) => `${s.nombre} (${hm(s.horaApertura)}-${hm(s.horaCierre)})`, f.sedeId.value);
    if (!f.fecha.value) f.fecha.value = today();
    $('#availability-equipment').innerHTML = '<legend>Equipamiento</legend>' + state.equipos
      .map((e) => `<label><input type="checkbox" value="${e.id}"> ${esc(e.nombre)}</label>`).join('');
  }

  $('#availability-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const d = formData(ev.target);
    const equip = $$('#availability-equipment input:checked').map((i) => i.value);
    try {
      const rooms = await Api.get('/api/disponibilidad' + Api.qs({ ...d, equipamientoIds: equip }));
      $('#availability-result').innerHTML = rooms.length ? `<div class="card"><table>
        <tr><th>Sala</th><th>Tipo</th><th>Capacidad</th><th>Tarifa/h</th><th>Equipamiento</th><th></th></tr>
        ${rooms.map((r) => `<tr><td>${esc(r.nombre)}</td><td>${esc(r.tipo)}</td><td>${r.capacidad}</td><td>${money(r.tarifaHora)}</td>
          <td>${r.equipamientos.map((e) => esc(e.nombre)).join(', ') || '—'}</td>
          <td><button class="btn sm primary" data-book="${r.id}">Reservar</button></td></tr>`).join('')}
      </table></div>` : '<div class="card empty">No hay salas libres con esos criterios</div>';
      $$('[data-book]').forEach((b) => b.addEventListener('click', () => {
        show('reservas');
        const bf = $('#booking-form');
        bf.salaId.value = b.dataset.book;
        bf.fecha.value = d.fecha; bf.horaInicio.value = d.horaInicio; bf.horaFin.value = d.horaFin;
      }));
    } catch (e) { fail(e); }
  });

  // ---------------------------------------------------------------- reservas
  function renderBookings() {
    const bf = $('#booking-form');
    const memberOpts = isStaff()
      ? options(state.miembros.filter((m) => m.estado === 'ACTIVE'), (m) => `${m.nombreCompleto} (${m.documento})`, bf.miembroId.value)
      : `<option value="${state.user.miembroId}">${esc(state.user.usuario)}</option>`;
    bf.miembroId.innerHTML = memberOpts;
    bf.salaId.innerHTML = options(state.salas.filter((s) => s.activa), (s) => `${s.nombre} · ${s.sedeNombre} (cap. ${s.capacidad})`, bf.salaId.value);
    if (!bf.fecha.value) bf.fecha.value = today();
    $('#booking-services').innerHTML = '<legend>Servicios adicionales (cantidad)</legend>' + state.servicios
      .map((s) => `<label>${esc(s.nombre)} (${money(s.precioUnitario)}) <input type="number" min="0" value="0" data-service="${s.id}"></label>`).join('');

    const ff = $('#booking-filter');
    ff.miembroId.innerHTML = isStaff()
      ? options(state.miembros, (m) => m.nombreCompleto, ff.miembroId.value, '—')
      : `<option value="${state.user.miembroId}">Mis reservas</option>`;
    ff.salaId.innerHTML = options(state.salas, (s) => s.nombre, ff.salaId.value, '—');
    ff.salaId.closest('label').classList.toggle('hidden', !isStaff());
    ff.fecha.closest('label').classList.toggle('hidden', !isStaff());
    ff.estado.closest('label').classList.toggle('hidden', !isStaff());
    loadBookings();
  }

  $('#booking-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const d = formData(ev.target);
    const servicios = $$('#booking-services [data-service]')
      .filter((i) => Number(i.value) > 0)
      .map((i) => ({ servicioId: Number(i.dataset.service), cantidad: Number(i.value) }));
    try {
      const r = await Api.post('/api/reservas', {
        salaId: Number(d.salaId), miembroId: Number(d.miembroId),
        inicio: `${d.fecha}T${d.horaInicio}:00`, fin: `${d.fecha}T${d.horaFin}:00`,
        asistentes: Number(d.asistentes), servicios,
      });
      toast(`Reserva #${r.id} creada. Total: ${money(r.costoTotal)}`);
      loadBookings();
      openBooking(r.id);
    } catch (e) { fail(e); }
  });

  $('#booking-filter').addEventListener('submit', (ev) => { ev.preventDefault(); loadBookings(); });

  async function loadBookings() {
    const d = formData($('#booking-filter'));
    if (!isStaff()) d.miembroId = state.user.miembroId;
    try {
      const list = await Api.get('/api/reservas' + Api.qs(d));
      $('#booking-list').innerHTML = list.length ? `<table>
        <tr><th>#</th><th>Sala</th><th>Miembro</th><th>Inicio</th><th>Fin</th><th>Estado</th><th>Total</th><th></th></tr>
        ${list.map((b) => `<tr>
          <td>${b.id}</td><td>${esc(b.salaNombre)}</td><td>${esc(b.miembroNombre)}</td>
          <td>${dt(b.inicio)}</td><td>${dt(b.fin)}</td><td>${badge(b.estado)}</td><td>${money(b.costoTotal)}</td>
          <td class="actions">${bookingActions(b)}</td></tr>`).join('')}
      </table>` : '<div class="empty">No hay reservas para este filtro</div>';
      bindBookingActions($('#booking-list'));
    } catch (e) { fail(e); }
  }

  function bookingActions(b) {
    const a = [`<button class="btn sm" data-act="ver" data-id="${b.id}">Ver</button>`];
    if (isStaff() && b.estado === 'PENDING') a.push(`<button class="btn sm ok" data-act="confirmar" data-id="${b.id}">Confirmar</button>`);
    if (isStaff() && b.estado === 'CONFIRMED') a.push(`<button class="btn sm ok" data-act="completar" data-id="${b.id}">Completar</button>`);
    if (['PENDING', 'CONFIRMED'].includes(b.estado)) a.push(`<button class="btn sm danger" data-act="cancelar" data-id="${b.id}">Cancelar</button>`);
    return a.join('');
  }

  function bindBookingActions(root) {
    $$('[data-act]', root).forEach((btn) => btn.addEventListener('click', async () => {
      const { act, id } = btn.dataset;
      if (act === 'ver') return openBooking(id);
      if (act === 'cancelar' && !confirmAction(`¿Cancelar la reserva #${id}? Se aplicará la política de cancelación (RN-08).`)) return;
      try {
        const r = await Api.post(`/api/reservas/${id}/${act}`);
        toast(act === 'cancelar' ? `Reserva #${id} cancelada. Cargo: ${money(r.cargoCancelacion)}` : `Reserva #${id}: ${r.estado}`);
        loadBookings();
        if ($('#modal').open) openBooking(id);
      } catch (e) { fail(e); }
    }));
  }

  async function openBooking(id) {
    try {
      const b = await Api.get(`/api/reservas/${id}`);
      const editable = b.estado === 'PENDING';
      const body = modal(`Reserva #${b.id}`, `
        <dl class="kv">
          <dt>Sala</dt><dd>${esc(b.salaNombre)}</dd><dt>Miembro</dt><dd>${esc(b.miembroNombre)}</dd>
          <dt>Inicio</dt><dd>${dt(b.inicio)}</dd><dt>Fin</dt><dd>${dt(b.fin)}</dd>
          <dt>Asistentes</dt><dd>${b.asistentes}</dd><dt>Estado</dt><dd>${badge(b.estado)}</dd>
          <dt>Costo sala</dt><dd>${money(b.costoSala)}</dd><dt>Costo servicios</dt><dd>${money(b.costoServicios)}</dd>
          <dt>Total</dt><dd><strong>${money(b.costoTotal)}</strong></dd>
          ${b.cargoCancelacion != null ? `<dt>Cargo cancelación</dt><dd>${money(b.cargoCancelacion)} ${b.horasDevueltas ? '(horas devueltas)' : ''}</dd>` : ''}
        </dl>
        <h3>Servicios</h3>
        ${b.servicios.length ? `<table><tr><th>Servicio</th><th>Cant.</th><th>Precio aplicado</th><th>Subtotal</th><th></th></tr>
          ${b.servicios.map((s) => `<tr><td>${esc(s.servicioNombre)}</td><td>${s.cantidad}</td><td>${money(s.precioUnitarioAplicado)}</td>
            <td>${money(s.subtotal)}</td><td>${editable ? `<button class="btn sm danger" data-remove="${s.servicioId}">Quitar</button>` : ''}</td></tr>`).join('')}
        </table>` : '<p class="muted">Sin servicios</p>'}
        ${editable ? `<form id="add-service" class="filters" style="margin-top:12px">
          <label>Servicio <select name="servicioId">${options(state.servicios, (s) => `${s.nombre} (${money(s.precioUnitario)})`)}</select></label>
          <label>Cantidad <input type="number" name="cantidad" min="1" value="1"></label>
          <button class="btn primary">Agregar</button></form>` : '<p class="muted">Los servicios solo se pueden cambiar en estado PENDING (RN-10).</p>'}
        <div class="actions" style="margin-top:14px">${bookingActions(b).replace(/<button class="btn sm" data-act="ver"[^>]*>Ver<\/button>/, '')}</div>`);

      $$('[data-remove]', body).forEach((btn) => btn.addEventListener('click', async () => {
        try { await Api.del(`/api/reservas/${id}/servicios/${btn.dataset.remove}`); openBooking(id); loadBookings(); } catch (e) { fail(e); }
      }));
      $('#add-service', body)?.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const d = formData(ev.target);
        try {
          await Api.post(`/api/reservas/${id}/servicios`, { servicioId: Number(d.servicioId), cantidad: Number(d.cantidad) });
          openBooking(id); loadBookings();
        } catch (e) { fail(e); }
      });
      bindBookingActions(body);
    } catch (e) { fail(e); }
  }

  // ---------------------------------------------------------------- miembros
  function renderMembers() {
    const f = $('#member-form');
    f.planId.innerHTML = options(state.planes, (p) => `${p.nombre} (${p.horasIncluidasMes} h/mes)`, f.planId.value);
    loadMembers();
  }

  function resetMemberForm() {
    const f = $('#member-form');
    f.reset();
    f.id.value = '';
    f.documento.disabled = false;
    $('#member-form-title').textContent = 'Registrar miembro';
    $('.only-create', f).classList.remove('hidden');
    $$('.only-create input', f).forEach((i) => { i.required = true; });
  }
  $('#member-form-reset').addEventListener('click', resetMemberForm);

  $('#member-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const d = formData(ev.target);
    try {
      if (d.id) {
        await Api.put(`/api/miembros/${d.id}`, { nombreCompleto: d.nombreCompleto, email: d.email, planId: Number(d.planId) });
        toast('Miembro actualizado');
      } else {
        await Api.post('/api/miembros', { ...d, planId: Number(d.planId) });
        toast('Miembro y usuario registrados');
      }
      resetMemberForm();
      state.miembros = await Api.get('/api/miembros');
      loadMembers();
    } catch (e) { fail(e); }
  });

  $('#member-filter').addEventListener('submit', (ev) => { ev.preventDefault(); loadMembers(); });

  async function loadMembers() {
    try {
      const list = await Api.get('/api/miembros' + Api.qs(formData($('#member-filter'))));
      $('#member-list').innerHTML = list.length ? `<table>
        <tr><th>#</th><th>Documento</th><th>Nombre</th><th>Email</th><th>Plan</th><th>Usuario</th><th>Estado</th><th></th></tr>
        ${list.map((m) => `<tr><td>${m.id}</td><td>${esc(m.documento)}</td><td>${esc(m.nombreCompleto)}</td><td>${esc(m.email)}</td>
          <td>${esc(m.planNombre)}</td><td>${esc(m.usuario || '—')}</td><td>${badge(m.estado)}</td>
          <td class="actions">
            <button class="btn sm" data-m="editar" data-id="${m.id}">Editar</button>
            <button class="btn sm" data-m="consumo" data-id="${m.id}">Consumo</button>
            ${m.estado === 'ACTIVE' ? `<button class="btn sm danger" data-m="suspender" data-id="${m.id}">Suspender</button>`
                                    : `<button class="btn sm ok" data-m="activar" data-id="${m.id}">Activar</button>`}
            <button class="btn sm danger" data-m="eliminar" data-id="${m.id}">Eliminar</button>
          </td></tr>`).join('')}</table>` : '<div class="empty">Sin resultados</div>';

      $$('[data-m]').forEach((btn) => btn.addEventListener('click', async () => {
        const { m: act, id } = btn.dataset;
        const member = list.find((x) => String(x.id) === id);
        try {
          if (act === 'editar') {
            const f = $('#member-form');
            f.id.value = id; f.documento.value = member.documento; f.documento.disabled = true;
            f.nombreCompleto.value = member.nombreCompleto; f.email.value = member.email; f.planId.value = member.planId;
            $('.only-create', f).classList.add('hidden');
            $$('.only-create input', f).forEach((i) => { i.required = false; });
            $('#member-form-title').textContent = `Editar miembro #${id}`;
            return;
          }
          if (act === 'consumo') {
            const body = modal(`Consumo de ${member.nombreCompleto}`, `
              <form class="filters" id="consumo-form"><label>Mes <input type="month" name="mes" value="${today().slice(0, 7)}"></label>
              <button class="btn">Ver</button></form><div id="consumo-box">Cargando...</div>`);
            showConsumption(id, $('#consumo-box', body));
            $('#consumo-form', body).addEventListener('submit', (ev) => {
              ev.preventDefault(); showConsumption(id, $('#consumo-box', body), ev.target.mes.value);
            });
            return;
          }
          if (act === 'eliminar') {
            if (!confirmAction(`¿Eliminar a ${member.nombreCompleto}? También se elimina su usuario.`)) return;
            await Api.del(`/api/miembros/${id}`);
          } else {
            await Api.patch(`/api/miembros/${id}/${act}`);
          }
          toast('Listo');
          state.miembros = await Api.get('/api/miembros');
          loadMembers();
        } catch (e) { fail(e); }
      }));
    } catch (e) { fail(e); }
  }

  // ---------------------------------------------------------------- catálogos
  const CATALOGS = {
    sedes: {
      label: 'Sedes', path: '/api/sedes', key: 'sedes',
      fields: [['nombre', 'Nombre'], ['ciudad', 'Ciudad'], ['direccion', 'Dirección'],
        ['horaApertura', 'Apertura', 'time'], ['horaCierre', 'Cierre', 'time']],
      cols: (x) => [x.nombre, x.ciudad, x.direccion, `${hm(x.horaApertura)} - ${hm(x.horaCierre)}`],
      head: ['Nombre', 'Ciudad', 'Dirección', 'Horario'],
    },
    salas: {
      label: 'Salas', path: '/api/salas', key: 'salas',
      fields: [['nombre', 'Nombre'], ['tipo', 'Tipo', 'select:MEETING_ROOM,PRIVATE_OFFICE,DESK'], ['capacidad', 'Capacidad', 'number'],
        ['tarifaHora', 'Tarifa por hora', 'number'], ['sedeId', 'Sede', 'sede'], ['activa', 'Activa', 'checkbox'], ['equipamientoIds', 'Equipamiento', 'equipos']],
      cols: (x) => [x.nombre, x.tipo, x.capacidad, money(x.tarifaHora), x.sedeNombre,
        x.activa ? 'Sí' : 'No', x.equipamientos.map((e) => e.nombre).join(', ') || '—'],
      head: ['Nombre', 'Tipo', 'Cap.', 'Tarifa/h', 'Sede', 'Activa', 'Equipamiento'],
    },
    equipos: {
      label: 'Equipamiento', path: '/api/equipamientos', key: 'equipos',
      fields: [['nombre', 'Nombre']], cols: (x) => [x.nombre], head: ['Nombre'],
    },
    servicios: {
      label: 'Servicios adicionales', path: '/api/servicios-adicionales', key: 'servicios',
      fields: [['nombre', 'Nombre'], ['precioUnitario', 'Precio unitario', 'number']],
      cols: (x) => [x.nombre, money(x.precioUnitario)], head: ['Nombre', 'Precio'],
    },
    planes: {
      label: 'Planes', path: '/api/planes', key: 'planes',
      fields: [['nombre', 'Nombre'], ['horasIncluidasMes', 'Horas incluidas/mes', 'number'],
        ['descuentoExcedente', 'Descuento excedente (%)', 'number'], ['maxReservasActivas', 'Máx. reservas activas', 'number']],
      cols: (x) => [x.nombre, x.horasIncluidasMes, `${x.descuentoExcedente} %`, x.maxReservasActivas],
      head: ['Nombre', 'Horas/mes', 'Descuento', 'Máx. activas'],
    },
  };
  let currentCatalog = 'sedes';

  function renderCatalogs() {
    $('#catalog-tabs').innerHTML = Object.entries(CATALOGS)
      .map(([k, c]) => `<button data-cat="${k}" class="${k === currentCatalog ? 'active' : ''}">${c.label}</button>`).join('');
    $$('#catalog-tabs button').forEach((b) => b.addEventListener('click', () => { currentCatalog = b.dataset.cat; renderCatalogs(); }));
    renderCatalogForm();
    renderCatalogList();
  }

  function fieldHtml([name, label, type = 'text'], value) {
    if (type.startsWith('select:')) {
      return `<label>${label}<select name="${name}">${type.slice(7).split(',').map((o) => `<option ${o === value ? 'selected' : ''}>${o}</option>`).join('')}</select></label>`;
    }
    if (type === 'sede') return `<label>${label}<select name="${name}">${options(state.sedes, (s) => s.nombre, value)}</select></label>`;
    if (type === 'checkbox') return `<label style="flex-direction:row;align-items:center;gap:8px;color:var(--text)"><input type="checkbox" name="${name}" ${value !== false ? 'checked' : ''}> ${label}</label>`;
    if (type === 'equipos') {
      const sel = (value || []).map(String);
      return `<fieldset class="checks"><legend>${label}</legend>${state.equipos.map((e) =>
        `<label><input type="checkbox" name="${name}" value="${e.id}" ${sel.includes(String(e.id)) ? 'checked' : ''}> ${esc(e.nombre)}</label>`).join('')}</fieldset>`;
    }
    const step = type === 'time' ? 'step="60"' : type === 'number' ? 'step="any" min="0"' : '';
    return `<label>${label}<input name="${name}" type="${type}" ${step} required value="${esc(value ?? '')}"></label>`;
  }

  function renderCatalogForm(item) {
    const c = CATALOGS[currentCatalog];
    const form = $('#catalog-form');
    if (!isAdmin()) {
      form.innerHTML = `<h3>${c.label}</h3><p class="muted">Solo el ADMIN puede crear, modificar o eliminar catálogos.</p>`;
      return;
    }
    const values = item ? {
      ...item,
      horaApertura: hm(item.horaApertura), horaCierre: hm(item.horaCierre),
      equipamientoIds: item.equipamientos?.map((e) => e.id),
    } : {};
    form.innerHTML = `<h3>${item ? `Editar #${item.id}` : `Nuevo: ${c.label}`}</h3>
      ${c.fields.map((f) => fieldHtml(f, values[f[0]])).join('')}
      <div class="row"><button class="btn primary">Guardar</button><button type="button" class="btn ghost" id="cat-reset">Limpiar</button></div>`;
    form.dataset.id = item?.id || '';
    $('#cat-reset').addEventListener('click', () => renderCatalogForm());
  }

  $('#catalog-form').addEventListener('submit', async (ev) => {
    ev.preventDefault();
    const c = CATALOGS[currentCatalog];
    const form = ev.target;
    const body = {};
    c.fields.forEach(([name, , type = 'text']) => {
      if (type === 'checkbox') body[name] = form[name].checked;
      else if (type === 'equipos') body[name] = $$(`input[name="${name}"]:checked`, form).map((i) => Number(i.value));
      else if (type === 'number' || type === 'sede') body[name] = Number(form[name].value);
      else body[name] = form[name].value;
    });
    try {
      if (form.dataset.id) await Api.put(`${c.path}/${form.dataset.id}`, body);
      else await Api.post(c.path, body);
      toast('Guardado');
      state[c.key] = await Api.get(c.path);
      renderCatalogForm();
      renderCatalogList();
    } catch (e) { fail(e); }
  });

  function renderCatalogList() {
    const c = CATALOGS[currentCatalog];
    const list = state[c.key];
    $('#catalog-list').innerHTML = list.length ? `<table>
      <tr><th>#</th>${c.head.map((h) => `<th>${h}</th>`).join('')}${isAdmin() ? '<th></th>' : ''}</tr>
      ${list.map((x) => `<tr><td>${x.id}</td>${c.cols(x).map((v) => `<td>${esc(v)}</td>`).join('')}
        ${isAdmin() ? `<td class="actions"><button class="btn sm" data-edit="${x.id}">Editar</button>
          <button class="btn sm danger" data-delete="${x.id}">Eliminar</button></td>` : ''}</tr>`).join('')}
    </table>` : '<div class="empty">Sin registros</div>';
    $$('[data-edit]').forEach((b) => b.addEventListener('click', () => renderCatalogForm(list.find((x) => String(x.id) === b.dataset.edit))));
    $$('[data-delete]').forEach((b) => b.addEventListener('click', async () => {
      if (!confirmAction('¿Eliminar este registro?')) return;
      try {
        await Api.del(`${c.path}/${b.dataset.delete}`);
        toast('Eliminado');
        state[c.key] = await Api.get(c.path);
        renderCatalogList();
      } catch (e) { fail(e); }
    }));
  }

  // ---------------------------------------------------------------- arranque
  if (Api.session()?.token) start(); else logout();
})();
