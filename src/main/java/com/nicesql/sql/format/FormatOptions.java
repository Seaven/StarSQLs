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

    public boolean isFormat = false;

    public String newLine = "";

    public String indent = "    ";

    public int spaceBeforeComma = 0;

    public int spaceAfterComma = 0;

    public int maxLineLength = 120;

    public boolean upperCaseKeyWords = false;

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

    public boolean breakLimit = false;

    public boolean alignJoinRelations = false;

    public boolean alignJoinRelationsAs = false;

    public boolean formatSubquery = false;

    // ================================
    // simplify flags
    // ================================
    public boolean simplifyBrackets = false;
}
