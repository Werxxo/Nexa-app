// Variable global para el gráfico de registros
let registrosChart;

document.addEventListener("DOMContentLoaded", () => {
  initDashboard();
  setupEventListeners();
});

function initDashboard() {
  // Configurar nombre de usuario
  const userNameSpan = document.getElementById("user-name");
  if (userNameSpan) {
    const nombreUsuario = "Administrador";
    const iniciales = nombreUsuario.split(' ').map(n => n[0]).join('').toUpperCase();
    userNameSpan.dataset.nombre = nombreUsuario;
    userNameSpan.dataset.initiales = iniciales;
    userNameSpan.textContent = nombreUsuario;
  }

  // Configurar sección activa
  const currentSection = window.location.hash.substring(1) || 'home';
  showSection(currentSection);
  highlightActiveSidebarItem(currentSection);

  // Inicializar gráfico de registros
  //setupRegistrosChart();
}


function setupEventListeners() {
  document.addEventListener('click', (e) => {
    const modal = document.getElementById("infoModal");
    if (e.target === modal && modal) closeModal();
  });

  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });
}

function toggleSidebar() {
  const sidebar = document.getElementById("sidebar");
  if (!sidebar) return;

  const icon = sidebar.querySelector(".toggle-btn i");
  const userName = document.getElementById("user-name");

  if (icon && userName) {
    sidebar.classList.toggle("collapsed");
    icon.classList.toggle("fa-angle-double-left");
    icon.classList.toggle("fa-angle-double-right");

    userName.textContent = sidebar.classList.contains("collapsed")
      ? userName.dataset.initiales || "ADM"
      : userName.dataset.nombre || "Administrador";
  }
}

function showSection(sectionId) {
  if (!sectionId) return;
  
  document.querySelectorAll(".section").forEach((section) => {
    section.classList.remove("active");
  });

  const section = document.getElementById(sectionId);
  if (section) {
    section.classList.add("active");
    window.location.hash = sectionId;
    highlightActiveSidebarItem(sectionId);
  }
}

function highlightActiveSidebarItem(sectionId) {
  if (!sectionId) return;
  
  const sidebarItems = document.querySelectorAll("#sidebar li");
  if (!sidebarItems.length) return;

  sidebarItems.forEach(item => item.classList.remove("active"));
  
  const activeItem = document.querySelector(`#sidebar li[onclick*="${sectionId}"]`);
  if (activeItem) activeItem.classList.add("active");
}

function filterTable(status) {
  if (!status) return;
  
  const rows = document.querySelectorAll("#tabla-conductores tbody tr");
  const buttons = document.querySelectorAll(".btn-filter");

  if (!rows.length || !buttons.length) return;

  buttons.forEach(btn => btn.classList.remove("active"));
  
  const activeButton = document.querySelector(`.btn-filter[onclick*="${status}"]`);
  if (activeButton) activeButton.classList.add("active");

  rows.forEach(row => {
    const rowStatus = row.dataset.status;
    let shouldShow = false;

    switch(status) {
      case 'all': shouldShow = true; break;
      case 'pending': shouldShow = rowStatus === 'pendiente'; break;
      case 'approved': shouldShow = rowStatus === 'aprobado'; break;
      case 'rejected': shouldShow = rowStatus === 'rechazado'; break;
      default: shouldShow = rowStatus === status;
    }

    row.style.display = shouldShow ? "" : "none";
  });
}

function openDriverModal(element) {
  if (!element) return;
  
  const modal = document.getElementById("infoModal");
  const modalActions = document.getElementById("modal-actions");
  const row = element.closest('tr');

  if (!modal || !modalActions || !row) return;

  const cells = row.querySelectorAll('td');
  if (!cells.length) return;

  const nombre = cells[0]?.textContent.trim() || 'No disponible';
  const email = cells[1]?.textContent.trim() || 'No disponible';
  const telefono = cells[2]?.textContent.trim() || 'No disponible';
  const licencia = cells[3]?.textContent.trim() || 'No disponible';

  let soat = element.getAttribute('data-soat') || 'No disponible';
  let tarjeta = element.getAttribute('data-tarjeta') || 'No disponible';

  
  document.getElementById("modal-nombre").textContent = nombre;
  document.getElementById("modal-email").textContent = email;
  document.getElementById("modal-telefono").textContent = telefono;
  document.getElementById("modal-licencia").textContent = licencia;
  document.getElementById("modal-soat").textContent = soat;
  document.getElementById("modal-tarjeta").textContent = tarjeta;

  modalActions.innerHTML = '';
  const rowStatus = row.dataset.status;

if (rowStatus === 'pendiente') {
    // Obtener el token CSRF desde el meta tag o desde un formulario existente
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content ||
                      document.querySelector('input[name="_csrf"]')?.value;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // Formulario de Aprobar
    const formApprove = document.createElement("form");
    formApprove.method = "POST";
    formApprove.action = "/admin/aprobar-conductor";

    const inputApprove = document.createElement("input");
    inputApprove.type = "hidden";
    inputApprove.name = "email";
    inputApprove.value = email;

    // ⭐ AGREGAR TOKEN CSRF
    const csrfInputApprove = document.createElement("input");
    csrfInputApprove.type = "hidden";
    csrfInputApprove.name = "_csrf";
    csrfInputApprove.value = csrfToken;

    const btnApprove = document.createElement("button");
    btnApprove.type = "submit";
    btnApprove.className = "btn-approve";
    btnApprove.innerHTML = '<i class="fas fa-check"></i> Aprobar';

    formApprove.appendChild(inputApprove);
    formApprove.appendChild(csrfInputApprove);
    formApprove.appendChild(btnApprove);
    modalActions.appendChild(formApprove);

    // Formulario de Rechazar
    const formReject = document.createElement("form");
    formReject.method = "POST";
    formReject.action = "/admin/rechazar-conductor";

    const inputReject = document.createElement("input");
    inputReject.type = "hidden";
    inputReject.name = "email";
    inputReject.value = email;

    // ⭐ AGREGAR TOKEN CSRF
    const csrfInputReject = document.createElement("input");
    csrfInputReject.type = "hidden";
    csrfInputReject.name = "_csrf";
    csrfInputReject.value = csrfToken;

    const btnReject = document.createElement("button");
    btnReject.type = "submit";
    btnReject.className = "btn-reject";
    btnReject.innerHTML = '<i class="fas fa-times"></i> Rechazar';

    formReject.appendChild(inputReject);
    formReject.appendChild(csrfInputReject); // ⭐ AGREGAR ESTO
    formReject.appendChild(btnReject);
    modalActions.appendChild(formReject);
}

  modal.style.display = "flex";
  document.body.style.overflow = "hidden";
}

function closeModal() {
  const modal = document.getElementById("infoModal");
  if (modal) {
    modal.style.display = "none";
    document.body.style.overflow = "auto";
  }
}

async function fetchDashboardData() {
  try {
    const response = await fetch('/admin/dashboard/actualizar');
    if (!response.ok) throw new Error('Error en la respuesta del servidor');
    
    const data = await response.json();
    
    // Actualizar los contadores
    const pasajerosElement = document.getElementById("count-pasajeros");
    const conductoresElement = document.getElementById("count-conductores");
    const pendientesElement = document.getElementById("count-pendientes");
    
    if (pasajerosElement) pasajerosElement.textContent = data.pasajeros || 0;
    if (conductoresElement) conductoresElement.textContent = data.conductoresAprobados || 0;
    if (pendientesElement) pendientesElement.textContent = data.conductoresPendientes || 0;
    
    showToast('Datos actualizados correctamente', 'success');
    
  } catch (error) {
    console.error("Error al actualizar los datos:", error);
    showToast('Error al actualizar los datos', 'error');
  }
}

async function refreshValidationTable() {
  try {
    const response = await fetch('/admin/conductores/actualizar');
    if (!response.ok) throw new Error('Error al actualizar la tabla');
    
    // implementar la lógica para actualizar la tabla con los nuevos datos
    showToast('Tabla de conductores actualizada', 'success');
  } catch (error) {
    console.error("Error al actualizar la tabla:", error);
    showToast('Error al actualizar la tabla', 'error');
  }
}

function changeTheme(theme) {
  const lightOption = document.querySelector('.theme-option.light');
  const darkOption = document.querySelector('.theme-option.dark');
  
  if (!lightOption || !darkOption) return;

  if (theme === 'light') {
    lightOption.classList.add('active');
    darkOption.classList.remove('active');
    document.documentElement.style.setProperty('--light', '#f7f7f7');
    document.documentElement.style.setProperty('--white', '#ffffff');
    document.documentElement.style.setProperty('--dark', '#333333');
  } else {
    darkOption.classList.add('active');
    lightOption.classList.remove('active');
    document.documentElement.style.setProperty('--light', '#1a1a1a');
    document.documentElement.style.setProperty('--white', '#2d2d2d');
    document.documentElement.style.setProperty('--dark', '#f0f0f0');
  }
}

function showToast(message, type) {
  if (!message || !type) return;
  
  try {
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    toast.innerHTML = `
      <i class="fas ${type === 'success' ? 'fa-check-circle' : 'fa-exclamation-circle'}"></i>
      <span>${message}</span>
    `;

    document.body.appendChild(toast);
    
    setTimeout(() => toast.classList.add('show'), 10);
    setTimeout(() => {
      toast.classList.remove('show');
      setTimeout(() => toast.remove(), 300);
    }, 3000);
  } catch (error) {
    console.error("Error al mostrar toast:", error);
  }
}


// Asignar funciones al objeto window
window.toggleSidebar = toggleSidebar;
window.showSection = showSection;
window.filterTable = filterTable;
window.openDriverModal = openDriverModal;
window.closeModal = closeModal;
window.refreshValidationTable = refreshValidationTable;
window.changeTheme = changeTheme;
window.fetchDashboardData = fetchDashboardData;