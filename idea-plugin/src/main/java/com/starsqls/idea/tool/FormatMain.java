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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextArea;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormatMain implements ToolWindowFactory {
    Map<String, JBCheckBox> optionBoxes = new LinkedHashMap<>();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.getComponent().add(createMainPanel(project));
    }

    private JBCheckBox registerOption(String key, String label, boolean selected) {
        JBCheckBox checkBox = new JBCheckBox(label, selected);
        optionBoxes.put(key, checkBox);
        return checkBox;
    }

    private JComponent createMainPanel(Project project) {
        // Title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("StarSQLs");
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 18));
        titlePanel.add(titleLabel);

        // SQL input/output area
        JBTextArea sqlArea = new JBTextArea(12, 80);
        sqlArea.setLineWrap(true);
        JBScrollPane scrollPane = new JBScrollPane(sqlArea);

        FormatOptions options = FormatOptions.defaultOptions();

        // Build UI
        int row = 0;
        int col = 3;
        JPanel optionsPanel = new JPanel(new GridLayout(row, col));

        // Common parameters
        JBTextField indentField = new JBTextField(options.indent);
        JBTextField maxLineLengthField = new JBTextField(String.valueOf(options.maxLineLength));
        optionsPanel.add(new JLabel("Indent:"), row);
        optionsPanel.add(indentField, row + 1);
        optionsPanel.add(new JLabel("Max Line Length:"), row += col);
        optionsPanel.add(maxLineLengthField, row + 1);

        // Options area (checkbox)
        optionsPanel.add(registerOption("upperCaseKeywords", "Uppercase keywords", options.upperCaseKeyWords),
                row += col);

        optionsPanel.add(new JLabel("Comma: "), row += col);
        optionsPanel.add(registerOption("spaceBeforeComma", "Before comma", options.spaceBeforeComma), row + 1);
        optionsPanel.add(registerOption("spaceAfterComma", "After comma", options.spaceAfterComma), row + 1);
        optionsPanel.add(new JLabel("Expressions: "), row += col);
        optionsPanel.add(registerOption("breakFunctionArgs", "Break function args", options.breakFunctionArgs),
                row + 1);
        optionsPanel.add(registerOption("alignFunctionArgs", "Align function args", options.alignFunctionArgs));
        optionsPanel.add(registerOption("breakCaseWhen", "Break case when", options.breakCaseWhen));
        optionsPanel.add(registerOption("breakInList", "Break in list", options.breakInList));
        optionsPanel.add(registerOption("breakAndOr", "Break and/or", options.breakAndOr));
        optionsPanel.add(new JLabel("Statements: "), row = row / col + 1);
        optionsPanel.add(registerOption("breakExplain", "Break explain", options.breakExplain));
        optionsPanel.add(registerOption("breakCTE", "Break CTE", options.breakCTE));
        optionsPanel.add(registerOption("breakJoinRelations", "Break join relations", options.breakJoinRelations));
        optionsPanel.add(registerOption("breakJoinOn", "Break join on", options.breakJoinOn));
        optionsPanel.add(registerOption("breakSelectItems", "Break select items", options.breakSelectItems));
        optionsPanel.add(registerOption("breakGroupByItems", "Break group by items", options.breakGroupByItems));
        optionsPanel.add(registerOption("breakOrderBy", "Break order by", options.breakOrderBy));
        optionsPanel.add(registerOption("formatSubquery", "Format subquery", options.formatSubquery));

        // Buttons
        JButton formatBtn = new JButton("Format");
        JButton minifyBtn = new JButton("Minify");

        // Button events
        formatBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(optionBoxes, indentField, maxLineLengthField, false);
            formatSql(sqlArea, opts);
        });
        minifyBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(optionBoxes, indentField, maxLineLengthField, true);
            formatSql(sqlArea, opts);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(formatBtn);
        btnPanel.add(minifyBtn);

        return FormBuilder.createFormBuilder()
                .addComponent(titlePanel)
                .addComponent(new JLabel("SQL Input/Output:"))
                .addComponent(scrollPane)
                .addComponent(optionsPanel)
                .addComponent(btnPanel)
                .getPanel();
    }

    private FormatOptions collectOptions(Map<String, JBCheckBox> boxes, JBTextField indentField,
                                         JBTextField maxLineLengthField, boolean isMinify) {
        FormatOptions opts = new FormatOptions();
        opts.isMinify = isMinify;
        if (opts.isMinify) {
            return opts;
        }
        opts.upperCaseKeyWords = boxes.get("upperCaseKeyWords").isSelected();
        opts.spaceBeforeComma = boxes.get("spaceBeforeComma").isSelected();
        opts.spaceAfterComma = boxes.get("spaceAfterComma").isSelected();
        opts.breakFunctionArgs = boxes.get("breakFunctionArgs").isSelected();
        opts.alignFunctionArgs = boxes.get("alignFunctionArgs").isSelected();
        opts.breakCaseWhen = boxes.get("breakCaseWhen").isSelected();
        opts.breakInList = boxes.get("breakInList").isSelected();
        opts.breakAndOr = boxes.get("breakAndOr").isSelected();
        opts.breakExplain = boxes.get("breakExplain").isSelected();
        opts.breakCTE = boxes.get("breakCTE").isSelected();
        opts.breakJoinRelations = boxes.get("breakJoinRelations").isSelected();
        opts.breakJoinOn = boxes.get("breakJoinOn").isSelected();
        opts.breakSelectItems = boxes.get("breakSelectItems").isSelected();
        opts.breakGroupByItems = boxes.get("breakGroupByItems").isSelected();
        opts.breakOrderBy = boxes.get("breakOrderBy").isSelected();
        opts.formatSubquery = boxes.get("formatSubquery").isSelected();
        opts.indent = indentField.getText();
        try {
            opts.maxLineLength = Integer.parseInt(maxLineLengthField.getText());
        } catch (Exception e) {
            opts.maxLineLength = 120;
        }
        return opts;
    }

    private void formatSql(JBTextArea sqlArea, FormatOptions opts) {
        String sql = sqlArea.getText();
        try {
            FormatPrinter printer = new FormatPrinter(opts);
            String result = printer.format(sql);
            sqlArea.setText(result);
        } catch (Exception ex) {
            sqlArea.setText("Format failed: " + ex.getMessage());
        }
    }
}