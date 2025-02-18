/*
 * Copyright 2025 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.sql.impl.type.converter;

import com.hazelcast.core.HazelcastJsonValue;
import com.hazelcast.internal.serialization.SerializableByConvention;
import com.hazelcast.sql.impl.expression.RowValue;
import com.hazelcast.sql.impl.type.QueryDataTypeFamily;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Converter for {@link QueryDataTypeFamily#NULL NULL} type.
 * <p>
 * This converter is special. Converters operate on non-nullable types only (see
 * {@link Converter} javadoc). Therefore, no conversion to or from NULL type
 * could be performed in converters. At the same time, expressions operate on
 * nullable types, so conversions to any type from NULL type are declared as
 * allowed (see {@link NotConvertible}). But the semantics and implementation of
 * such conversions is defined by every expression itself.
 */
@SerializableByConvention
public final class NullConverter extends Converter {

    public static final NullConverter INSTANCE = new NullConverter();

    private NullConverter() {
        super(ID_NULL, QueryDataTypeFamily.NULL);
    }

    @Override
    public Class<?> getValueClass() {
        return Void.class;
    }

    @Override
    public boolean asBoolean(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public byte asTinyint(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public short asSmallint(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public int asInt(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public long asBigint(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public BigDecimal asDecimal(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public float asReal(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public double asDouble(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public String asVarchar(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public LocalDate asDate(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public LocalTime asTime(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public LocalDateTime asTimestamp(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public OffsetDateTime asTimestampWithTimezone(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public Object asObject(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public Map<?, ?> asMap(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public HazelcastJsonValue asJson(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public RowValue asRow(Object val) {
        throw new UnsupportedOperationException("must never be called");
    }

    @Override
    public Object convertToSelf(Converter converter, Object value) {
        throw new UnsupportedOperationException("must never be called");
    }
}
