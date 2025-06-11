// Copyright 2021-present StarRocks, Inc. All rights reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

lexer grammar GenericLex;
tokens {
    CONCAT
}

ALL: 'ALL';
ANALYZE: 'ANALYZE';
AND: 'AND';
ANTI: 'ANTI';
ARRAY: 'ARRAY';
ARRAY_AGG: 'ARRAY_AGG';
ARRAY_AGG_DISTINCT: 'ARRAY_AGG_DISTINCT';
AS: 'AS';
ASC: 'ASC';
ASSERT_ROWS: 'ASSERT_ROWS';
AVG: 'AVG';
AWARE: 'AWARE';
BETWEEN: 'BETWEEN';
BIGINT: 'BIGINT';
BINARY: 'BINARY';
BITMAP: 'BITMAP';
BOOLEAN: 'BOOLEAN';
BY: 'BY';
CASE: 'CASE';
CAST: 'CAST';
CATALOG: 'CATALOG';
CEIL: 'CEIL';
CHAR: 'CHAR';
COLLATE: 'COLLATE';
CONVERT: 'CONVERT';
COSTS: 'COSTS';
COUNT: 'COUNT';
CROSS: 'CROSS';
CUBE: 'CUBE';
CUME_DIST: 'CUME_DIST';
CURRENT: 'CURRENT';
CURRENT_DATE: 'CURRENT_DATE';
CURRENT_GROUP: 'CURRENT_GROUP';
CURRENT_ROLE: 'CURRENT_ROLE';
CURRENT_TIME: 'CURRENT_TIME';
CURRENT_TIMESTAMP: 'CURRENT_TIMESTAMP';
CURRENT_USER: 'CURRENT_USER';
DATABASE: 'DATABASE';
DATE: 'DATE';
DATETIME: 'DATETIME';
DAY: 'DAY';
DECIMAL: 'DECIMAL';
DECIMALV2: 'DECIMALV2';
DECIMAL32: 'DECIMAL32';
DECIMAL64: 'DECIMAL64';
DECIMAL128: 'DECIMAL128';
DENSE_RANK: 'DENSE_RANK';
NTILE: 'NTILE';
DESC: 'DESC';
DISTINCT: 'DISTINCT';
DOUBLE: 'DOUBLE';
DUAL: 'DUAL';
ELSE: 'ELSE';
END: 'END';
EXCEPT: 'EXCEPT';
EXCLUDE: 'EXCLUDE';
EXISTS: 'EXISTS';
EXPLAIN: 'EXPLAIN';
EXTRACT: 'EXTRACT';
FALSE: 'FALSE';
FIRST: 'FIRST';
FIRST_VALUE: 'FIRST_VALUE';
FLOAT: 'FLOAT';
FLOOR: 'FLOOR';
FN: 'FN';
FOLLOWING: 'FOLLOWING';
FOR: 'FOR';
FROM: 'FROM';
FULL: 'FULL';
GLOBAL: 'GLOBAL';
GROUP: 'GROUP';
GROUPING: 'GROUPING';
GROUPING_ID: 'GROUPING_ID';
GROUP_CONCAT: 'GROUP_CONCAT';
HAVING: 'HAVING';
HLL: 'HLL';
HLL_UNION: 'HLL_UNION';
HOUR: 'HOUR';
HOURS: 'HOURS';
IF: 'IF';
IGNORE: 'IGNORE';
IN: 'IN';
INNER: 'INNER';
INT: 'INT';
INTEGER: 'INTEGER';
INTERSECT: 'INTERSECT';
INTERVAL: 'INTERVAL';
IS: 'IS';
JOIN: 'JOIN';
JSON: 'JSON';
LAG: 'LAG';
LARGEINT: 'LARGEINT';
LAST: 'LAST';
LAST_VALUE: 'LAST_VALUE';
LATERAL: 'LATERAL';
LEAD: 'LEAD';
LEFT: 'LEFT';
LIKE: 'LIKE';
LIMIT: 'LIMIT';
LOCAL: 'LOCAL';
LOCALTIME: 'LOCALTIME';
LOCALTIMESTAMP: 'LOCALTIMESTAMP';
LOGICAL: 'LOGICAL';
MAP: 'MAP';
MATCH: 'MATCH';
MAX: 'MAX';
MICROSECOND: 'MICROSECOND';
MILLISECOND: 'MILLISECOND';
MIN: 'MIN';
MINUTE: 'MINUTE';
MINUS: 'MINUS';
MOD: 'MOD';
MONTH: 'MONTH';
NOT: 'NOT';
NULL: 'NULL';
NULLS: 'NULLS';
NUMBER: 'NUMBER';
NUMERIC: 'NUMERIC';
OF: 'OF';
OFFSET: 'OFFSET';
ON: 'ON';
OR: 'OR';
ORDER: 'ORDER';
OUTER: 'OUTER';
OVER: 'OVER';
PARAMETER: '?';
PARTITION: 'PARTITION';
PARTITIONS: 'PARTITIONS';
PASSWORD: 'PASSWORD';
PERCENT_RANK: 'PERCENT_RANK';
PERCENTILE: 'PERCENTILE';
PIVOT: 'PIVOT';
PRECEDING: 'PRECEDING';
QUALIFY: 'QUALIFY';
QUARTER: 'QUARTER';
RANGE: 'RANGE';
RANK: 'RANK';
REGEXP: 'REGEXP';
REPLACE: 'REPLACE';
REPLICA: 'REPLICA';
RIGHT: 'RIGHT';
RLIKE: 'RLIKE';
ROLLUP: 'ROLLUP';
ROW: 'ROW';
ROWS: 'ROWS';
ROW_NUMBER: 'ROW_NUMBER';
SCHEMA: 'SCHEMA';
SECOND: 'SECOND';
SELECT: 'SELECT';
SEMI: 'SEMI';
SEPARATOR: 'SEPARATOR';
SESSION: 'SESSION';
SETS: 'SETS';
SIGNED: 'SIGNED';
SMALLINT: 'SMALLINT';
STRING: 'STRING';
TEXT: 'TEXT';
SUM: 'SUM';
SYSTEM_TIME: 'SYSTEM_TIME';
STRUCT: 'STRUCT';
TABLE: 'TABLE';
TABLET: 'TABLET';
TEMPORARY: 'TEMPORARY';
THEN: 'THEN';
TIME: 'TIME';
TIMESTAMP: 'TIMESTAMP';
TIMESTAMPADD: 'TIMESTAMPADD';
TIMESTAMPDIFF: 'TIMESTAMPDIFF';
TINYINT: 'TINYINT';
TRANSLATE: 'TRANSLATE';
TO: 'TO';
TRUE: 'TRUE';
UNBOUNDED: 'UNBOUNDED';
UNION: 'UNION';
UNSIGNED: 'UNSIGNED';
USER: 'USER';
USING: 'USING';
VALUES: 'VALUES';
VARBINARY: 'VARBINARY';
VARCHAR: 'VARCHAR';
VERBOSE: 'VERBOSE';
VERSION: 'VERSION';
WEEK: 'WEEK';
WHEN: 'WHEN';
WHERE: 'WHERE';
WITH: 'WITH';
YEAR: 'YEAR';
BEFORE: 'BEFORE';

EQ  : '=';
NEQ : '<>' | '!=';
LT  : '<';
LTE : '<=';
GT  : '>';
GTE : '>=';
EQ_FOR_NULL: '<=>';
ARRAY_ELEMENT: '[*]';

PLUS_SYMBOL: '+';
MINUS_SYMBOL: '-';
ASTERISK_SYMBOL: '*';
SLASH_SYMBOL: '/';
PERCENT_SYMBOL: '%';

LOGICAL_OR: '||' ;
LOGICAL_AND: '&&';
LOGICAL_NOT: '!';

INT_DIV: 'DIV';
BITAND: '&';
BITOR: '|';
BITXOR: '^';
BITNOT: '~';
BIT_SHIFT_LEFT: 'BITSHIFTLEFT';
BIT_SHIFT_RIGHT: 'BITSHIFTRIGHT';
BIT_SHIFT_RIGHT_LOGICAL: 'BITSHIFTRIGHTLOGICAL';

ARROW: '->';
AT: '@';

INTEGER_VALUE
    : DIGIT+
    ;

DECIMAL_VALUE
    : DIGIT+ '.' DIGIT*
    | '.' DIGIT+
    ;

DOUBLE_VALUE
    : DIGIT+ ('.' DIGIT*)? EXPONENT
    | '.' DIGIT+ EXPONENT
    ;

SINGLE_QUOTED_TEXT
    : '\'' ('\\'. | '\'\'' | ~('\'' | '\\'))* '\''
    ;

DOUBLE_QUOTED_TEXT
    : '"' ('\\'. | '""' | ~('"'| '\\'))* '"'
    ;

BINARY_SINGLE_QUOTED_TEXT
    : 'X\'' (~('\'' | '\\'))* '\''
    ;

BINARY_DOUBLE_QUOTED_TEXT
    : 'X"' (~('"'| '\\'))* '"'
    ;

LETTER_IDENTIFIER
    : (LETTER | '_') (LETTER | DIGIT | '_')*
    ;

DIGIT_IDENTIFIER
    : DIGIT (LETTER | DIGIT | '_')+
    ;

BACKQUOTED_IDENTIFIER
    : '`' ( ~'`' | '``' )* '`'
    ;

// Prevent recognize string:         .123somelatin AS ((.123), DECIMAL_LITERAL), ((somelatin), IDENTIFIER)
// it must recoginze:                .123somelatin AS ((.), DOT), (123somelatin, IDENTIFIER)
DOT_IDENTIFIER
    : '.' DIGIT_IDENTIFIER
    ;

fragment EXPONENT
    : 'E' [+-]? DIGIT+
    ;

fragment DIGIT
    : [0-9]
    ;

fragment LETTER
    : [a-zA-Z_$\u0080-\uffff]
    ;

SIMPLE_COMMENT
    : '--' ~[\r\n]* '\r'? '\n'? -> channel(HIDDEN)
    ;

BRACKETED_COMMENT
    : '/*'([ \r\n\t\u3000]* | ~'+' .*?) '*/' -> channel(2)
    ;

OPTIMIZER_HINT
    : '/*+' .*? '*/' -> channel(2)
    ;

SEMICOLON: ';';

DOTDOTDOT: '...';

WS
    : [ \r\n\t\u3000]+ -> channel(HIDDEN)
    ;