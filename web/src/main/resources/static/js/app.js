class SQLFormatter {
    // Constants
    static API_ENDPOINT = '/api/format';
    static SETTINGS_KEY = 'sqlFormatterSettings';
    static MONACO_CDN_URL = 'https://unpkg.com/monaco-editor@0.45.0/min/vs';
    
    constructor() {
        this.editor = null;
        this.initMonacoEditor();
        this.initElements();
        this.bindEvents();
        // Delay loading settings to ensure all elements are initialized
        setTimeout(() => this.loadSettings(), 100);
    }

    async initMonacoEditor() {
        // Wait for Monaco Editor to load
        await new Promise((resolve) => {
            require.config({ paths: { vs: SQLFormatter.MONACO_CDN_URL } });
            require(['vs/editor/editor.main'], () => {
                const editorConfig = {
                    value: '',
                    language: 'sql',
                    theme: 'vs',
                    fontSize: 14,
                    lineNumbers: 'on',
                    roundedSelection: false,
                    scrollBeyondLastLine: false,
                    readOnly: false,
                    automaticLayout: true,
                    minimap: { enabled: false },
                    folding: true,
                    bracketPairColorization: { enabled: true },
                    wordWrap: 'off', // Default to off
                    scrollbar: {
                        vertical: 'auto',
                        horizontal: 'auto',
                        verticalScrollbarSize: 14,
                        horizontalScrollbarSize: 14,
                        useShadows: false
                    },
                    guides: {
                        bracketPairs: true,
                        indentation: true
                    }
                };
                
                this.editor = monaco.editor.create(document.getElementById('monaco-editor'), editorConfig);
                
                // Ensure scrollbar position is at the left
                this.resetScrollPosition();
                
                resolve();
            });
        });
    }

    initElements() {
        this.formatBtn = document.getElementById('formatBtn');
        this.minifyBtn = document.getElementById('minifyBtn');
        this.clearBtn = document.getElementById('clearBtn');
        this.copyBtn = document.getElementById('copyBtn');
        this.wordWrapToggle = document.getElementById('wordWrapToggle');
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
        
        // Word wrap toggle
        this.wordWrapToggle.addEventListener('change', () => {
            this.editor.updateOptions({
                wordWrap: this.wordWrapToggle.checked ? 'on' : 'off'
            });
            this.saveSettings();
        });
        
        // Auto-format on Enter (Ctrl+Enter)
        this.editor.onKeyDown((e) => {
            if (e.ctrlKey && e.code === 'Enter') {
                e.preventDefault();
                this.formatSQL();
            }
        });
        
        // Handle window resize for responsive layout
        window.addEventListener('resize', () => {
            if (this.editor) {
                this.editor.layout();
            }
        });
        
        // Save settings on change
        this.bindOptionElements();
    }

    // Bind change events to all option elements
    bindOptionElements() {
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

    // Reset scrollbar position to the left
    resetScrollPosition() {
        if (this.editor) {
            this.editor.setScrollPosition({ scrollLeft: 0, scrollTop: 0 });
        }
    }

    async formatSQL() {
        await this.processSQL('format', 'Formatting');
    }

    async minifySQL() {
        await this.processSQL('minify', 'Minification');
    }

    // Generic method to process SQL (format or minify)
    async processSQL(action, actionName) {
        const sql = this.editor.getValue().trim();
        if (!sql) {
            this.showMessage('Please enter SQL statement', 'error');
            return;
        }
        
        this.setLoading(true);
        try {
            const options = this.getFormatOptions();
            if (action === 'minify') {
                options.mode = 'MINIFY'; // Force minify mode
            }
            
            const response = await fetch(SQLFormatter.API_ENDPOINT, {
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
                this.editor.setValue(result.formattedSQL);
                // Ensure scrollbar position is at the left
                this.resetScrollPosition();
                this.showMessage(`${actionName} successful`, 'success');
            } else {
                this.showMessage(result.error || `${actionName} failed`, 'error');
            }
        } catch (error) {
            console.error(`${actionName} error:`, error);
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
            mode: 'FORMAT', // Always FORMAT for format, MINIFY for minify
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
        this.editor.setValue('');
        // Ensure scrollbar position is at the left
        this.resetScrollPosition();
        this.showMessage('Cleared', 'success');
    }

    async copyResult() {
        const text = this.editor.getValue();
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
        
        // Remove body loading class to avoid page flicker
        // Only change button states, keep editor fully interactive
        if (this.editor) {
            this.editor.updateOptions({ readOnly: false });
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
            wordWrap: this.wordWrapToggle.checked,
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
        localStorage.setItem(SQLFormatter.SETTINGS_KEY, JSON.stringify(settings));
    }

    loadSettings() {
        // Default values strictly match FormatOptions.defaultOptions()
        const defaults = {
            wordWrap: false,
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
        
        // Try to load settings from localStorage, use defaults if not available
        const savedSettings = localStorage.getItem(SQLFormatter.SETTINGS_KEY);
        const settings = savedSettings ? JSON.parse(savedSettings) : defaults;
        
        // Apply settings (use saved values or defaults)
        this.wordWrapToggle.checked = settings.wordWrap !== undefined ? settings.wordWrap : defaults.wordWrap;
        this.indentChar.value = settings.indentChar || defaults.indentChar;
        this.indentCount.value = settings.indentCount || defaults.indentCount;
        this.maxLineLength.value = settings.maxLineLength || defaults.maxLineLength;
        this.keyWordStyle.value = settings.keyWordStyle || defaults.keyWordStyle;
        this.commaStyle.value = settings.commaStyle || defaults.commaStyle;
        this.breakFunctionArgs.checked = settings.breakFunctionArgs !== undefined ? settings.breakFunctionArgs : defaults.breakFunctionArgs;
        this.alignFunctionArgs.checked = settings.alignFunctionArgs !== undefined ? settings.alignFunctionArgs : defaults.alignFunctionArgs;
        this.breakCaseWhen.checked = settings.breakCaseWhen !== undefined ? settings.breakCaseWhen : defaults.breakCaseWhen;
        this.alignCaseWhen.checked = settings.alignCaseWhen !== undefined ? settings.alignCaseWhen : defaults.alignCaseWhen;
        this.breakInList.checked = settings.breakInList !== undefined ? settings.breakInList : defaults.breakInList;
        this.alignInList.checked = settings.alignInList !== undefined ? settings.alignInList : defaults.alignInList;
        this.breakAndOr.checked = settings.breakAndOr !== undefined ? settings.breakAndOr : defaults.breakAndOr;
        this.breakExplain.checked = settings.breakExplain !== undefined ? settings.breakExplain : defaults.breakExplain;
        this.breakCTE.checked = settings.breakCTE !== undefined ? settings.breakCTE : defaults.breakCTE;
        this.breakJoinRelations.checked = settings.breakJoinRelations !== undefined ? settings.breakJoinRelations : defaults.breakJoinRelations;
        this.breakJoinOn.checked = settings.breakJoinOn !== undefined ? settings.breakJoinOn : defaults.breakJoinOn;
        this.alignJoinOn.checked = settings.alignJoinOn !== undefined ? settings.alignJoinOn : defaults.alignJoinOn;
        this.breakSelectItems.checked = settings.breakSelectItems !== undefined ? settings.breakSelectItems : defaults.breakSelectItems;
        this.breakGroupByItems.checked = settings.breakGroupByItems !== undefined ? settings.breakGroupByItems : defaults.breakGroupByItems;
        this.breakOrderBy.checked = settings.breakOrderBy !== undefined ? settings.breakOrderBy : defaults.breakOrderBy;
        this.formatSubquery.checked = settings.formatSubquery !== undefined ? settings.formatSubquery : defaults.formatSubquery;
        
        // Apply word wrap setting to editor
        if (this.editor) {
            this.editor.updateOptions({
                wordWrap: this.wordWrapToggle.checked ? 'on' : 'off'
            });
        }
    }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', async () => {
    const formatter = new SQLFormatter();
    // Wait for Monaco Editor to be ready
    await new Promise(resolve => {
        const checkEditor = () => {
            if (formatter.editor) {
                // Apply word wrap setting after editor is ready
                formatter.editor.updateOptions({
                    wordWrap: formatter.wordWrapToggle.checked ? 'on' : 'off'
                });
                resolve();
            } else {
                setTimeout(checkEditor, 100);
            }
        };
        checkEditor();
    });
}); 