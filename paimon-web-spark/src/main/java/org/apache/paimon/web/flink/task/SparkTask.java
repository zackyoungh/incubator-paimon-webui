/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.web.flink.task;

import org.apache.paimon.web.common.data.constant.SqlConstants;
import org.apache.paimon.web.common.data.vo.SubmitResult;
import org.apache.paimon.web.task.SubmitJob;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructField;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
public class SparkTask implements SubmitJob {
    private final SparkSession sparkSession;

    @Override
    public SubmitResult execute(String statement) throws Exception {
        List<Row> rows = sparkSession.sql(statement).collectAsList();
        List<Map<String, Object>> result = new ArrayList<>();
        rows.stream()
                .forEach(
                        row -> {
                            Map<String, Object> map = new LinkedHashMap<>();
                            row.schema()
                                    .foreach(
                                            (StructField field) -> {
                                                map.put(
                                                        field.name(),
                                                        row.getAs(field.name()).toString());
                                                return field;
                                            });
                            result.add(map);
                        });
        return SubmitResult.builder().data(result).build();
    }

    @Override
    public boolean stop(String statement) throws Exception {
        // todo Need to be implemented here
        return false;
    }

    @Override
    public boolean checkStatus() {
        try {
            sparkSession.sql(SqlConstants.VALIDATE_SQL);
            return true;
        } catch (Exception e) {
            log.error("SparkTask checkStatus error", e);
            return false;
        }
    }
}