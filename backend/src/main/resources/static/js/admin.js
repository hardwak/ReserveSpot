// Admin Panel JavaScript

// Autocomplete functionality for relation fields
function initAutocomplete(inputId, searchUrl, displayField, valueField) {
    const input = document.getElementById(inputId);
    if (!input) return;

    let dropdown = null;
    let selectedIndex = -1;

    input.addEventListener('input', function() {
        const query = this.value.trim();
        
        if (query.length < 2) {
            hideDropdown();
            return;
        }

        fetch(`${searchUrl}?q=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(data => {
                showDropdown(data, displayField, valueField);
            })
            .catch(error => {
                console.error('Autocomplete error:', error);
            });
    });

    input.addEventListener('keydown', function(e) {
        if (!dropdown) return;

        const items = dropdown.querySelectorAll('.autocomplete-item');
        
        if (e.key === 'ArrowDown') {
            e.preventDefault();
            selectedIndex = Math.min(selectedIndex + 1, items.length - 1);
            updateSelection(items);
        } else if (e.key === 'ArrowUp') {
            e.preventDefault();
            selectedIndex = Math.max(selectedIndex - 1, -1);
            updateSelection(items);
        } else if (e.key === 'Enter' && selectedIndex >= 0) {
            e.preventDefault();
            items[selectedIndex].click();
        } else if (e.key === 'Escape') {
            hideDropdown();
        }
    });

    function showDropdown(items, displayField, valueField) {
        hideDropdown();

        dropdown = document.createElement('div');
        dropdown.className = 'autocomplete-dropdown';
        
        if (items.length === 0) {
            dropdown.innerHTML = '<div class="autocomplete-item">No results found</div>';
        } else {
            items.forEach(item => {
                const div = document.createElement('div');
                div.className = 'autocomplete-item';
                div.textContent = item[displayField] || item.name || item.id;
                div.addEventListener('click', function() {
                    input.value = item[displayField] || item.name || item.id;
                    const hiddenInput = document.getElementById(inputId + '_id');
                    if (hiddenInput) {
                        hiddenInput.value = item[valueField] || item.id;
                    }
                    hideDropdown();
                });
                dropdown.appendChild(div);
            });
        }

        const container = input.closest('.autocomplete-container') || input.parentElement;
        container.style.position = 'relative';
        container.appendChild(dropdown);
        selectedIndex = -1;
    }

    function updateSelection(items) {
        items.forEach((item, index) => {
            if (index === selectedIndex) {
                item.style.background = '#f0f0f0';
            } else {
                item.style.background = '';
            }
        });
    }

    function hideDropdown() {
        if (dropdown) {
            dropdown.remove();
            dropdown = null;
        }
        selectedIndex = -1;
    }

    // Hide dropdown when clicking outside
    document.addEventListener('click', function(e) {
        if (dropdown && !dropdown.contains(e.target) && e.target !== input) {
            hideDropdown();
        }
    });
}

// Confirm delete dialogs
document.addEventListener('DOMContentLoaded', function() {
    const deleteButtons = document.querySelectorAll('.btn-delete, [data-action="delete"]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            if (!confirm('Are you sure you want to delete this item? This action cannot be undone.')) {
                e.preventDefault();
            }
        });
    });

    // Auto-hide alerts after 5 seconds
    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.transition = 'opacity 0.5s';
            alert.style.opacity = '0';
            setTimeout(() => alert.remove(), 500);
        }, 5000);
    });

    // Form validation
    const forms = document.querySelectorAll('form[data-validate]');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!form.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });
});

// Sortable table headers
document.addEventListener('DOMContentLoaded', function() {
    const sortableHeaders = document.querySelectorAll('th.sortable');
    sortableHeaders.forEach(header => {
        header.addEventListener('click', function() {
            const currentSort = new URLSearchParams(window.location.search).get('sortBy');
            const currentDir = new URLSearchParams(window.location.search).get('sortDir') || 'asc';
            const sortField = this.dataset.sort || this.textContent.trim().toLowerCase().replace(/\s+/g, '');
            const newDir = (currentSort === sortField && currentDir === 'asc') ? 'desc' : 'asc';
            
            const url = new URL(window.location);
            url.searchParams.set('sortBy', sortField);
            url.searchParams.set('sortDir', newDir);
            window.location = url;
        });
    });
});

// Initialize autocomplete for relation fields
document.addEventListener('DOMContentLoaded', function() {
    // Example: User autocomplete for restaurant owner
    // initAutocomplete('ownerSearch', '/admin/api/users/search', 'name', 'id');
});

