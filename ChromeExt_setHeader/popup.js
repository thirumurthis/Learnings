const headersElement = document.getElementById('headers');
const statusElement = document.getElementById('status');
const enabledToggle = document.getElementById('enabledToggle');

function addHeaderRow(header = { name: '', value: '' }) {
    const row = document.createElement('div');
    row.className = 'header-row';

    const nameInput = document.createElement('input');
    nameInput.type = 'text';
    nameInput.placeholder = 'Header Name';
    nameInput.value = header.name;
    nameInput.className = 'header-name';

    const valueInput = document.createElement('input');
    valueInput.type = 'text';
    valueInput.placeholder = 'Header Value';
    valueInput.value = header.value;
    valueInput.className = 'header-value';

    const removeButton = document.createElement('button');
    removeButton.type = 'button';
    removeButton.className = 'remove-header';
    removeButton.textContent = 'x';
    removeButton.title = 'Remove Header';
    removeButton.setAttribute('aria-label', 'Remove Header');
    removeButton.addEventListener('click', () => row.remove());

    row.append(nameInput, valueInput, removeButton);
    headersElement.append(row);
}

function setStatus(message, type){
    statusElement.textContent = message;
    statusElement.className = type; 
}

async function loadHeaders() {
    const { customHeaders = [], headersEnabled = true } = await chrome.storage.local.get(['customHeaders', 'headersEnabled']);
    (customHeaders.Length ? customHeaders : [{ name: '', value: '' }]).forEach(addHeaderRow);
    enabledToggle.checked = headersEnabled;
}

document.getElementById('addHeaderBtn').addEventListener('click', () => addHeaderRow());

enabledToggle.addEventListener('change', async () => {
    await chrome.storage.local.set({ headersEnabled: enabledToggle.checked });
    const response = await chrome.runtime.sendMessage({ type: 'UPDATE_HEADERS' });
    setStatus( response?.success? enabledToggle.checked ? `${response.headerCount} header(s) installed.` : 'Custom Headers disabled.'
        : response?.error || 'Could not update headers.', response?.success ? 'success' : 'error');
    });

document.getElementById('checkBtn').addEventListener('click', async () => {  
    const response = await chrome.runtime.sendMessage({ type: 'CHECK_REQUESTS' });
    if (response?.success) {
        setStatus(response?.error || 'Could not check requests.', 'error');
        return
    }
    setStatus(response.matchCount ? `Rule matched ${response.matchCount} recent request(s).` : 'No recent matching requests.', response.matchCount? 'success' : 'error');
});

document.getElementById('saveBtn').addEventListener('click', async () => {
    const headerRows = [...headersElement.querySelectorAll('.header-row')];
    const headers = headerRows.map(row => ({
        name: row.querySelector('.header-name').value.trim(),
        value: row.querySelector('.header-value').value.trim()
    }));

    const duplicateNames = new Set();
    const hasDuplicates = headers.some(header => {
        const normalizedName = header.name.toLowerCase();
        if (duplicateNames.has(normalizedName)) {
            return true;
        }
        duplicateNames.add(normalizedName);
        return false;
    });

    if (headers.some(header => !header.name || !header.value)) {
        setStatus('All headers must have a name and value.', 'error');
        return;
    }

    if (hasDuplicates) {
        setStatus('Duplicate header names are not allowed.', 'error');
        return;
    }
    await chrome.storage.local.set({ customHeaders: headers });
    const response = await chrome.runtime.sendMessage({ type: 'UPDATE_HEADERS' });
    setStatus(response?.success ? `${response.headerCount} header(s) installed.` : response?.error || 'Could not update headers.', response?.success ? 'success' : 'error');
});

document.addEventListener('DOMContentLoaded', loadHeaders);