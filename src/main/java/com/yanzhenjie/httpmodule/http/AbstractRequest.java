/*
 * Copyright © Yan Zhenjie. All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.httpmodule.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yanzhenjie.nohttp.Binary;
import com.yanzhenjie.nohttp.Headers;
import com.yanzhenjie.nohttp.RequestMethod;
import com.yanzhenjie.nohttp.rest.RestRequest;
import com.yanzhenjie.nohttp.rest.StringRequest;
import com.yanzhenjie.nohttp.tools.MultiValueMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>业务状态码用Http响应码返回。</p>
 * Created by Yan Zhenjie on 2016/12/17.
 */
public abstract class AbstractRequest<T> extends RestRequest<Result<T>> {

    public AbstractRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    @Override
    public void onPreExecute() {
        // 这个方法在子线程被调用，你可以做一些耗时操作，它不会堵塞主线程。
        // 设置Https证书、数据加密、数据签名。

        //------------ http --------------//
//        setSSLSocketFactory();
//        setHostnameVerifier();


        // 添加统一参数。
        add("token", "000000");

        // ------------- 签名 --------------//

        //第一步:获取所有请求参数
        MultiValueMap<String, Object> multiValueMap = getParamKeyValues();

        //第二步,定义List用于存储所有请求参数的key
        List<String> keyList = new ArrayList<>();

        //第三步:定义Map用于存储所有请求参数的value
        Map<String, String> keyValueMap = new HashMap<>();

        //第四步:拿到所有具体请求参数
        for (Map.Entry<String, List<Object>> paramsEntry : multiValueMap.entrySet()) {
            String key = paramsEntry.getKey();
            List<Object> values = paramsEntry.getValue();
            for (Object value : values) {

                if (value instanceof String) {
                    //第五步:将请求参数的key添加到list中用于排序
                    keyList.add(key);
                    //第六步:将请求参数的value添加到Map中
                    keyValueMap.put(key, (String) value);
                } else if (value instanceof Binary) {
                    // TODO ...
                }
            }
        }

        //第七步:对请求参数key进行排序
        Collections.sort(keyList);

        StringBuilder paramsBuilder = new StringBuilder();
        // name=value&name1=value1&name3=value3

        //第八步:依次取出排序之后的key-value,并拼接
        for (String key : keyList) {
            String value = keyValueMap.get(key);
            paramsBuilder.append(key).append("=").append(value).append("&");
        }

        String params = "";
        if (paramsBuilder.length() > 0) {
            //去掉最后一个&
            params = paramsBuilder.toString().substring(0, paramsBuilder.length() - 1);
        }

        //第九步:对拼接好的参数进行MD5加密
        String auth = md5(params);// TODO 对参数进行MD5加密。

        //最后，添加到请求头。
        addHeader("auth", auth);

        // 如果你们服务器要求添加到head，那么：
        // addHeader("token", token);
    }

    private String md5(String params) {
        return params;
    }

    @Override
    public Result<T> parseResponse(Headers headers, byte[] body) throws Exception {
        int responseCode = headers.getResponseCode();
        // 响应码正确，且包体不为空。
        if (responseCode == 200 && body != null && body.length > 0) {
            String result = StringRequest.parseResponseString(headers, body);
            try {
                T t = getResult(result);
                return new Result<>(true, t, headers, null);
            } catch (Exception e) { // 解析发生错误。
                String error = "服务器返回数据格式错误，请稍后重试";
                return new Result<>(false, null, headers, error);
            }
        } else if (responseCode >= 400) { // 其它响应码处理。
            String result = StringRequest.parseResponseString(headers, body);

            String error = "服务器发生错误，请稍后重试";
            // 错误响应码时正常解析说明是服务器返回的json数据。
            // 非正常解析说明是服务器返回的崩溃信息html等。
            try {
                JSONObject jsonObject = JSON.parseObject(result);
                error = jsonObject.getString("message");
            } catch (Exception ignored) {
            }
            return new Result<>(false, null, headers, error);
        } else {
            String error = "服务器返回数据格式错误，请稍后重试";
            return new Result<>(false, null, headers, error);
        }
    }

    protected abstract T getResult(String responseBody) throws Exception;
}
