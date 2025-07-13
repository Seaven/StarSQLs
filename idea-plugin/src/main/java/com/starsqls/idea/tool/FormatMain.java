// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.starsqls.idea.tool;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.CollectionComboBoxModel;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.content.Content;
import com.intellij.util.ui.FormBuilder;
import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;
import org.jetbrains.annotations.NotNull;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Color;

public class FormatMain implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel mainPanel = createMainPanel(project);
        toolWindow.getComponent().add(mainPanel);
        Content content = toolWindow.getContentManager().getFactory().createContent(mainPanel, "Formatter", true);
        toolWindow.getContentManager().addContent(content);
    }

    private JPanel createMainPanel(Project project) {
        // SQL input/output area
        JBTextArea sqlArea = new JBTextArea(12, 80);
        sqlArea.setLineWrap(true);
        JBScrollPane scrollPane = new JBScrollPane(sqlArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Add top and bottom margin

        // Error message area
        JTextArea errorArea = new JTextArea();
        errorArea.setEditable(false);
        errorArea.setLineWrap(true);
        errorArea.setWrapStyleWord(true);
        errorArea.setForeground(new Color(220, 53, 69)); // Bootstrap danger red - visible in both light and dark themes
        errorArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Error Messages"),
            BorderFactory.createEmptyBorder(2, 5, 2, 5)
        ));
        JBScrollPane errorScrollPane = new JBScrollPane(errorArea);
        errorScrollPane.setVisible(false); // Initially hidden
        // Set preferred size to limit height
        errorScrollPane.setPreferredSize(new java.awt.Dimension(0, 40)); // 40px height
        errorScrollPane.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 40)); // Force max height
        errorScrollPane.setMinimumSize(new java.awt.Dimension(0, 40)); // Force min height
        // Set scroll policy to prevent vertical expansion
        errorScrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        errorScrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        FormatOptions options = FormatOptions.defaultOptions();

        // Build UI
        OptionsPanelBuilder builder = new OptionsPanelBuilder(0, 3);

        // Common parameters - Improved indent configuration
        builder.row().add(new JLabel("Indent Character:")).add("indentChar", new ComboBox<>(
                new CollectionComboBoxModel<>(List.of("Space", "Tab"), "Space")));
        builder.row().add(new JLabel("Indent Size:")).add("indentSize", new JBTextField("4"));
        builder.row().add(new JLabel("Max Line Length:"))
                .add("lineLength", new JBTextField(String.valueOf(options.maxLineLength)));

        // Options area (checkbox)
        builder.row().add(new JLabel("Keyword: ")).add("keyword", new ComboBox<>(
                new CollectionComboBoxModel<>(List.of(FormatOptions.KeyWordStyle.values()), options.keyWordStyle)));
        builder.row().add(new JLabel("Comma: ")).add("comma", new ComboBox<>(
                new CollectionComboBoxModel<>(List.of(FormatOptions.CommaStyle.values()), options.commaStyle)));
        builder.row().add(new JLabel("Expressions: "));
        builder.row().add("breakFunctionArgs", new JBCheckBox("Break function args", options.breakFunctionArgs))
                .add("breakFunctionArgs", new JBCheckBox("Break function args", options.breakFunctionArgs))
                .add("alignFunctionArgs", new JBCheckBox("Align function args", options.alignFunctionArgs))
                .add("breakCaseWhen", new JBCheckBox("Break case when", options.breakCaseWhen))
                .add("alignCaseWhen", new JBCheckBox("Align case when", options.alignCaseWhen))
                .add("breakInList", new JBCheckBox("Break in list", options.breakInList))
                .add("alignInList", new JBCheckBox("Align in list", options.alignInList))
                .add("breakAndOr", new JBCheckBox("Break and/or", options.breakAndOr));
        builder.row().add(new JLabel("Statements: "));
        builder.row().add("breakExplain", new JBCheckBox("Break explain", options.breakExplain))
                .add("breakCTE", new JBCheckBox("Break CTE", options.breakCTE))
                .add("breakJoinRelations", new JBCheckBox("Break join relations", options.breakJoinRelations))
                .add("breakJoinOn", new JBCheckBox("Break join on", options.breakJoinOn))
                .add("alignJoinOn", new JBCheckBox("Align join on", options.alignJoinOn))
                .add("breakSelectItems", new JBCheckBox("Break select items", options.breakSelectItems))
                .add("breakGroupByItems", new JBCheckBox("Break group by items", options.breakGroupByItems))
                .add("breakOrderBy", new JBCheckBox("Break order by", options.breakOrderBy))
                .add("formatSubquery", new JBCheckBox("Format subquery", options.formatSubquery));

        // Buttons
        JButton formatBtn = new JButton("Format");
        JButton minifyBtn = new JButton("Minify");

        // Button events
        formatBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(builder, false);
            formatSql(sqlArea, errorArea, errorScrollPane, opts);
        });
        minifyBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(builder, true);
            formatSql(sqlArea, errorArea, errorScrollPane, opts);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5)); // Add 10px horizontal gap, 5px vertical gap
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Add top and bottom margin
        btnPanel.add(formatBtn);
        btnPanel.add(minifyBtn);

        // Create main panel with proper margins
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add 10px margin on all sides
        
        // Create content panel with FormBuilder
        JPanel contentPanel = FormBuilder.createFormBuilder()
                .addComponent(new JLabel("SQL Input/Output:"))
                .addComponent(scrollPane)
                .addComponent(errorScrollPane)
                .addComponent(builder.build())
                .addComponent(btnPanel)
                .getPanel();
        
        // Add margins to the content panel
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        return mainPanel;
    }

    private FormatOptions collectOptions(OptionsPanelBuilder builder, boolean isMinify) {
        FormatOptions opts = new FormatOptions();
        opts.mode = isMinify ? FormatOptions.Mode.MINIFY : FormatOptions.Mode.FORMAT;
        if (opts.mode == FormatOptions.Mode.MINIFY) {
            return opts;
        }
        opts.keyWordStyle = (FormatOptions.KeyWordStyle) builder.<ComboBox>get("keyword").getSelectedItem();
        opts.commaStyle = (FormatOptions.CommaStyle) builder.<ComboBox>get("comma").getSelectedItem();
        opts.breakFunctionArgs = builder.<JBCheckBox>get("breakFunctionArgs").isSelected();
        opts.alignFunctionArgs = builder.<JBCheckBox>get("alignFunctionArgs").isSelected();
        opts.breakCaseWhen = builder.<JBCheckBox>get("breakCaseWhen").isSelected();
        opts.alignCaseWhen = builder.<JBCheckBox>get("alignCaseWhen").isSelected();
        opts.breakInList = builder.<JBCheckBox>get("breakInList").isSelected();
        opts.alignInList = builder.<JBCheckBox>get("alignInList").isSelected();
        opts.breakAndOr = builder.<JBCheckBox>get("breakAndOr").isSelected();
        opts.breakExplain = builder.<JBCheckBox>get("breakExplain").isSelected();
        opts.breakCTE = builder.<JBCheckBox>get("breakCTE").isSelected();
        opts.breakJoinRelations = builder.<JBCheckBox>get("breakJoinRelations").isSelected();
        opts.breakJoinOn = builder.<JBCheckBox>get("breakJoinOn").isSelected();
        opts.alignJoinOn = builder.<JBCheckBox>get("alignJoinOn").isSelected();
        opts.breakSelectItems = builder.<JBCheckBox>get("breakSelectItems").isSelected();
        opts.breakGroupByItems = builder.<JBCheckBox>get("breakGroupByItems").isSelected();
        opts.breakOrderBy = builder.<JBCheckBox>get("breakOrderBy").isSelected();
        opts.formatSubquery = builder.<JBCheckBox>get("formatSubquery").isSelected();
        
        // Calculate indent based on character and size selection
        String indentChar = (String) builder.<ComboBox>get("indentChar").getSelectedItem();
        String indentSizeText = builder.<JBTextField>get("indentSize").getText();
        int indentSize;
        try {
            indentSize = Integer.parseInt(indentSizeText);
            if (indentSize < 1 || indentSize > 8) {
                indentSize = 4; // Default to 4 if out of range
            }
        } catch (Exception e) {
            indentSize = 4; // Default to 4 if invalid
        }
        
        // Generate indent string based on character and size
        if ("Tab".equals(indentChar)) {
            opts.indent = "\t".repeat(indentSize);
        } else {
            opts.indent = " ".repeat(indentSize);
        }
        
        String maxLineLengthText = builder.<JBTextField>get("lineLength").getText();
        try {
            opts.maxLineLength = Integer.parseInt(maxLineLengthText);
        } catch (Exception e) {
            opts.maxLineLength = 120;
        }
        return opts;
    }

    private void formatSql(JBTextArea sqlArea, JTextArea errorArea, JBScrollPane errorScrollPane, FormatOptions opts) {
        String sql = sqlArea.getText();
        try {
            FormatPrinter printer = new FormatPrinter(opts);
            String result = printer.format(sql);
            sqlArea.setText(result);
            // Hide error area on success and revalidate layout
            errorScrollPane.setVisible(false);
            errorArea.setText("");
            // Force layout update to properly hide the error area
            errorScrollPane.getParent().revalidate();
            errorScrollPane.getParent().repaint();
        } catch (Exception ex) {
            // Show error in separate area without clearing SQL
            String errorMsg = ex.getMessage();
            errorArea.setText("Format failed: " + errorMsg);
            errorScrollPane.setVisible(true);
            // Ensure error area is visible by revalidating the container
            errorScrollPane.getParent().revalidate();
            errorScrollPane.getParent().repaint();
        }
    }
}