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
package com.yanzhenjie.httpmodule;

import android.app.Application;

import com.yanzhenjie.nohttp.OkHttpNetworkExecutor;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.NoHttp;
import com.yanzhenjie.nohttp.cache.DBCacheStore;

/**
 * Created by Yan Zhenjie on 2016/12/17.
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Logger.setDebug(true);
        Logger.setTag("HttpModule");

        // 使用默认配置。
        // NoHttp.initialize(this);

        NoHttp.initialize(this, new NoHttp.Config()
                .setConnectTimeout(1000 * 20)
                .setReadTimeout(1000 * 30)
                .setCacheStore(new DBCacheStore(this))
                // .setNetworkExecutor(new URLConnectionNetworkExecutor())
                .setNetworkExecutor(new OkHttpNetworkExecutor())
        );
    }
}
