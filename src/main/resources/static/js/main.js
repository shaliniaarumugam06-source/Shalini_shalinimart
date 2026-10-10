// Main JavaScript for Shalini SmartMart

document.addEventListener("DOMContentLoaded", function () {
    // Auto dismiss SUCCESS alerts only after 5 seconds so user can clearly read error messages
    setTimeout(function () {
        let successAlerts = document.querySelectorAll('.alert-success.alert-dismissible');
        if (typeof bootstrap !== 'undefined' && bootstrap.Alert) {
            successAlerts.forEach(function (alert) {
                try {
                    let bsAlert = new bootstrap.Alert(alert);
                    bsAlert.close();
                } catch (e) {
                    // silently handle if already closed
                }
            });
        }
    }, 5000);

    // Registration password matching check
    const regForm = document.getElementById("registerForm");
    if (regForm) {
        regForm.addEventListener("submit", function (e) {
            const pwd = document.getElementById("password");
            const cpwd = document.getElementById("confirmPassword");
            if (pwd && cpwd && pwd.value !== cpwd.value) {
                e.preventDefault();
                alert("Passwords do not match. Please re-enter identical passwords.");
                cpwd.focus();
                return false;
            }
        });
    }
});

// Helper for quantity change buttons in cart
function changeCartQty(button, delta) {
    let input = button.closest('.input-group').querySelector('input[name="quantity"]');
    if (input) {
        let currentVal = parseInt(input.value) || 1;
        let newVal = currentVal + delta;
        if (newVal >= 1) {
            input.value = newVal;
            button.closest('form').submit();
        }
    }
}
