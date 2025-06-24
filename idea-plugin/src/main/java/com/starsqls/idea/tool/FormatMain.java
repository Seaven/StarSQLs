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

import java.awt.*;
import java.util.Map;
import javax.swing.*;

public class FormatMain implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setTitle("StarSQLs");
        toolWindow.getComponent().add(createMainPanel(project));
    }

    private JComponent createMainPanel(Project project) {
        // SQL input/output area
        JBTextArea sqlArea = new JBTextArea(12, 80);
        sqlArea.setLineWrap(true);
        JBScrollPane scrollPane = new JBScrollPane(sqlArea);

        FormatOptions options = FormatOptions.defaultOptions();

        // Build UI
        OptionsPanelBuilder builder = new OptionsPanelBuilder(0, 3);

        // Common parameters
        JBTextField indentField = new JBTextField(options.indent);
        JBTextField maxLineLengthField = new JBTextField(String.valueOf(options.maxLineLength));
        builder.row().add(new JLabel("Indent:")).add(indentField);
        builder.row().add(new JLabel("Max Line Length:")).add(maxLineLengthField);

        // Options area (checkbox)
        builder.row().addOption("upperCaseKeywords", "Uppercase keywords", options.upperCaseKeyWords);
        builder.row().add(new JLabel("Comma: "));
        builder.row().addOption("spaceBeforeComma", "Before comma", options.spaceBeforeComma)
                .addOption("spaceAfterComma", "After comma", options.spaceAfterComma);
        builder.row().add(new JLabel("Expressions: "));
        builder.row().addOption("breakFunctionArgs", "Break function args", options.breakFunctionArgs)
                .addOption("breakFunctionArgs", "Break function args", options.breakFunctionArgs)
                .addOption("alignFunctionArgs", "Align function args", options.alignFunctionArgs)
                .addOption("breakCaseWhen", "Break case when", options.breakCaseWhen)
                .addOption("breakInList", "Break in list", options.breakInList)
                .addOption("breakAndOr", "Break and/or", options.breakAndOr);
        builder.row().add(new JLabel("Statements: "));
        builder.row().addOption("breakExplain", "Break explain", options.breakExplain)
                .addOption("breakCTE", "Break CTE", options.breakCTE)
                .addOption("breakJoinRelations", "Break join relations", options.breakJoinRelations)
                .addOption("breakJoinOn", "Break join on", options.breakJoinOn)
                .addOption("breakSelectItems", "Break select items", options.breakSelectItems)
                .addOption("breakGroupByItems", "Break group by items", options.breakGroupByItems)
                .addOption("breakOrderBy", "Break order by", options.breakOrderBy)
                .addOption("formatSubquery", "Format subquery", options.formatSubquery);

        // Buttons
        JButton formatBtn = new JButton("Format");
        JButton minifyBtn = new JButton("Minify");

        // Button events
        formatBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(builder.getOptionBoxes(), indentField, maxLineLengthField, false);
            formatSql(sqlArea, opts);
        });
        minifyBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(builder.getOptionBoxes(), indentField, maxLineLengthField, true);
            formatSql(sqlArea, opts);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(formatBtn);
        btnPanel.add(minifyBtn);

        return FormBuilder.createFormBuilder().addComponent(new JLabel("SQL Input/Output:")).addComponent(scrollPane)
                .addComponent(builder.build()).addComponent(btnPanel).getPanel();
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