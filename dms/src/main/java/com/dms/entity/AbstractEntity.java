package com.dms.entity;

import com.datastax.driver.core.Row;

/**
 * Created by volodymyr on 15.12.17.
 */
public interface AbstractEntity {


    String saveQuery();

    String readByIdQuery();

    String selectQuery();

    AbstractEntity withRow(Row row);
}
