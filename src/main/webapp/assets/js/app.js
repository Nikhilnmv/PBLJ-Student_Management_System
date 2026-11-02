/*
 * Student Registration System - small client-side helpers.
 *
 * IMPORTANT: these checks only improve the user experience (instant
 * feedback, no accidental double clicks). They are NOT security. Every rule
 * is checked again on the server in ValidationUtil.java, because a user can
 * switch JavaScript off or send a request without using this page at all.
 */
(function () {
    'use strict';

    // ------------------------------------------------------------------
    // 1. Form validation (uses the browser's built-in HTML5 checks such as
    //    required, type="email", pattern and min/max) + double-submit guard
    // ------------------------------------------------------------------
    document.querySelectorAll('form.needs-validation').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
                form.classList.add('was-validated');
                var firstInvalid = form.querySelector(':invalid');
                if (firstInvalid) {
                    firstInvalid.focus();
                }
                return;
            }

            // Prevent a second submission if the user clicks the button twice
            if (form.dataset.submitting === 'true') {
                event.preventDefault();
                return;
            }
            form.dataset.submitting = 'true';
            var submitButton = form.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                var loadingText = submitButton.dataset.loadingText || 'Please wait...';
                submitButton.innerHTML =
                    '<span class="spinner-border spinner-border-sm me-2" aria-hidden="true"></span>' + loadingText;
            }
        });
    });

    // When the user edits a field that the server marked as wrong, remove the red outline
    document.querySelectorAll('.is-invalid').forEach(function (field) {
        var clearError = function () {
            field.classList.remove('is-invalid');
        };
        field.addEventListener('input', clearError);
        field.addEventListener('change', clearError);
    });
    document.querySelectorAll('.choice-group.is-invalid input').forEach(function (radio) {
        radio.addEventListener('change', function () {
            radio.closest('.choice-group').classList.remove('is-invalid');
        });
    });

    // Roll numbers are stored in capitals, so show them in capitals while typing
    document.querySelectorAll('input[data-uppercase]').forEach(function (input) {
        input.addEventListener('input', function () {
            var position = input.selectionStart;
            input.value = input.value.toUpperCase();
            input.setSelectionRange(position, position);
        });
    });

    // ------------------------------------------------------------------
    // 2. Confirmation dialog before dangerous actions (e.g. delete).
    //    Any <form data-confirm="message"> asks for confirmation first.
    // ------------------------------------------------------------------
    var modalElement = document.getElementById('confirmModal');
    var confirmModal = (modalElement && window.bootstrap) ? new bootstrap.Modal(modalElement) : null;
    var pendingForm = null;

    document.querySelectorAll('form[data-confirm]').forEach(function (form) {
        form.addEventListener('submit', function (event) {
            event.preventDefault();
            var message = form.dataset.confirm;

            if (!confirmModal) {
                // Fallback: the browser's simple confirm box
                if (window.confirm(message)) {
                    form.submit();
                }
                return;
            }

            pendingForm = form;
            modalElement.querySelector('[data-confirm-title]').textContent = form.dataset.confirmTitle || 'Are you sure?';
            modalElement.querySelector('[data-confirm-message]').textContent = message;
            var okButton = modalElement.querySelector('[data-confirm-ok]');
            okButton.textContent = form.dataset.confirmButton || 'Yes, continue';
            okButton.className = 'btn ' + (form.dataset.confirmClass || 'btn-danger');
            confirmModal.show();
        });
    });

    if (modalElement) {
        modalElement.querySelector('[data-confirm-ok]').addEventListener('click', function () {
            if (pendingForm) {
                // form.submit() does not fire the submit event again, so no loop
                pendingForm.submit();
                pendingForm = null;
            }
            confirmModal.hide();
        });
    }

    // ------------------------------------------------------------------
    // 3. Print buttons: <button data-action="print">
    // ------------------------------------------------------------------
    document.querySelectorAll('[data-action="print"]').forEach(function (button) {
        button.addEventListener('click', function () {
            window.print();
        });
    });
})();
