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
 * Enumeration of DAG edge types representing different data flow relationships.
 */
public enum EdgeType {
    /**
     * Main data flow edge (default)
     */
    DATAFLOW,
    
    /**
     * CTE reference edge (dashed line)
     */
    CTE_REFERENCE,
    
    /**
     * Subquery reference edge
     */
    SUBQUERY_REFERENCE,
    
    /**
     * Dependency edge (generic)
     */
    DEPENDENCY
}
