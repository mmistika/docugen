/*
 * Docugen — Document Generation & Management Platform
 * Copyright (C) 2026 Artem Bilous
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.artembilous.docugen.dialect;

import org.hibernate.dialect.H2Dialect;
import org.hibernate.type.SqlTypes;

public class TestH2Dialect extends H2Dialect {
    @Override
    public String columnType(int sqlTypeCode) {
        if (sqlTypeCode == SqlTypes.NAMED_ENUM) {
            return "varchar(255)";
        }
        return super.columnType(sqlTypeCode);
    }
}
