class SQLFormatter {
    constructor() {
        this.initElements();
        this.bindEvents();
        this.loadSettings();
    }

    initElements() {
        this.inputSQL = document.getElementById('inputSQL');
        this.outputSQL = document.getElementById('outputSQL');
        this.formatBtn = document.getElementById('formatBtn');
        this.clearBtn = document.getElementById('clearBtn');
        this.copyBtn = document.getElementById('copyBtn');
        
        // Options elements
        this.indent = document.getElementById('indent');
        this.maxLineLength = document.getElementById('maxLineLength');
        this.keywordStyle = document.getElementById('keywordStyle');
        this.commaStyle = document.getElementById('commaStyle');
        this.breakCTE = document.getElementById('breakCTE');
        this.breakOrderBy = document.getElementById('breakOrderBy');
        this.minify = document.getElementById('minify');
    }

    bindEvents() {
        this.formatBtn.addEventListener('click', () => this.formatSQL());
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
            this.indent, this.maxLineLength, this.keywordStyle, 
            this.commaStyle, this.breakCTE, this.breakOrderBy, this.minify
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
                this.outputSQL.textContent = result.formattedSQL;
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

    getFormatOptions() {
        // Create FormatOptions object that matches the backend FormatOptions class
        return {
            isMinify: this.minify.checked,
            indent: this.indent.value,
            maxLineLength: parseInt(this.maxLineLength.value),
            commaStyle: this.commaStyle.value.toUpperCase(),
            keyWordStyle: this.keywordStyle.value === 'UPPER' ? 'UPPER_CASE' : 
                          this.keywordStyle.value === 'LOWER' ? 'LOWER_CASE' : 'NONE',
            breakCTE: this.breakCTE.checked,
            breakOrderBy: this.breakOrderBy.checked
        };
    }

    clearAll() {
        this.inputSQL.value = '';
        this.outputSQL.textContent = '';
        this.showMessage('Cleared', 'success');
    }

    async copyResult() {
        const text = this.outputSQL.textContent;
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

        // Insert after header
        const header = document.querySelector('header');
        header.parentNode.insertBefore(messageDiv, header.nextSibling);

        // Auto remove after 3 seconds
        setTimeout(() => {
            if (messageDiv.parentNode) {
                messageDiv.remove();
            }
        }, 3000);
    }

    saveSettings() {
        const settings = {
            indent: this.indent.value,
            maxLineLength: this.maxLineLength.value,
            keywordStyle: this.keywordStyle.value,
            commaStyle: this.commaStyle.value,
            breakCTE: this.breakCTE.checked,
            breakOrderBy: this.breakOrderBy.checked,
            minify: this.minify.checked
        };

        localStorage.setItem('sqlFormatterSettings', JSON.stringify(settings));
    }

    loadSettings() {
        const saved = localStorage.getItem('sqlFormatterSettings');
        if (saved) {
            try {
                const settings = JSON.parse(saved);
                
                this.indent.value = settings.indent || '    ';
                this.maxLineLength.value = settings.maxLineLength || 80;
                this.keywordStyle.value = settings.keywordStyle || 'UPPER';
                this.commaStyle.value = settings.commaStyle || 'AFTER';
                this.breakCTE.checked = settings.breakCTE !== false;
                this.breakOrderBy.checked = settings.breakOrderBy !== false;
                this.minify.checked = settings.minify || false;
            } catch (error) {
                console.error('Failed to load settings:', error);
            }
        }
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new SQLFormatter();
}); 