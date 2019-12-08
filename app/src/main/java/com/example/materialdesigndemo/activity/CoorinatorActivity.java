package com.example.materialdesigndemo.activity;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.materialdesigndemo.R;
import com.example.materialdesigndemo.adapter.NewsAdapter;
import com.example.materialdesigndemo.model.News;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;

public class CoorinatorActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener  {
    private static final String NEWS_URL = "http://v.juhe.cn/toutiao/index";
    private static final int GET_NEWS = 1;

    private NewsHandler handler;
    private List<News> newsList;
    private NewsAdapter adapter;

    private Toolbar toolbar;
    private RecyclerView rvNews;
    private SwipeRefreshLayout refreshLayout;
    private CollapsingToolbarLayout collapsingToolbar;
    private AppBarLayout appBarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coorinator_new);

        handler = new NewsHandler(this);

        initView();
        initData();
    }

    private void initData() {
        String appKey = "98df2053b10c78798c78ff04917034a9";
        String url = NEWS_URL + "?key=" + appKey + "&type=top";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10,TimeUnit.SECONDS)
                .readTimeout(10,TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("NewsActivity",e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String json = response.body().string();
                    JSONObject obj = JSON.parseObject(json);
                    JSONObject result = obj.getJSONObject("result");
                    if(result!=null){
                        JSONArray data = result.getJSONArray("data");

                        Message msg = handler.obtainMessage();
                        msg.what = GET_NEWS;
                        msg.obj = data.toJSONString();
                        handler.sendMessage(msg);
                    }

                }

            }
        });
    }

    private void initView() {
        // 1. 初始化Toolbar
        toolbar = findViewById(R.id.tool_bar);
        collapsingToolbar = findViewById(R.id.collapsing_toolbar);

        collapsingToolbar.setTitle("新闻头条");
        setSupportActionBar(toolbar);

        //2.初始化RecyclerView列表和自动刷新布局
        rvNews = findViewById(R.id.rv_news);
        refreshLayout = findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);

        //3.解决SwipeRefreshLayout的下拉刷新与CoordinatorLayout的冲突
        rvNews.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int topRowPos = (rvNews == null || rvNews.getChildCount() == 0)?0:
                        rvNews.getChildAt(0).getTop();
                refreshLayout.setEnabled(topRowPos >= 0);
            }
        });

        //监听AppBarLayout的位置变化，当位置i>0时可刷新
        appBarLayout = findViewById(R.id.app_bar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
                refreshLayout.setEnabled(i>=0);
            }
        });
    }



    @Override
    public void onRefresh() {
        initData();
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.setRefreshing(false);
            }
        },3000);

    }

    static class NewsHandler extends Handler{
        private WeakReference<Activity> ref;

        public NewsHandler(Activity activity){
            this.ref = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final CoorinatorActivity activity = (CoorinatorActivity) this.ref.get();
            if(msg.what==GET_NEWS){
                String json = (String) msg.obj;
                activity.newsList = JSON.parseArray(json,News.class);

                DividerItemDecoration decoration = new DividerItemDecoration(activity,DividerItemDecoration.VERTICAL);
                activity.rvNews.addItemDecoration(decoration);
                activity.rvNews.setLayoutManager(new LinearLayoutManager(activity));

                activity.adapter = new NewsAdapter(activity.newsList);
                activity.rvNews.setAdapter(activity.adapter);
                activity.adapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(activity,NewsDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("news",activity.newsList.get(position));
                        intent.putExtras(bundle);
                        activity.startActivity(intent);
                    }
                });
            }
        }
    }

    public void sendEmail(View view){
        Snackbar.make(view, "正在发送邮件。。。", Snackbar.LENGTH_LONG).show();
    }
}


