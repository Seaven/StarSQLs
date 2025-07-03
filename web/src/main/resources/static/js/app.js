class SQLFormatter {
    constructor() {
        this.initElements();
        this.bindEvents();
        this.loadSettings();
    }

    initElements() {
        this.inputSQL = document.getElementById('inputSQL');
        this.formatBtn = document.getElementById('formatBtn');
        this.minifyBtn = document.getElementById('minifyBtn');
        this.clearBtn = document.getElementById('clearBtn');
        this.copyBtn = document.getElementById('copyBtn');
        // Options elements
        this.indentChar = document.getElementById('indentChar');
        this.indentCount = document.getElementById('indentCount');
        this.maxLineLength = document.getElementById('maxLineLength');
        this.keyWordStyle = document.getElementById('keyWordStyle');
        this.commaStyle = document.getElementById('commaStyle');
        this.breakFunctionArgs = document.getElementById('breakFunctionArgs');
        this.alignFunctionArgs = document.getElementById('alignFunctionArgs');
        this.breakCaseWhen = document.getElementById('breakCaseWhen');
        this.alignCaseWhen = document.getElementById('alignCaseWhen');
        this.breakInList = document.getElementById('breakInList');
        this.alignInList = document.getElementById('alignInList');
        this.breakAndOr = document.getElementById('breakAndOr');
        this.breakExplain = document.getElementById('breakExplain');
        this.breakCTE = document.getElementById('breakCTE');
        this.breakJoinRelations = document.getElementById('breakJoinRelations');
        this.breakJoinOn = document.getElementById('breakJoinOn');
        this.alignJoinOn = document.getElementById('alignJoinOn');
        this.breakSelectItems = document.getElementById('breakSelectItems');
        this.breakGroupByItems = document.getElementById('breakGroupByItems');
        this.breakOrderBy = document.getElementById('breakOrderBy');
        this.formatSubquery = document.getElementById('formatSubquery');
    }

    bindEvents() {
        this.formatBtn.addEventListener('click', () => this.formatSQL());
        this.minifyBtn.addEventListener('click', () => this.minifySQL());
        this.clearBtn.addEventListener('click', () => this.clearAll());
        this.copyBtn.addEventListener('click', () => this.copyResult());
        // Auto-format on Enter (Ctrl+Enter)
        this.inputSQL.addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.key === 'Enter') {
                e.preventDefault();
                this.formatSQL();
            }
        });
        // Save settings on change
        const optionElements = [
            this.indentChar, this.indentCount, this.maxLineLength, this.keyWordStyle, 
            this.commaStyle, this.breakFunctionArgs, this.alignFunctionArgs, 
            this.breakCaseWhen, this.alignCaseWhen, this.breakInList, this.alignInList, 
            this.breakAndOr, this.breakExplain, this.breakCTE, this.breakJoinRelations, 
            this.breakJoinOn, this.alignJoinOn, this.breakSelectItems, 
            this.breakGroupByItems, this.breakOrderBy, this.formatSubquery
        ];
        optionElements.forEach(element => {
            element.addEventListener('change', () => this.saveSettings());
        });
    }

    async formatSQL() {
        const sql = this.inputSQL.value.trim();
        if (!sql) {
            this.showMessage('Please enter SQL statement', 'error');
            return;
        }
        this.setLoading(true);
        try {
            const options = this.getFormatOptions();
            const response = await fetch('/api/format', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    sql: sql,
                    options: options
                })
            });
            const result = await response.json();
            if (result.success) {
                this.inputSQL.value = result.formattedSQL;
                this.showMessage('Formatting successful', 'success');
            } else {
                this.showMessage(result.error || 'Formatting failed', 'error');
            }
        } catch (error) {
            console.error('Format error:', error);
            this.showMessage('Network error, please try again later', 'error');
        } finally {
            this.setLoading(false);
        }
    }

    async minifySQL() {
        const sql = this.inputSQL.value.trim();
        if (!sql) {
            this.showMessage('Please enter SQL statement', 'error');
            return;
        }
        this.setLoading(true);
        try {
            const options = this.getFormatOptions();
            options.isMinify = true; // Force minify mode
            const response = await fetch('/api/format', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    sql: sql,
                    options: options
                })
            });
            const result = await response.json();
            if (result.success) {
                this.inputSQL.value = result.formattedSQL;
                this.showMessage('Minification successful', 'success');
            } else {
                this.showMessage(result.error || 'Minification failed', 'error');
            }
        } catch (error) {
            console.error('Minify error:', error);
            this.showMessage('Network error, please try again later', 'error');
        } finally {
            this.setLoading(false);
        }
    }

    getFormatOptions() {
        // Calculate indent value
        const indentChar = this.indentChar.value;
        const indentCount = parseInt(this.indentCount.value);
        const indent = indentChar === '\\t' ? '\t'.repeat(indentCount) : ' '.repeat(indentCount);
        
        return {
            isMinify: false, // Always false for format, true only for minify
            indent: indent,
            maxLineLength: parseInt(this.maxLineLength.value),
            keyWordStyle: this.keyWordStyle.value,
            commaStyle: this.commaStyle.value,
            breakFunctionArgs: this.breakFunctionArgs.checked,
            alignFunctionArgs: this.alignFunctionArgs.checked,
            breakCaseWhen: this.breakCaseWhen.checked,
            alignCaseWhen: this.alignCaseWhen.checked,
            breakInList: this.breakInList.checked,
            alignInList: this.alignInList.checked,
            breakAndOr: this.breakAndOr.checked,
            breakExplain: this.breakExplain.checked,
            breakCTE: this.breakCTE.checked,
            breakJoinRelations: this.breakJoinRelations.checked,
            breakJoinOn: this.breakJoinOn.checked,
            alignJoinOn: this.alignJoinOn.checked,
            breakSelectItems: this.breakSelectItems.checked,
            breakGroupByItems: this.breakGroupByItems.checked,
            breakOrderBy: this.breakOrderBy.checked,
            formatSubquery: this.formatSubquery.checked
        };
    }

    clearAll() {
        this.inputSQL.value = '';
        this.showMessage('Cleared', 'success');
    }

    async copyResult() {
        const text = this.inputSQL.value;
        if (!text) {
            this.showMessage('No content to copy', 'error');
            return;
        }
        try {
            await navigator.clipboard.writeText(text);
            this.showMessage('Copied to clipboard', 'success');
        } catch (error) {
            // Fallback for older browsers
            this.fallbackCopyTextToClipboard(text);
        }
    }

    fallbackCopyTextToClipboard(text) {
        const textArea = document.createElement('textarea');
        textArea.value = text;
        textArea.style.position = 'fixed';
        textArea.style.left = '-999999px';
        textArea.style.top = '-999999px';
        document.body.appendChild(textArea);
        textArea.focus();
        textArea.select();
        
        try {
            document.execCommand('copy');
            this.showMessage('Copied to clipboard', 'success');
        } catch (error) {
            this.showMessage('Copy failed', 'error');
        }
        
        document.body.removeChild(textArea);
    }

    setLoading(loading) {
        this.formatBtn.disabled = loading;
        this.formatBtn.textContent = loading ? 'Formatting...' : 'Format';
        this.minifyBtn.disabled = loading;
        this.minifyBtn.textContent = loading ? 'Minifying...' : 'Minify';
        
        if (loading) {
            document.body.classList.add('loading');
        } else {
            document.body.classList.remove('loading');
        }
    }

    showMessage(message, type) {
        // Remove existing messages
        const existingMessages = document.querySelectorAll('.message');
        existingMessages.forEach(msg => msg.remove());

        // Create new message
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;
        messageDiv.textContent = message;

        // Add to body for fixed positioning
        document.body.appendChild(messageDiv);

        // Auto remove after 3 seconds
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.style.animation = 'slideOut 0.3s ease-in';
                setTimeout(() => {
                    if (messageDiv.parentNode) {
                        messageDiv.remove();
                    }
                }, 300);
            }
        }, 3000);
    }

    saveSettings() {
        const settings = {
            indentChar: this.indentChar.value,
            indentCount: this.indentCount.value,
            maxLineLength: this.maxLineLength.value,
            keyWordStyle: this.keyWordStyle.value,
            commaStyle: this.commaStyle.value,
            breakFunctionArgs: this.breakFunctionArgs.checked,
            alignFunctionArgs: this.alignFunctionArgs.checked,
            breakCaseWhen: this.breakCaseWhen.checked,
            alignCaseWhen: this.alignCaseWhen.checked,
            breakInList: this.breakInList.checked,
            alignInList: this.alignInList.checked,
            breakAndOr: this.breakAndOr.checked,
            breakExplain: this.breakExplain.checked,
            breakCTE: this.breakCTE.checked,
            breakJoinRelations: this.breakJoinRelations.checked,
            breakJoinOn: this.breakJoinOn.checked,
            alignJoinOn: this.alignJoinOn.checked,
            breakSelectItems: this.breakSelectItems.checked,
            breakGroupByItems: this.breakGroupByItems.checked,
            breakOrderBy: this.breakOrderBy.checked,
            formatSubquery: this.formatSubquery.checked
        };
        localStorage.setItem('sqlFormatterSettings', JSON.stringify(settings));
    }

    loadSettings() {
        // 清除旧的设置，确保使用默认值
        localStorage.removeItem('sqlFormatterSettings');
        
        // 默认值严格与FormatOptions.defaultOptions()一致
        const defaults = {
            indentChar: ' ',
            indentCount: 4,
            maxLineLength: 120,
            keyWordStyle: 'UPPER_CASE',
            commaStyle: 'SPACE_AFTER',
            breakFunctionArgs: false,
            alignFunctionArgs: false,
            breakCaseWhen: false,
            alignCaseWhen: false,
            breakInList: false,
            alignInList: false,
            breakAndOr: false,
            breakExplain: false,
            breakCTE: true,
            breakJoinRelations: false,
            breakJoinOn: false,
            alignJoinOn: true,
            breakSelectItems: false,
            breakGroupByItems: false,
            breakOrderBy: false,
            formatSubquery: true
        };
        
        // 直接使用默认值，不读取localStorage
        this.indentChar.value = defaults.indentChar;
        this.indentCount.value = defaults.indentCount;
        this.maxLineLength.value = defaults.maxLineLength;
        this.keyWordStyle.value = defaults.keyWordStyle;
        this.commaStyle.value = defaults.commaStyle;
        this.breakFunctionArgs.checked = defaults.breakFunctionArgs;
        this.alignFunctionArgs.checked = defaults.alignFunctionArgs;
        this.breakCaseWhen.checked = defaults.breakCaseWhen;
        this.alignCaseWhen.checked = defaults.alignCaseWhen;
        this.breakInList.checked = defaults.breakInList;
        this.alignInList.checked = defaults.alignInList;
        this.breakAndOr.checked = defaults.breakAndOr;
        this.breakExplain.checked = defaults.breakExplain;
        this.breakCTE.checked = defaults.breakCTE;
        this.breakJoinRelations.checked = defaults.breakJoinRelations;
        this.breakJoinOn.checked = defaults.breakJoinOn;
        this.alignJoinOn.checked = defaults.alignJoinOn;
        this.breakSelectItems.checked = defaults.breakSelectItems;
        this.breakGroupByItems.checked = defaults.breakGroupByItems;
        this.breakOrderBy.checked = defaults.breakOrderBy;
        this.formatSubquery.checked = defaults.formatSubquery;
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new SQLFormatter();
}); 