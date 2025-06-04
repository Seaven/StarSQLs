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

import com.nicesql.sql.format.FormatOptions;
import com.nicesql.sql.format.FormatPrinter;
import org.junit.jupiter.api.Test;

public class PrettyPrintTest extends PrinterTestBase {

    private FormatOptions options = new FormatOptions();

    @Test
    public void test() {
        FormatPrinter printer = new FormatPrinter(options);
        String sql = sql("tpch/q1.sql");
        System.out.println(sql);
        String result = printer.format(sql);

        System.out.println(result);
    }

}
