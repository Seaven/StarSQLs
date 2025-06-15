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

package com.nicesql.sql.format;

public class FormatOptions {
    public boolean isCompact = true;

    public String indent = "";

    public boolean spaceBeforeComma = false;

    public boolean spaceAfterComma = false;

    public int maxLineLength = Integer.MAX_VALUE;

    public boolean upperCaseKeyWords = false;

    public boolean lowerCaseKeyWords = false;

    // ================================
    // Expressions keywords
    // ================================
    public boolean breakFunctionArgs = false;

    public boolean alignFunctionArgs = false;

    public boolean breakCaseWhen = false;

    public boolean alignCaseWhen = false;

    public boolean breakInList = false;

    public boolean alignInList = false;

    public boolean breakBetween = false;

    public boolean alignBetween = false;

    public boolean breakAndOr = false;

    public boolean alignAndOr = false;

    // ================================
    // Statements keywords
    // ================================
    public boolean breakExplain = false;

    public boolean breakCTE = true;

    public boolean breakJoinRelations = false;

    public boolean breakJoinOn = false;

    public boolean breakSelectItems = false;

    public boolean alignSelectItems = false;

    public boolean alignSelectAs = false;

    public boolean breakGroupByItems = false;

    public boolean alignGroupBy = false;

    public boolean breakOrderBy = false;

    public boolean alignOrderBy = false;

    public boolean alignJoinRelations = false;

    public boolean alignJoinRelationsAs = false;

    public boolean formatSubquery = false;

    // ================================
    // simplify flags
    // ================================
    public boolean simplifyBrackets = false;

    public static FormatOptions allFormatOptions() {
        FormatOptions options = new FormatOptions();
        options.isCompact = false;
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
        options.alignCaseWhen = true;
        options.breakInList = true;
        options.alignInList = true;
        options.breakBetween = true;
        options.alignBetween = true;
        options.breakAndOr = true;
        options.alignAndOr = true;

        // Statements keywords
        options.breakExplain = true;
        options.breakCTE = true;
        options.breakJoinRelations = true;
        options.breakJoinOn = true;
        options.breakSelectItems = true;
        options.alignSelectItems = true;
        options.alignSelectAs = true;
        options.breakGroupByItems = true;
        options.alignGroupBy = true;
        options.breakOrderBy = true;
        options.alignOrderBy = true;
        options.breakLimit = true;
        options.alignJoinRelations = true;
        options.alignJoinRelationsAs = true;
        options.formatSubquery = true;

        // simplify flags
        options.simplifyBrackets = true;
        return options;
    }
}
