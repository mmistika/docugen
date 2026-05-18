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
