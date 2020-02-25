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
package com.yanzhenjie.httpmodule.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.yanzhenjie.httpmodule.http.AbstractRequest;
import com.yanzhenjie.httpmodule.http.DefaultResponseListener;
import com.yanzhenjie.httpmodule.http.HttpListener;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.rest.RequestQueue;

/**
 * <p>BaseActivity，封装请求。</p>
 * Created by Yan Zhenjie on 2016/12/17.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * 请求队列。
     */
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = NoHttp.newRequestQueue(5);
    }

    private Object sign = new Object();

    /**
     * 发起请求。
     *
     * @param request      请求对象。
     * @param httpListener 接受请求结果。
     * @param <T>          请求数据类型。
     */
    protected <T> void request(AbstractRequest<T> request, HttpListener<T> httpListener) {
        request.setCancelSign(sign);
        requestQueue.add(0, request, new DefaultResponseListener<>(httpListener, request));
    }

    /**
     * 发起请求。
     *
     * @param what         如果多个被同一个Listener接受结果，那么使用what区分结果。
     * @param request      请求对象。
     * @param httpListener 接受请求结果。
     * @param <T>          请求数据类型。
     */
    protected <T> void request(int what, AbstractRequest<T> request, HttpListener<T> httpListener) {
        request.setCancelSign(sign);
        requestQueue.add(what, request, new DefaultResponseListener<>(httpListener, request));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        requestQueue.cancelBySign(sign);
        requestQueue.stop();
    }

}
