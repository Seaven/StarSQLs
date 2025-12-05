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

package com.starsqls.dag.model;

/**
 * Enumeration of DAG node types representing different SQL operations.
 */
public enum NodeType {
    /**
     * Table scan operation (FROM table)
     */
    SCAN,
    
    /**
     * Column projection (SELECT columns)
     */
    PROJECT,
    
    /**
     * Filter operation (WHERE/HAVING clause)
     */
    FILTER,
    
    /**
     * Join operation (INNER/LEFT/RIGHT/FULL/CROSS JOIN)
     */
    JOIN,
    
    /**
     * Aggregation operation (GROUP BY + aggregate functions)
     */
    AGGREGATE,
    
    /**
     * Sort operation (ORDER BY)
     */
    SORT,
    
    /**
     * Limit operation (LIMIT/OFFSET)
     */
    LIMIT,

    /**
     * Top N operation (TOP N)
     */
    TOP_N,

    /**
     * Distinct operation (SELECT DISTINCT)
     */
    DISTINCT,
    
    /**
     * Union operation (UNION/UNION ALL)
     */
    UNION,
    
    /**
     * Subquery boundary marker
     */
    SUBQUERY,
    
    /**
     * Common Table Expression (WITH clause)
     */
    CTE,
    
    /**
     * Window function operation (OVER clause)
     */
    WINDOW,
    
    /**
     * Final query result
     */
    RESULT
}
