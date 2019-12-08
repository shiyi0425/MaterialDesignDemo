package com.example.materialdesigndemo.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.example.materialdesigndemo.model.News;
import com.example.materialdesigndemo.adapter.NewsAdapter;
import com.example.materialdesigndemo.R;
import com.example.materialdesigndemo.activity.NewsDetailActivity;
import com.example.materialdesigndemo.utils.HttpsUtil;
import com.google.android.material.snackbar.Snackbar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String NEWS_URL = "http://v.juhe.cn/toutiao/index";
    private static final int GET_NEWS = 1;
    private static final int GET_NEWS_ERROR = 2;

    private NewsHandler handler;
    private List<News> newsList;
    private NewsAdapter adapter;

    private RecyclerView rvNews;
    private SwipeRefreshLayout refreshLayout;

    public NewsFragment() {
        // Required empty public constructor
    }

    public static NewsFragment newInstance() {
        return new NewsFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        handler = new NewsHandler(this);
        rvNews = view.findViewById(R.id.rv_news);
        refreshLayout = view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);

        initData();
        return view;
    }

    private void initData() {
        String appKey = "98df2053b10c78798c78ff04917034a9";
        String url = NEWS_URL + "?key=" + appKey + "&type=top";
        Request request = new Request.Builder().url(url).build();
        HttpsUtil.getInstance().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Log.e("NewsFragment", e.getMessage());
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    JSONObject obj = JSON.parseObject(json);
                    JSONObject result = obj.getJSONObject("result");
                    if (result != null) {
                        JSONArray data = result.getJSONArray("data");

                        Message msg = handler.obtainMessage();
                        msg.what = GET_NEWS;
                        msg.obj = data.toJSONString();
                        handler.sendMessage(msg);
                    } else {
                        Message msg = handler.obtainMessage();
                        msg.what = GET_NEWS_ERROR;
                        msg.obj = obj.getString("reason");
                        handler.sendMessage(msg);
                    }
                } else {
                    Log.e("NewsFragment", response.message());
                }
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
        }, 3000);
    }

    static class NewsHandler extends Handler {
        private WeakReference<Fragment> ref;

        public NewsHandler(Fragment fragment) {
            this.ref = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            final NewsFragment fragment = (NewsFragment) this.ref.get();
            if (msg.what == GET_NEWS) {
                String json = (String) msg.obj;
                fragment.newsList = JSON.parseArray(json, News.class);
                fragment.adapter = new NewsAdapter(fragment.newsList);
                fragment.rvNews.setAdapter(fragment.adapter);

                DividerItemDecoration decoration = new DividerItemDecoration(fragment.getContext(), DividerItemDecoration.VERTICAL);
                fragment.rvNews.addItemDecoration(decoration);
                fragment.rvNews.setLayoutManager(new LinearLayoutManager(fragment.getContext()));

                fragment.adapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(fragment.getContext(), NewsDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("news", fragment.newsList.get(position));
                        intent.putExtras(bundle);
                        fragment.getContext().startActivity(intent);
                    }
                });
            } else if (msg.what == GET_NEWS_ERROR) {
                String reason = (String) msg.obj;
                if(fragment.getView() != null) {
                    Snackbar.make(fragment.getView(), reason, Snackbar.LENGTH_LONG).show();
                }
            }
        }
    }

}