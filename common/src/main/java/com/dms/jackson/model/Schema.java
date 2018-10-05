package com.dms.jackson.model;

import javax.xml.bind.ValidationException;

/**
 * Created by VolodymyrD on 12/19/17.
 */
public interface Schema {
    boolean validate() throws ValidationException;
}
