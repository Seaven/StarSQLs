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
import com.intellij.util.ui.FormBuilder;
import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;
import org.jetbrains.annotations.NotNull;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
        builder.row().add(new JLabel("Indent:")).add("indent", new JBTextField(options.indent));
        builder.row().add(new JLabel("Max Line Length:"))
                .add("lineLength", new JBTextField(String.valueOf(options.maxLineLength)));

        // Options area (checkbox)
        builder.row().add(new JLabel("Keyword: ")).add("keyword", new ComboBox<>(
                new CollectionComboBoxModel<>(List.of(FormatOptions.KeyWordStyle.values()), options.keyWordStyle)));
        builder.row().add(new JLabel("Comma: ")).add("comma",
                new ComboBox<>(new CollectionComboBoxModel<>(List.of(FormatOptions.CommaStyle.values()),
                        options.commaStyle)));
        builder.row().add(new JLabel("Expressions: "));
        builder.row().add("breakFunctionArgs", new JBCheckBox("Break function args", options.breakFunctionArgs))
                .add("breakFunctionArgs", new JBCheckBox("Break function args", options.breakFunctionArgs))
                .add("alignFunctionArgs", new JBCheckBox("Align function args", options.alignFunctionArgs))
                .add("breakCaseWhen", new JBCheckBox("Break case when", options.breakCaseWhen))
                .add("breakInList", new JBCheckBox("Break in list", options.breakInList))
                .add("breakAndOr", new JBCheckBox("Break and/or", options.breakAndOr));
        builder.row().add(new JLabel("Statements: "));
        builder.row().add("breakExplain", new JBCheckBox("Break explain", options.breakExplain))
                .add("breakCTE", new JBCheckBox("Break CTE", options.breakCTE))
                .add("breakJoinRelations", new JBCheckBox("Break join relations", options.breakJoinRelations))
                .add("breakJoinOn", new JBCheckBox("Break join on", options.breakJoinOn))
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
            formatSql(sqlArea, opts);
        });
        minifyBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(builder, true);
            formatSql(sqlArea, opts);
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.add(formatBtn);
        btnPanel.add(minifyBtn);

        return FormBuilder.createFormBuilder().addComponent(new JLabel("SQL Input/Output:")).addComponent(scrollPane)
                .addComponent(builder.build()).addComponent(btnPanel).getPanel();
    }

    private FormatOptions collectOptions(OptionsPanelBuilder builder, boolean isMinify) {
        FormatOptions opts = new FormatOptions();
        opts.isMinify = isMinify;
        if (opts.isMinify) {
            return opts;
        }
        opts.keyWordStyle = (FormatOptions.KeyWordStyle) builder.<ComboBox>get("keyword").getSelectedItem();
        opts.commaStyle = (FormatOptions.CommaStyle) builder.<ComboBox>get("spaceBeforeComma").getSelectedItem();
        opts.breakFunctionArgs = builder.<JBCheckBox>get("breakFunctionArgs").isSelected();
        opts.alignFunctionArgs = builder.<JBCheckBox>get("alignFunctionArgs").isSelected();
        opts.breakCaseWhen = builder.<JBCheckBox>get("breakCaseWhen").isSelected();
        opts.breakInList = builder.<JBCheckBox>get("breakInList").isSelected();
        opts.breakAndOr = builder.<JBCheckBox>get("breakAndOr").isSelected();
        opts.breakExplain = builder.<JBCheckBox>get("breakExplain").isSelected();
        opts.breakCTE = builder.<JBCheckBox>get("breakCTE").isSelected();
        opts.breakJoinRelations = builder.<JBCheckBox>get("breakJoinRelations").isSelected();
        opts.breakJoinOn = builder.<JBCheckBox>get("breakJoinOn").isSelected();
        opts.breakSelectItems = builder.<JBCheckBox>get("breakSelectItems").isSelected();
        opts.breakGroupByItems = builder.<JBCheckBox>get("breakGroupByItems").isSelected();
        opts.breakOrderBy = builder.<JBCheckBox>get("breakOrderBy").isSelected();
        opts.formatSubquery = builder.<JBCheckBox>get("formatSubquery").isSelected();
        opts.indent = builder.<JBTextField>get("indent").getText();
        String maxLineLengthText = builder.<JBTextField>get("lineLength").getText();
        try {
            opts.maxLineLength = Integer.parseInt(maxLineLengthText);
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