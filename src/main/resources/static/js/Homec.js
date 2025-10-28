// Script modificado para mostrar todo inmediatamente
document.addEventListener('DOMContentLoaded', function() {
    // Añade la clase visible a todas las secciones al cargar
    document.querySelectorAll('section').forEach(section => {
      section.classList.add('visible');
    });

    // Mobile menu toggle (mantén igual)
    const menuToggler = document.getElementById('menu-toggler');
    const menu = document.querySelector('.all-links');
    const hamburgerBtn = document.getElementById('hamburger-btn');

    hamburgerBtn.addEventListener('click', function() {
      menu.classList.toggle('active');
    });

    // Close menu when clicking on a link (mantén igual)
    document.querySelectorAll('.all-links a').forEach(link => {
      link.addEventListener('click', () => {
        menu.classList.remove('active');
        menuToggler.checked = false;
      });
    });

    // Navbar scroll effect (mantén igual)
    window.addEventListener('scroll', function() {
      const header = document.querySelector('header');
      if (window.scrollY > 50) {
        header.style.boxShadow = '0 4px 20px rgba(0, 0, 0, 0.15)';
      } else {
        header.style.boxShadow = '0 2px 10px rgba(0, 0, 0, 0.1)';
      }
    });

    // Smooth scrolling for anchor links (mantén igual)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
      anchor.addEventListener('click', function(e) {
        e.preventDefault();
        
        const targetId = this.getAttribute('href');
        if (targetId === '#') return;
        
        const targetElement = document.querySelector(targetId);
        if (targetElement) {
          window.scrollTo({
            top: targetElement.offsetTop - document.querySelector('header').offsetHeight,
            behavior: 'smooth'
          });
        }
      });
    });
  });