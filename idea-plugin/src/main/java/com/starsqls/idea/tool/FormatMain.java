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
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.getComponent().add(createMainPanel(project));
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

        // Build UI
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
        for (JBCheckBox box : optionBoxes.values()) {
            optionsPanel.add(box);
        }
        // Options area (checkbox)
        Map<String, JBCheckBox> optionBoxes = new LinkedHashMap<>();
        FormatOptions options = FormatOptions.defaultOptions();
        optionBoxes.put("upperCaseKeyWords", new JBCheckBox("Uppercase", options.upperCaseKeyWords));

        optionBoxes.put("spaceBeforeComma", new JBCheckBox("Space before comma", options.spaceBeforeComma));
        optionBoxes.put("spaceAfterComma", new JBCheckBox("Space after comma", options.spaceAfterComma));
        optionBoxes.put("breakFunctionArgs", new JBCheckBox("Break function args", options.breakFunctionArgs));
        optionBoxes.put("alignFunctionArgs", new JBCheckBox("Align function args", options.alignFunctionArgs));
        optionBoxes.put("breakCaseWhen", new JBCheckBox("Break case when", options.breakCaseWhen));
        optionBoxes.put("breakInList", new JBCheckBox("Break in list", options.breakInList));
        optionBoxes.put("breakAndOr", new JBCheckBox("Break and/or", options.breakAndOr));
        optionBoxes.put("breakExplain", new JBCheckBox("Break explain", options.breakExplain));
        optionBoxes.put("breakCTE", new JBCheckBox("Break CTE", options.breakCTE));
        optionBoxes.put("breakJoinRelations",
                new JBCheckBox("Break join relations", options.breakJoinRelations));
        optionBoxes.put("breakJoinOn", new JBCheckBox("Break join on", options.breakJoinOn));
        optionBoxes.put("breakSelectItems", new JBCheckBox("Break select items", options.breakSelectItems));
        optionBoxes.put("breakGroupByItems", new JBCheckBox("Break group by items", options.breakGroupByItems));
        optionBoxes.put("breakOrderBy", new JBCheckBox("Break order by", options.breakOrderBy));
        optionBoxes.put("formatSubquery", new JBCheckBox("Format subquery", options.formatSubquery));

        // Additional parameters
        JBTextField indentField = new JBTextField(options.indent);
        JBTextField maxLineLengthField = new JBTextField(String.valueOf(options.maxLineLength));

        optionsPanel.add(new JLabel("Indent:"));
        optionsPanel.add(indentField);
        optionsPanel.add(new JLabel("Max Line Length:"));
        optionsPanel.add(maxLineLengthField);

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
                .addComponent(new JLabel("Format Options:"))
                .addComponent(optionsPanel)
                .addComponent(btnPanel)
                .getPanel();
    }

    private void addOptionBoxes(Map<String, JBCheckBox> optionBoxes, String key, String label, FormatOptions options) {
        optionBoxes.put("spaceBeforeComma", new JBCheckBox("Space before comma", options.spaceBeforeComma));
    }

    private FormatOptions collectOptions(Map<String, JBCheckBox> boxes, JBTextField indentField,
                                         JBTextField maxLineLengthField, boolean forceCompact) {
        FormatOptions opts = new FormatOptions();
        opts.isCompact = forceCompact;
        if (opts.isCompact) {
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