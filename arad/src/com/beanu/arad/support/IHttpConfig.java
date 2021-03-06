package com.beanu.arad.support;

import com.beanu.arad.error.AradException;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * Created by beanu on 13-11-29.
 */
public interface IHttpConfig<T> {

    /**
     * 根据结果 初步判断正确与否。如果是000则正确，返回T结果。如果不是001则错误，抛出异常
     *
     * @param result
     * @return
     * @throws com.beanu.arad.error.AradException
     */

    public abstract T handleResult(String result) throws AradException;

}
