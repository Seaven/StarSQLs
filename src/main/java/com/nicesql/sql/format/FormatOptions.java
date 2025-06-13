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
    public boolean isCompact = false;

    public boolean isFormat = true;

    public String newLine = "";

    public String indent = "    ";

    public int spaceBeforeComma = 0;

    public int spaceAfterComma = 1;

    public int maxLineLength = 120;

    public boolean upperCaseKeyWords = false;

    public boolean expandFunctionArgs = false;

    public boolean expandCaseWhen = false;

    public boolean expandInList = false;

    public boolean expandBetween = false;

    public boolean expandAndOr = false;

    public boolean breakExplain = false;

    public boolean breakCTE = true;

    public boolean breakJoinKeyWord = false;

    public boolean breakJoinOn = false;

    public boolean breakSelectItems = false;

    public boolean breakGroupByItems = false;

    public boolean breakOrderBy = false;

    public boolean breakLimit = false;

    public boolean alignSelectItems = false;

    public boolean alignSelectAs = false;

    public boolean alignJoinRelations = false;

    public boolean alignJoinRelationsAs = false;

    public boolean alignWhere  = false;

    public boolean alignGroupBy = false;

    public boolean formatSubquery = false;

    public boolean simplifyBrackets = false;
}
