/**
 * SQL DAG Visualizer using Cytoscape.js
 */
class SQLDagVisualizer {
    constructor(containerSelector) {
        this.container = document.querySelector(containerSelector);
        this.cy = null;
        this.currentLayout = 'dagre';
        this.initialized = false;
        this.isFullscreen = false;
        this.currentTheme = 'modern'; // Default theme
        this.initThemes();
    }
    
    initThemes() {
        this.themes = {
            modern: {
                name: 'Modern Blue',
                node: '#4A90E2',
                cte: '#7B68EE',
                result: '#50C878',
                selected: '#2E5C8A',
                selectedBorder: '#FFD700',
                edge: '#95A5A6'
            },
            professional: {
                name: 'Professional',
                node: '#2C3E50',
                cte: '#34495E',
                result: '#16A085',
                selected: '#1A252F',
                selectedBorder: '#3498DB',
                edge: '#7F8C8D'
            },
            ocean: {
                name: 'Ocean',
                node: '#0077BE',
                cte: '#005F99',
                result: '#00A86B',
                selected: '#004C77',
                selectedBorder: '#00D9FF',
                edge: '#6DB3C8'
            },
            forest: {
                name: 'Forest',
                node: '#27AE60',
                cte: '#229954',
                result: '#52BE80',
                selected: '#1E8449',
                selectedBorder: '#F39C12',
                edge: '#85929E'
            },
            purple: {
                name: 'Purple Dream',
                node: '#667eea',
                cte: '#764ba2',
                result: '#f093fb',
                selected: '#5568d3',
                selectedBorder: '#feca57',
                edge: '#a4b0be'
            },
            github: {
                name: 'GitHub',
                node: '#0969DA',
                cte: '#8250DF',
                result: '#1F883D',
                selected: '#054ADA',
                selectedBorder: '#FFA657',
                edge: '#656D76'
            }
        };
    }
    
    getThemeStyle() {
        const theme = this.themes[this.currentTheme];
        return [
            // Node styles
            {
                selector: 'node',
                style: {
                    'shape': 'roundrectangle',
                    'background-color': theme.node,
                    'label': 'data(displayLabel)',
                    'color': '#fff',
                    'text-valign': 'center',
                    'text-halign': 'center',
                    'text-wrap': 'wrap',
                    'text-max-width': '200px',
                    'width': 'label',
                    'height': 'label',
                    'padding': '15px',
                    'font-size': '17px',
                    'font-family': 'Monaco, Consolas, monospace',
                    'border-width': 0
                }
            },
            {
                selector: 'node[type="CTE"]',
                style: {
                    'background-color': theme.cte
                }
            },
            {
                selector: 'node[type="RESULT"]',
                style: {
                    'background-color': theme.result,
                    'shape': 'ellipse'
                }
            },
            {
                selector: 'node:selected',
                style: {
                    'border-width': 3,
                    'border-color': theme.selectedBorder,
                    'background-color': theme.selected
                }
            },
            // Edge styles
            {
                selector: 'edge',
                style: {
                    'width': 2,
                    'line-color': theme.edge,
                    'target-arrow-color': theme.edge,
                    'target-arrow-shape': 'triangle',
                    'curve-style': 'straight',  // Use straight edges
                    'label': 'data(label)',
                    'font-size': '15px',
                    'text-rotation': 'none',  // Horizontal text
                    'text-margin-y': -10
                }
            },
            {
                selector: 'edge[type="CTE_REFERENCE"]',
                style: {
                    'line-style': 'dashed',
                    'line-color': theme.cte,
                    'target-arrow-color': theme.cte
                }
            }
        ];
    }
    
    setTheme(themeName) {
        if (!this.themes[themeName]) {
            console.error('Theme not found:', themeName);
            return;
        }
        
        this.currentTheme = themeName;
        if (this.cy) {
            this.cy.style(this.getThemeStyle());
            this.updateNavigator();
        }
    }
    
    async init() {
        if (this.initialized) return;
        
        try {
            // Check if container exists
            if (!this.container) {
                throw new Error('DAG container element not found');
            }
            
            // Check if dependencies are loaded
            if (typeof window.cytoscape === 'undefined') {
                throw new Error('Cytoscape is not loaded');
            }
            if (typeof window.dagre === 'undefined') {
                throw new Error('Dagre is not loaded');
            }
            if (typeof window.cytoscapeDagre === 'undefined') {
                throw new Error('Cytoscape-Dagre is not loaded');
            }
            
            // Register dagre layout
            window.cytoscape.use(window.cytoscapeDagre);
            
            this.initCytoscape();
            this.initialized = true;
        } catch (error) {
            console.error('[DAG Visualizer] Failed to initialize:', error);
            throw error;
        }
    }
    
    initCytoscape() {
        this.cy = window.cytoscape({
            container: this.container,
            userPanningEnabled: true,
            userZoomingEnabled: true,
            boxSelectionEnabled: true,
            autoungrabify: false,  // Allow node dragging
            style: this.getThemeStyle(),
            layout: { name: 'preset' }
        });
        
        this.setupTooltips();
        this.setupEdgeEditor();
    }
    
    renderDAG(nodes, edges) {
        if (!this.cy) return;
        
        this.cy.elements().remove();
        
        // Add nodes
        nodes.forEach(node => {
            const displayLabel = this.buildDisplayLabel(node);
            
            this.cy.add({
                group: 'nodes',
                data: {
                    id: node.id,
                    type: node.type,
                    label: node.label,
                    displayLabel: displayLabel,
                    sqlFragment: node.sqlFragment,
                    nodeData: node  // Store the entire node object
                }
            });
        });
        
        // Add edges
        edges.forEach((edge, index) => {
            this.cy.add({
                group: 'edges',
                data: {
                    id: edge.id || `edge_${index}`,
                    source: edge.source,
                    target: edge.target,
                    type: edge.type || 'DATAFLOW',
                    label: edge.label || '',
                    rows: edge.rows
                }
            });
        });
        
        // Apply layout (Bottom to Top for SQL execution flow)
        const layout = this.cy.layout({
            name: this.currentLayout,
            rankDir: 'BT',
            nodeSep: 50,
            rankSep: 60,
            animate: true,
            animationDuration: 500
        });
        
        // Initialize navigator after layout completes
        layout.on('layoutstop', () => {
            this.initNavigator();
        });
        
        layout.run();
    }
    
    buildDisplayLabel(node) {
        let label = node.label;
        if (node.details && node.details.length > 0) {
            label += '\n' + node.details.join('\n');
        }
        return label;
    }
    
    applyLayout(layoutName) {
        this.currentLayout = layoutName;
        
        const layoutConfig = {
            name: layoutName,
            rankDir: 'BT',  // Bottom to Top (SCAN at bottom, RESULT at top)
            nodeSep: 50,    // Horizontal spacing between nodes
            rankSep: 60,    // Vertical spacing between ranks
            animate: true,
            animationDuration: 500
        };
        
        const layout = this.cy.layout(layoutConfig);
        layout.run();
        return layout;
    }
    
    setupTooltips() {
        const tooltip = this.createTooltipElement();
        
        this.cy.on('tap', 'node', (evt) => {
            const node = evt.target;
            const nodeData = node.data('nodeData');
            
            if (nodeData) {
                const label = nodeData.label || 'Unknown';
                const id = nodeData.id || '';
                const sqlFragment = nodeData.sqlFragment || '';
                
                // First line: label (id), Second line: SQL fragment
                tooltip.innerHTML = `
                    <div class="tooltip-title">${this.escapeHtml(label)} (${this.escapeHtml(id)})</div>
                    ${sqlFragment ? `<div class="tooltip-sql">${this.escapeHtml(sqlFragment)}</div>` : ''}
                `;
                tooltip.style.display = 'block';
                
                // Position tooltip on the right side of the DAG panel (not browser)
                const dagPanel = this.container.closest('.dag-panel-right');
                if (dagPanel) {
                    const panelRect = dagPanel.getBoundingClientRect();
                    tooltip.style.right = (window.innerWidth - panelRect.right + 20) + 'px';
                    tooltip.style.top = (panelRect.top + 80) + 'px';
                }
            }
        });
        
        // Click outside to close tooltip
        document.addEventListener('click', (evt) => {
            if (!this.container.contains(evt.target) && !tooltip.contains(evt.target)) {
                tooltip.style.display = 'none';
            }
        });
        
        // Click on container background to close tooltip
        this.cy.on('tap', (evt) => {
            if (evt.target === this.cy) {
                tooltip.style.display = 'none';
            }
        });
    }
    
    initNavigator() {
        if (!this.cy) return;
        
        try {
            // Remove existing navigator if present
            let navContainer = document.getElementById('cy-navigator');
            if (navContainer) {
                navContainer.remove();
            }
            
            // Create navigator container with canvas and viewport overlay
            navContainer = document.createElement('div');
            navContainer.id = 'cy-navigator';
            navContainer.innerHTML = `
                <canvas id="nav-canvas" width="200" height="150"></canvas>
                <div id="nav-viewport"></div>
            `;
            
            // Append to DAG panel
            const dagPanel = this.container.closest('.dag-panel-right');
            if (dagPanel) {
                dagPanel.appendChild(navContainer);
            } else {
                this.container.parentElement.appendChild(navContainer);
            }
            
            this.navCanvas = document.getElementById('nav-canvas');
            this.navViewport = document.getElementById('nav-viewport');
            this.navCtx = this.navCanvas.getContext('2d');
            
            // Update navigator on viewport changes
            this.cy.on('viewport', () => this.updateNavigator());
            
            // Initial render
            this.updateNavigator();
            
            // Setup mouse interaction
            this.setupNavigatorInteraction();
        } catch (error) {
            console.error('[DAG Visualizer] Failed to initialize navigator:', error);
        }
    }
    
    updateNavigator() {
        if (!this.navCanvas || !this.navCtx || !this.cy) return;
        
        const ctx = this.navCtx;
        const canvas = this.navCanvas;
        const width = canvas.width;
        const height = canvas.height;
        
        // Clear canvas
        ctx.clearRect(0, 0, width, height);
        ctx.fillStyle = '#f8f9fa';
        ctx.fillRect(0, 0, width, height);
        
        // Get graph bounds
        const extent = this.cy.elements().boundingBox();
        if (!extent || extent.w === 0 || extent.h === 0) return;
        
        const scale = Math.min(width / extent.w, height / extent.h) * 0.9;
        const offsetX = (width - extent.w * scale) / 2 - extent.x1 * scale;
        const offsetY = (height - extent.h * scale) / 2 - extent.y1 * scale;
        
        // Draw nodes
        const theme = this.themes[this.currentTheme];
        this.cy.nodes().forEach(node => {
            const pos = node.position();
            const w = node.width() * scale;
            const h = node.height() * scale;
            const x = pos.x * scale + offsetX - w / 2;
            const y = pos.y * scale + offsetY - h / 2;
            
            ctx.fillStyle = theme.node;
            ctx.fillRect(x, y, w, h);
        });
        
        // Draw edges
        ctx.strokeStyle = theme.edge;
        ctx.lineWidth = 1;
        this.cy.edges().forEach(edge => {
            const source = edge.source().position();
            const target = edge.target().position();
            
            ctx.beginPath();
            ctx.moveTo(source.x * scale + offsetX, source.y * scale + offsetY);
            ctx.lineTo(target.x * scale + offsetX, target.y * scale + offsetY);
            ctx.stroke();
        });
        
        // Draw viewport rectangle
        const pan = this.cy.pan();
        const zoom = this.cy.zoom();
        const containerW = this.cy.width();
        const containerH = this.cy.height();
        
        const viewX = (-pan.x / zoom) * scale + offsetX;
        const viewY = (-pan.y / zoom) * scale + offsetY;
        const viewW = (containerW / zoom) * scale;
        const viewH = (containerH / zoom) * scale;
        
        // Update viewport div position
        if (this.navViewport) {
            this.navViewport.style.left = viewX + 'px';
            this.navViewport.style.top = viewY + 'px';
            this.navViewport.style.width = viewW + 'px';
            this.navViewport.style.height = viewH + 'px';
        }
    }
    
    setupNavigatorInteraction() {
        if (!this.navCanvas || !this.cy) return;
        
        let isDragging = false;
        
        const handleMove = (clientX, clientY) => {
            const rect = this.navCanvas.getBoundingClientRect();
            const x = clientX - rect.left;
            const y = clientY - rect.top;
            
            const extent = this.cy.elements().boundingBox();
            const scale = Math.min(this.navCanvas.width / extent.w, this.navCanvas.height / extent.h) * 0.9;
            const offsetX = (this.navCanvas.width - extent.w * scale) / 2 - extent.x1 * scale;
            const offsetY = (this.navCanvas.height - extent.h * scale) / 2 - extent.y1 * scale;
            
            const graphX = (x - offsetX) / scale;
            const graphY = (y - offsetY) / scale;
            
            const zoom = this.cy.zoom();
            const newPan = {
                x: -graphX * zoom + this.cy.width() / 2,
                y: -graphY * zoom + this.cy.height() / 2
            };
            
            this.cy.pan(newPan);
        };
        
        this.navCanvas.addEventListener('mousedown', (e) => {
            isDragging = true;
            handleMove(e.clientX, e.clientY);
        });
        
        document.addEventListener('mousemove', (e) => {
            if (isDragging) {
                handleMove(e.clientX, e.clientY);
            }
        });
        
        document.addEventListener('mouseup', () => {
            isDragging = false;
        });
    }
    
    setupEdgeEditor() {
        // Double-click on edge to edit label directly
        this.cy.on('dbltap', 'edge', (evt) => {
            const edge = evt.target;
            const currentLabel = edge.data('label') || '';
            
            // Create inline input element
            const input = document.createElement('input');
            input.type = 'text';
            input.value = currentLabel;
            input.style.position = 'absolute';
            input.style.zIndex = '10000';
            input.style.padding = '4px 8px';
            input.style.fontSize = '14px';
            input.style.border = '2px solid #054ADA';
            input.style.borderRadius = '4px';
            input.style.outline = 'none';
            input.style.backgroundColor = '#fff';
            input.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)';
            
            // Position input at edge midpoint
            const source = edge.source().renderedPosition();
            const target = edge.target().renderedPosition();
            const containerOffset = this.cy.container().getBoundingClientRect();
            const midX = (source.x + target.x) / 2 + containerOffset.left;
            const midY = (source.y + target.y) / 2 + containerOffset.top - 10;
            
            input.style.left = midX - 50 + 'px';
            input.style.top = midY + 'px';
            
            document.body.appendChild(input);
            input.focus();
            input.select();
            
            // Save on Enter or blur
            const saveLabel = () => {
                const newLabel = input.value.trim();
                edge.data('label', newLabel);
                document.body.removeChild(input);
            };
            
            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    saveLabel();
                } else if (e.key === 'Escape') {
                    document.body.removeChild(input);
                }
            });
            
            input.addEventListener('blur', saveLabel);
        });
    }
    
    createTooltipElement() {
        let tooltip = document.getElementById('dag-tooltip');
        if (!tooltip) {
            tooltip = document.createElement('div');
            tooltip.id = 'dag-tooltip';
            tooltip.className = 'dag-tooltip';
            document.body.appendChild(tooltip);
        }
        return tooltip;
    }
    
    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
    
    fit() {
        if (this.cy) {
            this.cy.fit(null, 50);
        }
    }
    
    resize() {
        if (this.cy) {
            this.cy.resize();
            this.cy.fit(null, 50);
        }
    }
    
    toggleFullscreen() {
        const dagSection = this.container.closest('.dag-panel-right');
        if (!dagSection) return;
        
        const fullscreenBtn = document.getElementById('fullscreenBtn');
        const enterIcon = fullscreenBtn.querySelector('.fullscreen-enter-icon');
        const exitIcon = fullscreenBtn.querySelector('.fullscreen-exit-icon');
        
        if (!this.isFullscreen) {
            dagSection.classList.add('fullscreen');
            this.isFullscreen = true;
            enterIcon.style.display = 'none';
            exitIcon.style.display = 'block';
            fullscreenBtn.title = 'Exit Fullscreen';
            setTimeout(() => {
                this.cy.resize();
                this.cy.fit(null, 50);
            }, 100);
        } else {
            dagSection.classList.remove('fullscreen');
            this.isFullscreen = false;
            enterIcon.style.display = 'block';
            exitIcon.style.display = 'none';
            fullscreenBtn.title = 'Toggle Fullscreen';
            setTimeout(() => {
                this.cy.resize();
                this.cy.fit(null, 50);
            }, 100);
        }
    }
    
    clear() {
        if (this.cy) {
            this.cy.elements().remove();
        }
    }
    
    async exportPNG() {
        if (!this.cy) {
            console.error('Cytoscape instance not initialized');
            throw new Error('DAG not initialized');
        }
        
        try {
            // Generate PNG with high quality
            const pngDataUrl = this.cy.png({
                full: true,
                scale: 2,
                bg: '#ffffff',
                maxWidth: 4096,
                maxHeight: 4096
            });
            
            // Convert data URL to blob
            const response = await fetch(pngDataUrl);
            const blob = await response.blob();
            
            // Copy to clipboard using Clipboard API
            await navigator.clipboard.write([
                new ClipboardItem({
                    'image/png': blob
                })
            ]);
            
            console.log('DAG image copied to clipboard successfully');
        } catch (error) {
            console.error('Failed to copy DAG image:', error);
            throw error;
        }
    }
}
