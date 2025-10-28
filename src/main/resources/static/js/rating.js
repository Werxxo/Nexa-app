document.addEventListener('DOMContentLoaded', function() {
    const container = document.querySelector('.rating-container');
    const servicioId = container.getAttribute('data-servicio-id');
    const rol = container.getAttribute('data-rol');
    const stars = document.querySelectorAll('#stars i');
    let rating = 0;

    console.log("Datos recibidos:", {servicioId, rol}); // Para depuración

    if (!servicioId || !rol) {
        console.error("Faltan parámetros requeridos");
        alert('Datos de servicio incompletos');
        return;
    }

    // Manejo de estrellas
    stars.forEach(star => {
        star.addEventListener('click', function() {
            rating = parseInt(this.getAttribute('data-rating'));
            stars.forEach((s, index) => {
                s.classList.toggle('fas', index < rating);
                s.classList.toggle('far', index >= rating);
            });
        });
    });

    document.getElementById('submitRating').addEventListener('click', function() {
        if (rating === 0) {
            alert('Por favor selecciona una calificación');
            return;
        }

        const comentarios = document.getElementById('comentarios').value;
        const csrfToken = document.querySelector('input[name="_csrf"]').value;

        console.log("Enviando:", {servicioId, rating, comentarios, rol});

        fetch('/rating/calificar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-CSRF-TOKEN': csrfToken
            },
            body: new URLSearchParams({
                servicioId: servicioId,
                calificacion: rating,
                comentarios: comentarios,
                rol: rol
            })
        })
        .then(response => {
            if (response.redirected) {
                // Si hay una redirección, seguirla manualmente
                window.location.href = response.url;
            } else if (!response.ok) {
                // Si la respuesta no es OK, lanzar un error
                return response.text().then(text => { throw new Error(text) });
            } else {
                // Si no hay redirección y la respuesta es OK (no debería pasar en este caso)
                console.log('Calificación enviada exitosamente');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error al enviar calificación: ' + error.message);
        });
    });
});