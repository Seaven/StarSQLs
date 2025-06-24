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
//

package com.starsqls.idea.tool;

import com.intellij.ui.components.JBCheckBox;

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;

public class OptionsPanelBuilder {
    private final Map<String, JBCheckBox> optionBoxes = new LinkedHashMap<>();

    private final JPanel optionsPanel;

    private final int colsMax;

    private int cols = 0;

    private int row = 0;

    public OptionsPanelBuilder(int rows, int cols) {
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(rows, cols));
        this.colsMax = cols;
    }

    public Map<String, JBCheckBox> getOptionBoxes() {
        return optionBoxes;
    }

    public JPanel build() {
        return optionsPanel;
    }

    public OptionsPanelBuilder addOption(String key, String label, boolean selected) {
        JBCheckBox checkBox = new JBCheckBox(label, selected);
        optionBoxes.put(key, checkBox);
        optionsPanel.add(checkBox);
        cols++;
        return this;
    }

    public OptionsPanelBuilder add(JComponent component) {
        optionsPanel.add(component);
        cols++;
        return this;
    }

    public OptionsPanelBuilder row() {
        if (row != 0 && cols % colsMax != 0) {
            for (int i = 0; i < colsMax - cols % colsMax; i++) {
                optionsPanel.add(new JPanel());
            }
        }
        cols = 0;
        row++;
        return this;
    }
}
