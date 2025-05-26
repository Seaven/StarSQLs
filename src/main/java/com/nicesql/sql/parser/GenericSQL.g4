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

grammar GenericSQL;
import GenericLex;

sqlStatements
    : statement+ EOF
    ;

statement
    // Query Statement
    : (queryStatement (SEMICOLON | EOF)) | emptyStatement
    ;

emptyStatement
    : SEMICOLON
    ;

subfieldName
    : identifier | ARRAY_ELEMENT
    ;

nestedFieldName
    : subfieldName (DOT_IDENTIFIER | '.' subfieldName)*
    ;

// ------------------------------------------- Query Statement ---------------------------------------------------------

queryStatement
    : (explainDesc) ? queryRelation;

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
    : relationPrimary joinRelation*                                                     #nonBracketsRelation
    | '(' relationPrimary joinRelation* ')'                                             #bracketsRelation
    ;

relationPrimary
    : qualifiedName queryPeriod? partitionNames? tabletList? replicaList?
        (AS? alias=identifier)? bracketHint? (BEFORE ts=string)?                        #tableAtom
    | '(' VALUES rowConstructor (',' rowConstructor)* ')'
        (AS? alias=identifier columnAliases?)?                                          #inlineTable
    | ASSERT_ROWS? subquery (AS? alias=identifier columnAliases?)?                      #subqueryWithAlias
    | qualifiedName '(' expressionList ')'
        (AS? alias=identifier columnAliases?)?                                          #tableFunction
    | TABLE '(' qualifiedName '(' argumentList ')' ')'
        (AS? alias=identifier columnAliases?)?                                          #normalizedTableFunction
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

mapExpressionList
    : mapExpression (',' mapExpression)*
    ;

mapExpression
    : key=expression ':' value=expression
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
    | NOT? op=(LIKE | RLIKE | REGEXP) pattern=valueExpression                             #like
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
    | informationFunctionExpression                                                       #informationFunction
    | specialDateTimeExpression                                                           #specialDateTime
    | specialFunctionExpression                                                           #specialFunction
    | aggregationFunction over?                                                           #aggregationFunctionCall
    | windowFunction over                                                                 #windowFunctionCall
    | TRANSLATE '(' (expression (',' expression)*)? ')'                                   #translateFunctionCall
    | qualifiedName '(' (expression (',' expression)*)? ')'  over?                        #simpleFunctionCall
    ;

aggregationFunction
    : name = AVG '(' setQuantifier? expression ')'
    | name = COUNT '(' ASTERISK_SYMBOL? ')'
    | name = COUNT '(' (setQuantifier bracketHint?)? (expression (',' expression)*)? ')'
    | name = MAX '(' setQuantifier? expression ')'
    | name = MIN '(' setQuantifier? expression ')'
    | name = SUM '(' setQuantifier? expression ')'
    | name = ARRAY_AGG '(' setQuantifier? expression (ORDER BY sortItem (',' sortItem)*)? ')'
    | name = ARRAY_AGG_DISTINCT '(' expression (ORDER BY sortItem (',' sortItem)*)? ')'
    | name = GROUP_CONCAT '(' setQuantifier? expression (',' expression)* (ORDER BY sortItem (',' sortItem)*)? (SEPARATOR expression)? ')'
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
    : name = TIMESTAMPADD '(' unitIdentifier ',' expression ',' expression ')'
    | name = TIMESTAMPDIFF '(' unitIdentifier ',' expression ',' expression ')'
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
    : EXPLAIN level = (LOGICAL | ANALYZE | VERBOSE | COSTS)?
    ;

literalExpressionList
    : '(' literalExpression (',' literalExpression)* ')'
    ;

keyPartition
    : partitionColName=identifier '=' partitionColValue=literalExpression
    ;

varType
    : GLOBAL
    | LOCAL
    | SESSION
    | VERBOSE
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

identifier
    : LETTER_IDENTIFIER      #unquotedIdentifier
    | nonReserved            #unquotedIdentifier
    | DIGIT_IDENTIFIER       #digitIdentifier
    | BACKQUOTED_IDENTIFIER  #backQuotedIdentifier
    ;

identifierList
    : '(' identifier (',' identifier)* ')'
    ;

identifierOrString
    : identifier
    | string
    ;

number
    : DECIMAL_VALUE  #decimalValue
    | DOUBLE_VALUE   #doubleValue
    | INTEGER_VALUE  #integerValue
    ;

nonReserved
    : AVG | ANTI | ARRAY_ELEMENT | ARRAY_AGG | ARRAY_AGG_DISTINCT | ASSERT_ROWS | AWARE
    | BINARY | BOOLEAN | BEFORE
    | CAST | CATALOG | CEIL | CURRENT | CUME_DIST | COSTS | COUNT
    | DATE | DATETIME | DAY | DOTDOTDOT
    | END | EXTRACT | EXCLUDE | EXCEPT
    | FIRST | FLOOR | FOLLOWING | FN
    | GLOBAL | GROUP_CONCAT
    | HLL_UNION | HOUR | HOURS
    | INTERVAL
    | LAST | LOCAL | LOGICAL
    | MAP | MATCH | MAX | MIN | MINUTE | MONTH | MINUS
    | NULLS | NUMBER | NUMERIC
    | OF | OFFSET
    | PARTITIONS | PASSWORD | PIVOT | PERCENT_RANK
    | QUARTER | QUALIFY
    | RANK | REPLICA | ROLLUP | ROW
    | SECOND | SEPARATOR |SEMI | SESSION | SETS | SIGNED | SUM | STRING | STRUCT | SYSTEM_TIME
    | TABLET | TEMPORARY | TIMESTAMP | TIMESTAMPADD | TIMESTAMPDIFF | TIME | TRANSLATE
    | UNBOUNDED | USER
    | VARBINARY | VERBOSE | VERSION
    | WEEK
    | YEAR
    ;
