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

package com.starsqls.format;

import com.google.common.base.Strings;
import com.google.gson.Gson;

public class FormatOptions {
    // ================================
    // mode
    // ================================
    public boolean isMinify = true;

    // ================================
    // common keywords
    // ================================
    public String indent = "";

    public int maxLineLength = Integer.MAX_VALUE;

    public boolean spaceBeforeComma = false;

    public boolean spaceAfterComma = false;

    public boolean upperCaseKeyWords = false;

    public boolean lowerCaseKeyWords = false;

    // ================================
    // Expressions keywords
    // ================================
    public boolean breakFunctionArgs = false;

    public boolean alignFunctionArgs = false;

    public boolean breakCaseWhen = false;

    public boolean breakInList = false;

    public boolean breakAndOr = false;

    // ================================
    // Statements keywords
    // ================================
    public boolean breakExplain = false;

    public boolean breakCTE = true;

    public boolean breakJoinRelations = false;

    public boolean breakJoinOn = false;

    public boolean breakSelectItems = false;

    public boolean breakGroupByItems = false;

    public boolean breakOrderBy = false;

    public boolean formatSubquery = false;

    public static FormatOptions allFormatOptions() {
        FormatOptions options = new FormatOptions();
        options.isMinify = false;
        options.indent = "    ";
        options.spaceBeforeComma = true;
        options.spaceAfterComma = true;
        options.maxLineLength = 120;
        options.upperCaseKeyWords = true;
        options.lowerCaseKeyWords = false;

        // Expressions keywords
        options.breakFunctionArgs = true;
        options.alignFunctionArgs = true;
        options.breakCaseWhen = true;
        options.breakInList = true;
        options.breakAndOr = true;

        // Statements keywords
        options.breakExplain = true;
        options.breakCTE = true;
        options.breakJoinRelations = true;
        options.breakJoinOn = true;
        options.breakSelectItems = true;
        options.breakGroupByItems = true;
        options.breakOrderBy = true;
        options.formatSubquery = true;

        return options;
    }

    public static FormatOptions defaultOptions() {
        FormatOptions options = new FormatOptions();
        options.isMinify = false;
        options.indent = Strings.repeat(" ", 4);
        options.spaceBeforeComma = false;
        options.spaceAfterComma = true;
        options.maxLineLength = 120;
        options.upperCaseKeyWords = true;
        options.lowerCaseKeyWords = false;

        // Expressions keywords
        options.breakFunctionArgs = false;
        options.alignFunctionArgs = true;
        options.breakCaseWhen = true;
        options.breakInList = false;
        options.breakAndOr = false;

        // Statements keywords
        options.breakExplain = false;
        options.breakCTE = true;
        options.breakJoinRelations = false;
        options.breakJoinOn = false;
        options.breakSelectItems = false;
        options.breakGroupByItems = false;
        options.breakOrderBy = false;
        options.formatSubquery = true;

        return options;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this, FormatOptions.class);
    }

    public static FormatOptions fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, FormatOptions.class);
    }
}
