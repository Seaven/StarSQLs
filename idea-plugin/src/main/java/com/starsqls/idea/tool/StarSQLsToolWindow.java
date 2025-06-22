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

public class StarSQLsToolWindow implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.getComponent().add(createMainPanel(project));
    }

    private JComponent createMainPanel(Project project) {
        // SQL输入输出区
        JBTextArea sqlArea = new JBTextArea(10, 60);
        sqlArea.setLineWrap(true);
        JBScrollPane scrollPane = new JBScrollPane(sqlArea);

        // 选项区（checkbox）
        Map<String, JBCheckBox> optionBoxes = new LinkedHashMap<>();
        FormatOptions defaultOptions = FormatOptions.defaultOptions();
        optionBoxes.put("isCompact", new JBCheckBox("Compact (压缩)", defaultOptions.isCompact));
        optionBoxes.put("upperCaseKeyWords", new JBCheckBox("Uppercase Keywords", defaultOptions.upperCaseKeyWords));
        optionBoxes.put("lowerCaseKeyWords", new JBCheckBox("Lowercase Keywords", defaultOptions.lowerCaseKeyWords));
        optionBoxes.put("spaceBeforeComma", new JBCheckBox("Space Before Comma", defaultOptions.spaceBeforeComma));
        optionBoxes.put("spaceAfterComma", new JBCheckBox("Space After Comma", defaultOptions.spaceAfterComma));
        optionBoxes.put("breakFunctionArgs", new JBCheckBox("Break Function Args", defaultOptions.breakFunctionArgs));
        optionBoxes.put("alignFunctionArgs", new JBCheckBox("Align Function Args", defaultOptions.alignFunctionArgs));
        optionBoxes.put("breakCaseWhen", new JBCheckBox("Break Case When", defaultOptions.breakCaseWhen));
        optionBoxes.put("breakInList", new JBCheckBox("Break In List", defaultOptions.breakInList));
        optionBoxes.put("breakAndOr", new JBCheckBox("Break And/Or", defaultOptions.breakAndOr));
        optionBoxes.put("breakExplain", new JBCheckBox("Break Explain", defaultOptions.breakExplain));
        optionBoxes.put("breakCTE", new JBCheckBox("Break CTE", defaultOptions.breakCTE));
        optionBoxes.put("breakJoinRelations",
                new JBCheckBox("Break Join Relations", defaultOptions.breakJoinRelations));
        optionBoxes.put("breakJoinOn", new JBCheckBox("Break Join On", defaultOptions.breakJoinOn));
        optionBoxes.put("breakSelectItems", new JBCheckBox("Break Select Items", defaultOptions.breakSelectItems));
        optionBoxes.put("breakGroupByItems", new JBCheckBox("Break Group By Items", defaultOptions.breakGroupByItems));
        optionBoxes.put("breakOrderBy", new JBCheckBox("Break Order By", defaultOptions.breakOrderBy));
        optionBoxes.put("formatSubquery", new JBCheckBox("Format Subquery", defaultOptions.formatSubquery));

        // 额外参数
        JBTextField indentField = new JBTextField(defaultOptions.indent);
        JBTextField maxLineLengthField = new JBTextField(String.valueOf(defaultOptions.maxLineLength));

        // 按钮
        JButton formatBtn = new JButton("格式化");
        JButton compactBtn = new JButton("压缩");

        // 按钮事件
        formatBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(optionBoxes, indentField, maxLineLengthField, false);
            formatSql(sqlArea, opts);
        });
        compactBtn.addActionListener(e -> {
            FormatOptions opts = collectOptions(optionBoxes, indentField, maxLineLengthField, true);
            formatSql(sqlArea, opts);
        });

        // 构建UI
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
        for (JBCheckBox box : optionBoxes.values()) {
            optionsPanel.add(box);
        }
        optionsPanel.add(new JLabel("Indent:"));
        optionsPanel.add(indentField);
        optionsPanel.add(new JLabel("Max Line Length:"));
        optionsPanel.add(maxLineLengthField);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnPanel.add(formatBtn);
        btnPanel.add(compactBtn);

        JPanel mainPanel = FormBuilder.createFormBuilder()
                .addComponent(new JLabel("SQL 输入/输出："))
                .addComponent(scrollPane)
                .addComponent(new JLabel("格式化选项："))
                .addComponent(optionsPanel)
                .addComponent(btnPanel)
                .getPanel();
        return mainPanel;
    }

    private FormatOptions collectOptions(Map<String, JBCheckBox> boxes, JBTextField indentField,
                                         JBTextField maxLineLengthField, boolean forceCompact) {
        FormatOptions opts = new FormatOptions();
        opts.isCompact = forceCompact || boxes.get("isCompact").isSelected();
        opts.upperCaseKeyWords = boxes.get("upperCaseKeyWords").isSelected();
        opts.lowerCaseKeyWords = boxes.get("lowerCaseKeyWords").isSelected();
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
            sqlArea.setText("格式化失败: " + ex.getMessage());
        }
    }
} 