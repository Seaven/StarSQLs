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

package com.starsqls.web.dto;

public class FormatResponse {
    private boolean success;
    private String formattedSQL;
    private String error;

    public FormatResponse() {
    }

    public FormatResponse(boolean success, String formattedSQL) {
        this.success = success;
        this.formattedSQL = formattedSQL;
    }

    public FormatResponse(boolean success, String formattedSQL, String error) {
        this.success = success;
        this.formattedSQL = formattedSQL;
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getFormattedSQL() {
        return formattedSQL;
    }

    public void setFormattedSQL(String formattedSQL) {
        this.formattedSQL = formattedSQL;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
} 