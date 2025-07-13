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

package com.starsqls.web.controller;

import com.starsqls.format.FormatOptions;
import com.starsqls.format.FormatPrinter;
import com.starsqls.parser.StarRocksLexer;
import com.starsqls.parser.StarRocksParser;
import com.starsqls.web.dto.FormatRequest;
import com.starsqls.web.dto.FormatResponse;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class FormatController {

    @PostMapping("/format")
    public ResponseEntity<FormatResponse> formatSQL(@RequestBody FormatRequest request) {
        try {
            String sql = request.getSql();
            if (sql == null || sql.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new FormatResponse(false, null, "SQL cannot be empty"));
            }

            // Use format options from request, or default if null
            FormatOptions formatOptions = request.getOptions();
            if (formatOptions == null) {
                formatOptions = FormatOptions.defaultOptions();
            } else if (formatOptions.mode == FormatOptions.Mode.MINIFY) {
                formatOptions = new FormatOptions();
            }
            // Format SQL
            FormatPrinter printer = new FormatPrinter(formatOptions);
            String formattedSQL = printer.format(sql);

            return ResponseEntity.ok(new FormatResponse(true, formattedSQL, null));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new FormatResponse(false, null, "Failed to format SQL: " + e.getMessage()));
        }
    }
}