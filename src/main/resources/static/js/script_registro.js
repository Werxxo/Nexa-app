document.addEventListener("DOMContentLoaded", function() {
    const container = document.querySelector(".container");
    const btnSignIn = document.getElementById("btn-sign-in");
    const btnSignUp = document.getElementById("btn-sign-up");
    const btnNext = document.getElementById("btn-next");
    const btnPrev = document.getElementById("btn-prev");
    const formSections = document.querySelectorAll("#conductorForm .form-section");
    let currentSection = 0;

    // Mostrar/ocultar formularios principales
    btnSignIn.addEventListener("click", () => {
        container.classList.remove("toggle");
        resetConductorForm();
    });

    btnSignUp.addEventListener("click", () => {
        container.classList.add("toggle");
        resetConductorForm();
    });

    // Navegación entre secciones del formulario de conductor
    if (btnNext) {
        btnNext.addEventListener("click", () => {
            if (validateCurrentSection()) {

                // LA CORRECCIÓN: Usa .classList en lugar de .style.display
                formSections[currentSection].classList.remove("active");
                currentSection++;
                formSections[currentSection].classList.add("active");

                updateNavigationButtons();
            }
        });
    }

    // Botón Anterior
    if (btnPrev) {
        btnPrev.addEventListener("click", () => {

            // LA CORRECCIÓN: Usa .classList en lugar de .style.display
            formSections[currentSection].classList.remove("active");
            currentSection--;
            formSections[currentSection].classList.add("active");

            updateNavigationButtons();
        });
    }

    // Funciones auxiliares
    function validateCurrentSection() {
        const currentInputs = formSections[currentSection].querySelectorAll("input[required]");
        let isValid = true;

        currentInputs.forEach(input => {
            if (!input.value.trim()) {
                input.style.border = "1px solid red";
                isValid = false;
            } else {
                input.style.border = "";
            }
        });

        if (!isValid) {
            alert("Por favor complete todos los campos requeridos");
        }
        return isValid;
    }

    function updateNavigationButtons() {
        // Esta lógica oculta/muestra los botones Siguiente/Anterior
        if (currentSection === formSections.length - 1) {
            btnNext.style.display = "none";
        } else {
            btnNext.style.display = "block";
        }

        if (currentSection > 0) {
            btnPrev.style.display = "block";
        } else {
            btnPrev.style.display = "none";
        }
    }

    function resetConductorForm() {
        // Resetea el formulario de conductor al cambiar entre pestañas
        currentSection = 0;
        formSections.forEach((section, index) => {
            // LA CORRECCIÓN: Usa .classList
            if (index === 0) {
                section.classList.add("active");
            } else {
                section.classList.remove("active");
            }
        });
        updateNavigationButtons();
    }
});