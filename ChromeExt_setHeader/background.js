async function updateNetworkRules() {
    const { customHeaders = [], headersEnabled = true } = await chrome.storage.local.get(['customHeaders', 'headersEnabled']);
    const existingRule = await chrome.declarativeNetRequest.getDynamicRules();
    const addRules = headersEnabled && customHeaders.length ? [{
        id: 1,
        priority: 1,
        action: {
            type: 'modifyHeaders',
            requestHeaders: customHeaders.map(header => ({
                header: header.name,
                operation: 'set',
                value: header.value
            }))
        },
        condition: {
            urlFilter: '*',
            resourceTypes: ['main_frame', 'sub_frame', 'xmlhttprequest', 'script', 'image', 'object','stylesheet','font','ping','csp_report','media','websocket','webtransport','webbundle','other']
        }
    }] : [];

    await chrome.declarativeNetRequest.updateDynamicRules({
        removeRuleIds: existingRule.map(rule => rule.id),
        addRules: addRules
    });

    const installedRules = await chrome.declarativeNetRequest.getDynamicRules();
    const installedHeaderCount = installedRules.find(rule => rule.id === 1)?.action.requestHeaders.length || 0;
    const expectedHeaderCount = headersEnabled ? customHeaders.length : 0;

    if (installedHeaderCount !== expectedHeaderCount) {
        console.error(`Mismatch in header count: installed ${installedHeaderCount}, expected ${expectedHeaderCount}`);
        throw new Error('Chrome did not retain all requested header rules.');
    }
    return installedHeaderCount;
}

chrome.runtime.onMessage.addListener((message, sender, sendResponse) => {
    if (message.type === 'UPDATE_HEADERS') {
        updateNetworkRules()
        .then(headerCount => sendResponse({ success: true, headerCount }))
        .catch(error => sendResponse({ success: false, error: error.message }));
        return true; // Keep the message channel open for async response
    }

    if(message.type === 'CHECK_REQUESTS') {
        chrome.declarativeNetRequest.getMatchedRules().then(matchedRules => {
            const rules = Array.isArray(matchedRules) ? matchedRules : matchedRules.rulesMatchedInfo || [];
            sendResponse({ success: true, matchCount: rules.filter(match => match.rule.ruleId === 1).length });
        })
        .catch(error => sendResponse({ success: false, error: error.message }));
        return true;
    }
});

chrome.runtime.onInstalled.addListener(updateNetworkRules);
chrome.runtime.onStartup.addListener(updateNetworkRules);