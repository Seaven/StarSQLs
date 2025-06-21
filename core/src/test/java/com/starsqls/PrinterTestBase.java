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
package com.starsqls;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class PrinterTestBase {
    public static String read(String file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder s = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                s.append(line).append("\n");
            }
            return s.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String sql(String file) {
        String path = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("case")).getPath();
        return read(path + "/" + file);
    }

    public static String result(String file) {
        String path = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("result")).getPath();
        return read(path + "/" + file);
    }

    public static void saveResult(String file, String content) {
        String path = Objects.requireNonNull(ClassLoader.getSystemClassLoader().getResource("result")).getPath();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + "/" + file))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
