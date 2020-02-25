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
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.httpmodule.R;
import com.yanzhenjie.httpmodule.entity.ProjectEntity;
import com.yanzhenjie.httpmodule.entity.UserInfoEntity;
import com.yanzhenjie.httpmodule.http.EntityListRequest;
import com.yanzhenjie.httpmodule.http.EntityRequest;
import com.yanzhenjie.httpmodule.http.Result;
import com.yanzhenjie.httpmodule.http.SimpleHttpListener;
import com.yanzhenjie.httpmodule.http.StringRequest;
import com.yanzhenjie.nohttp.Logger;
import com.yanzhenjie.nohttp.RequestMethod;

import java.util.List;

public class MainActivity extends BaseActivity {

    private static final int NOHTTP_USER_INFO = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btn_request).setOnClickListener(requestClick);
        findViewById(R.id.btn_request_entity).setOnClickListener(requestClick);
        findViewById(R.id.btn_request_entity_list).setOnClickListener(requestClick);
    }

    /**
     * 点击请求按钮的时候请求。
     */
    private View.OnClickListener requestClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.btn_request: {
                    requestString();
                    break;
                }
                case R.id.btn_request_entity: {
                    requestEntity();
                    break;
                }
                case R.id.btn_request_entity_list: {
                    requestEntityList();
                    break;
                }
            }
        }
    };

    /**
     * 请求String。
     */
    private void requestEntityList() {
        String url = "http://api.nohttp.net/restJsonArray";
        EntityListRequest<ProjectEntity> entityRequest = new EntityListRequest<>(url, RequestMethod.GET, ProjectEntity.class);
        entityRequest.add("name", "yanzhenjie");
        entityRequest.add("pwd", 123);

        request(NOHTTP_USER_INFO, entityRequest, new SimpleHttpListener<List<ProjectEntity>>() {
            @Override
            public void onSucceed(int what, Result<List<ProjectEntity>> t) {
                if (t.isSucceed()) { // 我们自己的业务状态成功。
                    List<ProjectEntity> projectEntityList = t.getResult();
                    for (ProjectEntity projectEntity : projectEntityList) {
                        Logger.i("项目名字：" + projectEntity.getName());
                        Logger.i("项目说明：" + projectEntity.getComment());
                        Logger.d("-------------------");
                    }
                } else {
                    Toast.makeText(MainActivity.this, t.getError(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 请求String。
     */
    private void requestEntity() {
        String url = "http://api.nohttp.net/restJsonObject";
        EntityRequest<UserInfoEntity> entityRequest = new EntityRequest<>(url, RequestMethod.GET, UserInfoEntity.class);
        entityRequest.add("name", "yanzhenjie");
        entityRequest.add("pwd", 1234);

        request(NOHTTP_USER_INFO, entityRequest, new SimpleHttpListener<UserInfoEntity>() {
            @Override
            public void onSucceed(int what, Result<UserInfoEntity> t) {// Http层的成功。
                if (t.isSucceed()) { // 我们自己的业务状态成功。
                    UserInfoEntity userInfoEntity = t.getResult();
                    Logger.i("名字：" + userInfoEntity.getName());
                    Logger.i("博客：" + userInfoEntity.getBlog());
                    Logger.i("网站：" + userInfoEntity.getUrl());

                    Logger.d("-------------------");
                    List<ProjectEntity> projectEntityList = userInfoEntity.getProjectList();
                    for (ProjectEntity projectEntity : projectEntityList) {
                        Logger.i("项目名字：" + projectEntity.getName());
                        Logger.i("项目说明：" + projectEntity.getComment());
                        Logger.d("-------------------");
                    }
                } else {
                    Toast.makeText(MainActivity.this, t.getError(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * 请求String。
     */
    private void requestString() {
        String url = "http://api.nohttp.net/restJsonArray";
        StringRequest request = new StringRequest(url, RequestMethod.GET);
        request.add("name", "yanzhenjie");
        request.add("pwd", 123);

        request(NOHTTP_USER_INFO, request, httpListener);
    }

    /**
     * 处理String结果。
     *
     * @param result
     */
    private void result(Result<String> result) {
        if (result.isSucceed()) {
            String jsonString = result.getResult();

            Logger.i("请求成功：" + jsonString);
        } else {
            Toast.makeText(this, result.getError(), Toast.LENGTH_LONG).show();
        }
    }

    private SimpleHttpListener<String> httpListener = new SimpleHttpListener<String>() {
        @Override
        public void onSucceed(int what, Result<String> t) {
            switch (what) {
                case NOHTTP_USER_INFO: {
                    result(t);
                    break;
                }
            }
        }
    };

}
