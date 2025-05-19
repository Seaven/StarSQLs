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

grammar GenericeSQL;
import GenericLex;

sqlStatements
    : singleStatement+ EOF
    ;

singleStatement
    : (statement (SEMICOLON | EOF)) | emptyStatement
    ;
emptyStatement
    : SEMICOLON
    ;

statement
    // Query Statement
    : queryStatement
    | unsupportedStatement
    ;

subfieldName
    : identifier | ARRAY_ELEMENT
    ;

nestedFieldName
    : subfieldName (DOT_IDENTIFIER | '.' subfieldName)*
    ;

unsupportedStatement
    : LOCK TABLES lock_item (',' lock_item)*
    | UNLOCK TABLES
    ;

lock_item
    : identifier (AS? alias=identifier)? lock_type
    ;

lock_type
    : READ LOCAL?
    | LOW_PRIORITY? WRITE
    ;

// ------------------------------------------- Query Statement ---------------------------------------------------------

queryStatement
    : (explainDesc | optimizerTrace) ? queryRelation outfile?;

queryRelation
    : withClause? queryNoWith
    ;

withClause
    : WITH commonTableExpression (',' commonTableExpression)*
    ;

queryNoWith
    : queryPrimary (ORDER BY sortItem (',' sortItem)*)? (limitElement)?
    ;

queryPeriod
    : FOR? periodType BETWEEN expression AND expression
    | FOR? periodType FROM expression TO expression
    | FOR? periodType ALL
    | FOR? periodType AS OF end=expression
    ;

periodType
    : SYSTEM_TIME
    | TIMESTAMP
    | VERSION
    ;

queryPrimary
    : querySpecification                                                                    #queryPrimaryDefault
    | subquery                                                                              #queryWithParentheses
    | left=queryPrimary operator=INTERSECT setQuantifier? right=queryPrimary                #setOperation
    | left=queryPrimary operator=(UNION | EXCEPT | MINUS)
        setQuantifier? right=queryPrimary                                                   #setOperation
    ;

subquery
    : '(' queryRelation ')'
    ;

rowConstructor
     :'(' expressionList ')'
     ;

sortItem
    : expression ordering = (ASC | DESC)? (NULLS nullOrdering=(FIRST | LAST))?
    ;

limitConstExpr
    : INTEGER_VALUE
    | PARAMETER
    | userVariable
    ;

limitElement
    : LIMIT limit=limitConstExpr (OFFSET offset=limitConstExpr)?
    | LIMIT offset=limitConstExpr ',' limit=limitConstExpr
    ;

querySpecification
    : SELECT setQuantifier? selectItem (',' selectItem)*
      fromClause
      ((WHERE where=expression)? (GROUP BY groupingElement)? (HAVING having=expression)?
       (QUALIFY qualifyFunction=selectItem comparisonOperator limit=INTEGER_VALUE)?)
    ;

fromClause
    : (FROM relations pivotClause?)?                                                    #from
    | FROM DUAL                                                                         #dual
    ;

groupingElement
    : ROLLUP '(' (expressionList)? ')'                                                  #rollup
    | CUBE '(' (expressionList)? ')'                                                    #cube
    | GROUPING SETS '(' groupingSet (',' groupingSet)* ')'                              #multipleGroupingSets
    | expressionList                                                                    #singleGroupingSet
    ;

groupingSet
    : '(' expression? (',' expression)* ')'
    ;

commonTableExpression
    : name=identifier (columnAliases)? AS '(' queryRelation ')'
    ;

setQuantifier
    : DISTINCT
    | ALL
    ;

selectItem
    : expression (AS? (identifier | string))?                                            #selectSingle
    | qualifiedName '.' ASTERISK_SYMBOL excludeClause?                                   #selectAll
    | ASTERISK_SYMBOL excludeClause?                                                     #selectAll
    ;

excludeClause
    : ( EXCEPT | EXCLUDE ) '(' identifier (',' identifier)* ')'
    ;

relations
    : relation (',' LATERAL? relation)*
    ;

relation
    : relationPrimary joinRelation*
    | '(' relationPrimary joinRelation* ')'
    ;

relationPrimary
    : qualifiedName queryPeriod? partitionNames? tabletList? replicaList? sampleClause? (
        AS? alias=identifier)? bracketHint? (BEFORE ts=string)?                          #tableAtom
    | '(' VALUES rowConstructor (',' rowConstructor)* ')'
        (AS? alias=identifier columnAliases?)?                                          #inlineTable
    | ASSERT_ROWS? subquery (AS? alias=identifier columnAliases?)?                      #subqueryWithAlias
    | qualifiedName '(' expressionList ')'
        (AS? alias=identifier columnAliases?)?                                          #tableFunction
    | TABLE '(' qualifiedName '(' argumentList ')' ')'
        (AS? alias=identifier columnAliases?)?                                          #normalizedTableFunction
    | FILES propertyList
        (AS? alias=identifier columnAliases?)?                                          #fileTableFunction
    | '(' relations ')'                                                                 #parenthesizedRelation
    ;

pivotClause
    : PIVOT '(' pivotAggregationExpression (',' pivotAggregationExpression)*
        FOR (identifier | identifierList) IN '(' pivotValue (',' pivotValue)* ')' ')'
    ;

pivotAggregationExpression
    : functionCall (AS? (identifier | string))?
    ;


pivotValue
    : (literalExpression | literalExpressionList) (AS? (identifier | string))?
    ;

sampleClause
    : SAMPLE propertyList?
    ;

argumentList
    : expressionList
    | namedArgumentList
    ;

namedArgumentList
    : namedArgument (',' namedArgument)*
    ;

namedArgument
    : identifier '=>' expression                                                        #namedArguments
    ;

joinRelation
    : crossOrInnerJoinType bracketHint?
            LATERAL? rightRelation=relationPrimary joinCriteria?
    | outerAndSemiJoinType bracketHint?
            LATERAL? rightRelation=relationPrimary joinCriteria
    ;

crossOrInnerJoinType
    : JOIN | INNER JOIN
    | CROSS | CROSS JOIN
    ;

outerAndSemiJoinType
    : LEFT JOIN | RIGHT JOIN | FULL JOIN
    | LEFT OUTER JOIN | RIGHT OUTER JOIN
    | FULL OUTER JOIN
    | LEFT SEMI JOIN | RIGHT SEMI JOIN
    | LEFT ANTI JOIN | RIGHT ANTI JOIN
    | NULL AWARE LEFT ANTI JOIN
    ;

bracketHint
    : '[' identifier (',' identifier)* ']'
    | '[' identifier '|' primaryExpression literalExpressionList']'
    ;

hintMap
    : k=identifierOrString '=' v=literalExpression
    ;

joinCriteria
    : ON expression
    | USING '(' identifier (',' identifier)* ')'
    ;

columnAliases
    : '(' identifier (',' identifier)* ')'
    ;

// partitionNames should not support string, it should be identifier here only for compatibility with historical bugs
partitionNames
    : TEMPORARY? (PARTITION | PARTITIONS) '(' identifierOrString (',' identifierOrString)* ')'
    | TEMPORARY? (PARTITION | PARTITIONS) identifierOrString
    | keyPartitions
    ;

keyPartitions
    : PARTITION '(' keyPartition (',' keyPartition)* ')'                              #keyPartitionList
    ;

tabletList
    : TABLET '(' INTEGER_VALUE (',' INTEGER_VALUE)* ')'
    ;

prepareStatement
    : PREPARE identifier FROM prepareSql
    ;

prepareSql
    : statement
    | SINGLE_QUOTED_TEXT
    ;

executeStatement
    : EXECUTE identifier (USING  '@'identifierOrString (',' '@'identifierOrString)*)?
    ;

deallocateStatement
    : (DEALLOCATE | DROP) PREPARE identifier
    ;

replicaList
    : REPLICA '(' INTEGER_VALUE (',' INTEGER_VALUE)* ')'
    ;

// ------------------------------------------- Expression --------------------------------------------------------------

/**
 * Operator precedences are shown in the following list, from highest precedence to the lowest.
 *
 * !
 * - (unary minus), ~ (unary bit inversion)
 * ^
 * *, /, DIV, %, MOD
 * -, +
 * &
 * |
 * = (comparison), <=>, >=, >, <=, <, <>, !=, IS, LIKE, REGEXP
 * BETWEEN, CASE WHEN
 * NOT
 * AND, &&
 * XOR
 * OR, ||
 * = (assignment)
 */

expressionsWithDefault
    : '(' expressionOrDefault (',' expressionOrDefault)* ')'
    ;

expressionOrDefault
    : expression | DEFAULT
    ;

mapExpressionList
    : mapExpression (',' mapExpression)*
    ;

mapExpression
    : key=expression ':' value=expression
    ;

expressionSingleton
    : expression EOF
    ;

expression
    : (BINARY)? booleanExpression                                                         #expressionDefault
    | NOT expression                                                                      #logicalNot
    | left=expression operator=(AND|LOGICAL_AND) right=expression                         #logicalBinary
    | left=expression operator=(OR|LOGICAL_OR) right=expression                           #logicalBinary
    ;

expressionList
    : expression (',' expression)*
    ;

booleanExpression
    : predicate                                                                           #booleanExpressionDefault
    | booleanExpression IS NOT? NULL                                                      #isNull
    | left = booleanExpression comparisonOperator right = predicate                       #comparison
    | booleanExpression comparisonOperator '(' queryRelation ')'                          #scalarSubquery
    ;

predicate
    : valueExpression (predicateOperations[$valueExpression.ctx])?
    | tupleInSubquery
    ;

tupleInSubquery
    : '(' expression (',' expression)+ ')' NOT? IN '(' queryRelation ')'
    ;

predicateOperations [ParserRuleContext value]
    : NOT? IN '(' queryRelation ')'                                                       #inSubquery
    | NOT? IN '(' expressionList ')'                                                      #inList
    | NOT? BETWEEN lower = valueExpression AND upper = predicate                          #between
    | NOT? (LIKE | RLIKE | REGEXP) pattern=valueExpression                                #like
    ;

valueExpression
    : primaryExpression                                                                   #valueExpressionDefault
    | left = valueExpression operator = BITXOR right = valueExpression                    #arithmeticBinary
    | left = valueExpression operator = (
              ASTERISK_SYMBOL
            | SLASH_SYMBOL
            | PERCENT_SYMBOL
            | INT_DIV
            | MOD)
      right = valueExpression                                                             #arithmeticBinary
    | left = valueExpression operator = (PLUS_SYMBOL | MINUS_SYMBOL)
        right = valueExpression                                                           #arithmeticBinary
    | left = valueExpression operator = BITAND right = valueExpression                    #arithmeticBinary
    | left = valueExpression operator = BITOR right = valueExpression                     #arithmeticBinary
    | left = valueExpression operator = BIT_SHIFT_LEFT right = valueExpression              #arithmeticBinary
    | left = valueExpression operator = BIT_SHIFT_RIGHT right = valueExpression             #arithmeticBinary
    | left = valueExpression operator = BIT_SHIFT_RIGHT_LOGICAL right = valueExpression     #arithmeticBinary
    ;

primaryExpression
    : userVariable                                                                        #userVariableExpression
    | systemVariable                                                                      #systemVariableExpression
    | DICTIONARY_GET '(' expressionList ')'                                               #dictionaryGetExpr
    | functionCall                                                                        #functionCallExpression
    | '{' FN functionCall '}'                                                             #odbcFunctionCallExpression
    | primaryExpression COLLATE (identifier | string)                                     #collate
    | literalExpression                                                                   #literal
    | columnReference                                                                     #columnRef
    | base = primaryExpression (DOT_IDENTIFIER | '.' fieldName = identifier )             #dereference
    | left = primaryExpression CONCAT right = primaryExpression                           #concat
    | operator = (MINUS_SYMBOL | PLUS_SYMBOL | BITNOT) primaryExpression                  #arithmeticUnary
    | operator = LOGICAL_NOT primaryExpression                                            #arithmeticUnary
    | '(' expression ')'                                                                  #parenthesizedExpression
    | EXISTS '(' queryRelation ')'                                                        #exists
    | subquery                                                                            #subqueryExpression
    | CAST '(' expression AS type ')'                                                     #cast
    | CONVERT '(' expression ',' type ')'                                                 #convert
    | CASE caseExpr=expression whenClause+ (ELSE elseExpression=expression)? END          #simpleCase
    | CASE whenClause+ (ELSE elseExpression=expression)? END                              #searchedCase
    | arrayType? '[' (expressionList)? ']'                                                #arrayConstructor
    | mapType '{' (mapExpressionList)? '}'                                                #mapConstructor
    | MAP '{' (mapExpressionList)? '}'                                                    #mapConstructor
    | value=primaryExpression '[' index=valueExpression ']'                               #collectionSubscript
    | primaryExpression '[' start=INTEGER_VALUE? ':' end=INTEGER_VALUE? ']'               #arraySlice
    | primaryExpression ARROW string                                                      #arrowExpression
    | (identifier | identifierList) '->' expression                                       #lambdaFunctionExpr
    | identifierList '->' '('(expressionList)?')'                                         #lambdaFunctionExpr
    | left = primaryExpression NOT? MATCH right = primaryExpression                       #matchExpr
    ;

literalExpression
    : NULL                                                                                #nullLiteral
    | booleanValue                                                                        #booleanLiteral
    | number                                                                              #numericLiteral
    | (DATE | DATETIME) string                                                            #dateLiteral
    | string                                                                              #stringLiteral
    | interval                                                                            #intervalLiteral
    | unitBoundary                                                                        #unitBoundaryLiteral
    | binary                                                                              #binaryLiteral
    | PARAMETER                                                                           #Parameter
    ;

functionCall
    : EXTRACT '(' identifier FROM valueExpression ')'                                     #extract
    | GROUPING '(' (expression (',' expression)*)? ')'                                    #groupingOperation
    | GROUPING_ID '(' (expression (',' expression)*)? ')'                                 #groupingOperation
    | informationFunctionExpression                                                       #informationFunction
    | specialDateTimeExpression                                                           #specialDateTime
    | specialFunctionExpression                                                           #specialFunction
    | aggregationFunction over?                                                           #aggregationFunctionCall
    | windowFunction over                                                                 #windowFunctionCall
    | TRANSLATE '(' (expression (',' expression)*)? ')'                                   #translateFunctionCall
    | qualifiedName '(' (expression (',' expression)*)? ')'  over?                        #simpleFunctionCall
    ;

aggregationFunction
    : AVG '(' setQuantifier? expression ')'
    | COUNT '(' ASTERISK_SYMBOL? ')'
    | COUNT '(' (setQuantifier bracketHint?)? (expression (',' expression)*)? ')'
    | MAX '(' setQuantifier? expression ')'
    | MIN '(' setQuantifier? expression ')'
    | SUM '(' setQuantifier? expression ')'
    | ARRAY_AGG '(' setQuantifier? expression (ORDER BY sortItem (',' sortItem)*)? ')'
    | ARRAY_AGG_DISTINCT '(' expression (ORDER BY sortItem (',' sortItem)*)? ')'
    | GROUP_CONCAT '(' setQuantifier? expression (',' expression)* (ORDER BY sortItem (',' sortItem)*)? (SEPARATOR expression)? ')'
    ;

userVariable
    : AT identifierOrString
    ;

systemVariable
    : AT AT (varType '.')? identifier
    ;

columnReference
    : identifier
    ;

informationFunctionExpression
    : name = CATALOG '(' ')'
    | name = DATABASE '(' ')'
    | name = SCHEMA '(' ')'
    | name = USER '(' ')'
    | name = CURRENT_USER ('(' ')')?
    | name = CURRENT_ROLE ('(' ')')?
    | name = CURRENT_GROUP ('(' ')')?
    ;

specialDateTimeExpression
    : name = CURRENT_DATE ('(' ')')?
    | name = CURRENT_TIME ('(' ')')?
    | name = CURRENT_TIMESTAMP ('(' (INTEGER_VALUE)? ')')?
    | name = LOCALTIME ('(' ')')?
    | name = LOCALTIMESTAMP ('(' ')')?
    ;

specialFunctionExpression
    : CHAR '(' expression ')'
    | DAY '(' expression ')'
    | HOUR '(' expression ')'
    | IF '(' (expression (',' expression)*)? ')'
    | LEFT '(' expression ',' expression ')'
    | LIKE '(' expression ',' expression ')'
    | MINUTE '(' expression ')'
    | MOD '(' expression ',' expression ')'
    | MONTH '(' expression ')'
    | QUARTER '(' expression ')'
    | REGEXP '(' expression ',' expression ')'
    | REPLACE '(' (expression (',' expression)*)? ')'
    | RIGHT '(' expression ',' expression ')'
    | RLIKE '(' expression ',' expression ')'
    | SECOND '(' expression ')'
    | TIMESTAMPADD '(' unitIdentifier ',' expression ',' expression ')'
    | TIMESTAMPDIFF '(' unitIdentifier ',' expression ',' expression ')'
    //| WEEK '(' expression ')' TODO: Support week(expr) function
    | YEAR '(' expression ')'
    | PASSWORD '(' string ')'
    | FLOOR '(' expression ')'
    | CEIL '(' expression ')'
    ;

windowFunction
    : name = ROW_NUMBER '(' ')'
    | name = RANK '(' ')'
    | name = DENSE_RANK '(' ')'
    | name = CUME_DIST '(' ')'
    | name = PERCENT_RANK '(' ')'
    | name = NTILE  '(' expression? ')'
    | name = LEAD  '(' (expression ignoreNulls? (',' expression)*)? ')' ignoreNulls?
    | name = LAG '(' (expression ignoreNulls? (',' expression)*)? ')' ignoreNulls?
    | name = FIRST_VALUE '(' (expression ignoreNulls? (',' expression)*)? ')' ignoreNulls?
    | name = LAST_VALUE '(' (expression ignoreNulls? (',' expression)*)? ')' ignoreNulls?
    ;

whenClause
    : WHEN condition=expression THEN result=expression
    ;

over
    : OVER '('
        (bracketHint? PARTITION BY partition+=expression (',' partition+=expression)*)?
        (ORDER BY sortItem (',' sortItem)*)?
        windowFrame?
      ')'
    ;

ignoreNulls
    : IGNORE NULLS
    ;

windowFrame
    : frameType=RANGE start=frameBound
    | frameType=ROWS start=frameBound
    | frameType=RANGE BETWEEN start=frameBound AND end=frameBound
    | frameType=ROWS BETWEEN start=frameBound AND end=frameBound
    ;

frameBound
    : UNBOUNDED boundType=PRECEDING                 #unboundedFrame
    | UNBOUNDED boundType=FOLLOWING                 #unboundedFrame
    | CURRENT ROW                                   #currentRowBound
    | expression boundType=(PRECEDING | FOLLOWING)  #boundedFrame
    ;

explainDesc
    : (DESC | DESCRIBE | EXPLAIN) (LOGICAL | ANALYZE | VERBOSE | COSTS | SCHEDULER)?
    ;

optimizerTrace
    : TRACE (ALL | LOGS | TIMES | VALUES | REASON) identifier?
    ;

stringList
    : '(' string (',' string)* ')'
    ;

literalExpressionList
    : '(' literalExpression (',' literalExpression)* ')'
    ;

keyPartition
    : partitionColName=identifier '=' partitionColValue=literalExpression
    ;

properties
    : PROPERTIES '(' property (',' property)* ')'
    ;

propertyList
    : '(' property (',' property)* ')'
    ;

property
    : key=string '=' value=string
    ;

varType
    : GLOBAL
    | LOCAL
    | SESSION
    | VERBOSE
    ;

outfile
    : INTO OUTFILE file=string fileFormat? properties?
    ;

fileFormat
    : FORMAT AS (identifier | string)
    ;

string
    : SINGLE_QUOTED_TEXT
    | DOUBLE_QUOTED_TEXT
    ;

binary
    : BINARY_SINGLE_QUOTED_TEXT
    | BINARY_DOUBLE_QUOTED_TEXT
    ;

comparisonOperator
    : EQ | NEQ | LT | LTE | GT | GTE | EQ_FOR_NULL
    ;

booleanValue
    : TRUE | FALSE
    ;

interval
    : INTERVAL value=expression from=unitIdentifier
    ;

taskInterval
    : INTERVAL value=expression from=taskUnitIdentifier
    ;

taskUnitIdentifier
    : DAY | HOUR | MINUTE | SECOND
    ;

unitIdentifier
    : YEAR | MONTH | WEEK | DAY | HOUR | MINUTE | SECOND | QUARTER | MILLISECOND | MICROSECOND
    ;

unitBoundary
    : FLOOR | CEIL
    ;

type
    : baseType
    | decimalType
    | arrayType
    | structType
    | mapType
    ;

arrayType
    : ARRAY '<' type '>'
    ;

mapType
    : MAP '<' type ',' type '>'
    ;

subfieldDesc
    : (identifier | nestedFieldName) type
    ;

subfieldDescs
    : subfieldDesc (',' subfieldDesc)*
    ;

structType
    : STRUCT '<' subfieldDescs '>'
    ;

typeParameter
    : '(' INTEGER_VALUE ')'
    ;

baseType
    : BOOLEAN
    | TINYINT typeParameter?
    | SMALLINT typeParameter?
    | SIGNED INT?
    | SIGNED INTEGER?
    | UNSIGNED INT?
    | UNSIGNED INTEGER?
    | INT typeParameter?
    | INTEGER typeParameter?
    | BIGINT typeParameter?
    | LARGEINT typeParameter?
    | FLOAT
    | DOUBLE
    | DATE
    | DATETIME
    | TIME
    | CHAR typeParameter?
    | VARCHAR typeParameter?
    | STRING
    | TEXT
    | BITMAP
    | HLL
    | PERCENTILE
    | JSON
    | VARBINARY typeParameter?
    | BINARY typeParameter?
    ;

decimalType
    : (DECIMAL | DECIMALV2 | DECIMAL32 | DECIMAL64 | DECIMAL128 | NUMERIC | NUMBER )
        ('(' precision=INTEGER_VALUE (',' scale=INTEGER_VALUE)? ')')?
    ;

qualifiedName
    : identifier (DOT_IDENTIFIER | '.' identifier)*
    ;

tableName
    : qualifiedName
    ;

writeBranch
    : FOR? VERSION AS OF identifier
    ;

identifier
    : LETTER_IDENTIFIER      #unquotedIdentifier
    | nonReserved            #unquotedIdentifier
    | DIGIT_IDENTIFIER       #digitIdentifier
    | BACKQUOTED_IDENTIFIER  #backQuotedIdentifier
    ;

identifierWithAlias
    : originalName=identifier (AS alias=identifier)?
    ;

identifierWithAliasList
    : '(' identifierWithAlias (',' identifierWithAlias)* ')'
    ;

identifierList
    : '(' identifier (',' identifier)* ')'
    ;

identifierOrString
    : identifier
    | string
    ;

identifierOrStringList
    : identifierOrString (',' identifierOrString)*
    ;

identifierOrStringOrStar
    : ASTERISK_SYMBOL
    | identifier
    | string
    ;

user
    : identifierOrString                                     # userWithoutHost
    | identifierOrString '@' identifierOrString              # userWithHost
    | identifierOrString '@' '[' identifierOrString ']'      # userWithHostAndBlanket
    ;

assignment
    : identifier EQ expressionOrDefault
    ;

assignmentList
    : assignment (',' assignment)*
    ;

number
    : DECIMAL_VALUE  #decimalValue
    | DOUBLE_VALUE   #doubleValue
    | INTEGER_VALUE  #integerValue
    ;

nonReserved
    : ACCESS | ACTIVE | ADVISOR | AFTER | AGGREGATE | APPLY | ASYNC | AUTHORS | AVG | ADMIN | ANTI | AUTHENTICATION | AUTO_INCREMENT | AUTOMATED
    | ARRAY_AGG | ARRAY_AGG_DISTINCT | ASSERT_ROWS | AWARE
    | BACKEND | BACKENDS | BACKUP | BEGIN | BITMAP_UNION | BLACKLIST | BLACKHOLE | BINARY | BODY | BOOLEAN | BRANCH | BROKER | BUCKETS
    | BUILTIN | BASE | BEFORE | BASELINE
    | CACHE | CAST | CANCEL | CATALOG | CATALOGS | CEIL | CHAIN | CHARSET | CLEAN | CLEAR | CLUSTER | CLUSTERS | CNGROUP | CNGROUPS | CURRENT | COLLATION | COLUMNS
    | CUME_DIST | CUMULATIVE | COMMENT | COMMIT | COMMITTED | COMPUTE | CONNECTION | CONSISTENT | COSTS | COUNT
    | CONFIG | COMPACT
    | DATA | DATE | DATACACHE | DATETIME | DAY | DAYS | DECOMMISSION | DIALECT | DISABLE | DISK | DISTRIBUTION | DUPLICATE | DYNAMIC | DISTRIBUTED | DICTIONARY | DICTIONARY_GET | DEALLOCATE
    | ENABLE | END | ENGINE | ENGINES | ERRORS | EVENTS | EXECUTE | EXTERNAL | EXTRACT | EVERY | ENCLOSE | ESCAPE | EXPORT
    | FAILPOINT | FAILPOINTS | FIELDS | FILE | FILTER | FIRST | FLOOR | FOLLOWING | FORMAT | FN | FRONTEND | FRONTENDS | FOLLOWER | FREE
    | FUNCTIONS
    | GLOBAL | GRANTS | GROUP_CONCAT
    | HASH | HISTOGRAM | HELP | HLL_UNION | HOST | HOUR | HOURS | HUB
    | IDENTIFIED | IMAGE | IMPERSONATE | INACTIVE | INCREMENTAL | INDEXES | INSTALL | INTEGRATION | INTEGRATIONS | INTERMEDIATE
    | INTERVAL | ISOLATION
    | JOB
    | LABEL | LAST | LESS | LEVEL | LIST | LOCAL | LOCATION | LOGS | LOGICAL | LOW_PRIORITY | LOCK | LOCATIONS
    | MANUAL | MAP | MAPPING | MAPPINGS | MASKING | MATCH | MAPPINGS | MATERIALIZED | MAX | META | MIN | MINUTE | MINUTES | MODE | MODIFY | MONTH | MERGE | MINUS | MULTIPLE
    | NAME | NAMES | NEGATIVE | NO | NODE | NODES | NONE | NULLS | NUMBER | NUMERIC
    | OBSERVER | OF | OFFSET | ONLY | OPTIMIZER | OPEN | OPERATE | OPTION | OVERWRITE | OFF
    | PARTITIONS | PASSWORD | PATH | PAUSE | PENDING | PERCENTILE_UNION | PIVOT | PLAN | PLUGIN | PLUGINS | POLICY | POLICIES
    | PERCENT_RANK | PREDICATE | PRECEDING | PRIORITY | PROC | PROCESSLIST | PROFILE | PROFILELIST | PROVIDER | PROVIDERS | PRIVILEGES | PROBABILITY | PROPERTIES | PROPERTY | PIPE | PIPES
    | QUARTER | QUERY | QUERIES | QUEUE | QUOTA | QUALIFY
    | REASON | REMOVE | REWRITE | RANDOM | RANK | RECOVER | REFRESH | REPAIR | REPEATABLE | REPLACE_IF_NOT_NULL | REPLICA | REPOSITORY
    | REPOSITORIES
    | RESOURCE | RESOURCES | RESTORE | RESUME | RETAIN | RETENTION | RETURNS | RETRY | REVERT | ROLE | ROLES | ROLLUP | ROLLBACK | ROUTINE | ROW | RUNNING | RULE | RULES
    | SAMPLE | SCHEDULE | SCHEDULER | SECOND | SECURITY | SEPARATOR | SERIALIZABLE |SEMI | SESSION | SETS | SIGNED | SNAPSHOT | SNAPSHOTS | SQLBLACKLIST | START | STARROCKS
    | STREAM | SUM | STATUS | STOP | SKIP_HEADER | SWAP
    | STORAGE| STRING | STRUCT | STATS | SUBMIT | SUSPEND | SYNC | SYSTEM_TIME
    | TABLES | TABLET | TABLETS | TAG | TASK | TEMPORARY | TIMESTAMP | TIMESTAMPADD | TIMESTAMPDIFF | THAN | TIME | TIMES | TRANSACTION | TRACE | TRANSLATE
    | TRIM_SPACE
    | TRIGGERS | TRUNCATE | TYPE | TYPES
    | UNBOUNDED | UNCOMMITTED | UNSET | UNINSTALL | USAGE | USER | USERS | UNLOCK
    | VALUE | VARBINARY | VARIABLES | VIEW | VIEWS | VERBOSE | VERSION | VOLUME | VOLUMES
    | WARNINGS | WEEK | WHITELIST | WORK | WRITE  | WAREHOUSE | WAREHOUSES
    | YEAR
    | DOTDOTDOT | NGRAMBF | VECTOR
    | FIELD
    | ARRAY_ELEMENT
    | PERSISTENT
    | EXCLUDE | EXCEPT
    ;
