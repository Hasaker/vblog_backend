package com.hasaker.common.enums;

import com.hasaker.common.base.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 余天堂
 * @since 2019/11/17 20:08
 * @description 
 */
@Getter
@AllArgsConstructor
public enum CommonYNEnums implements IEnum<CommonYNEnums> {

    YES("1", "Y"),
    NO("0", "N");

    private final String code;
    private final String info;
}