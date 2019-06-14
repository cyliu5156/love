package com.yc.love.ui.activity;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.yc.love.R;
import com.yc.love.adaper.rv.MainT2MoreItemAdapter;
import com.yc.love.adaper.rv.base.RecyclerViewItemListener;
import com.yc.love.adaper.rv.holder.BaseViewHolder;
import com.yc.love.adaper.rv.holder.CaseTitleViewHolder;
import com.yc.love.adaper.rv.holder.EndEmptyViewHolder;
import com.yc.love.adaper.rv.holder.MainT2ToPayVipHolder;
import com.yc.love.adaper.rv.holder.MainT2ViewHolder;
import com.yc.love.adaper.rv.holder.ProgressBarViewHolder;
import com.yc.love.adaper.rv.holder.VipViewHolder;
import com.yc.love.cache.CacheWorker;
import com.yc.love.model.base.MySubscriber;
import com.yc.love.model.bean.AResultInfo;
import com.yc.love.model.bean.ExampDataBean;
import com.yc.love.model.bean.ExampListsBean;
import com.yc.love.model.bean.MainT2Bean;
import com.yc.love.model.engin.LoveEngin;
import com.yc.love.model.single.YcSingle;
import com.yc.love.ui.activity.base.BaseSameActivity;

import java.util.ArrayList;
import java.util.List;

public class LoveCaseActivity extends BaseSameActivity {


    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;
    private CacheWorker mCacheWorker;
    private LoveEngin mLoveEngin;
    private boolean mIsHandRefresh;
    private List<MainT2Bean> mMainT2Beans;

    private int PAGE_SIZE = 10;
    private int PAGE_NUM = 1;
    private boolean mIsAddToPayVipItem = false;
    private boolean loadMoreEnd;
    private MainT2MoreItemAdapter mAdapter;
    private boolean mIsNetData = false;
    private boolean mUserIsVip = false;
    private boolean loadDataEnd;
    private boolean showProgressBar = false;
    private int mRvLastPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_love_case);

        mLoveEngin = new LoveEngin(this);
        mCacheWorker = new CacheWorker();
        initViews();
        netData();

    }

    private void initViews() {
        ImageView ivToWx = findViewById(R.id.love_case_iv_to_wx);
        ivToWx.setOnClickListener(this);
        initRecyclerView();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        switch (v.getId()) {
            case R.id.love_case_iv_to_wx:
                showToWxServiceDialog();
                break;
        }
    }

    private void initRecyclerView() {
        mSwipeRefresh = findViewById(R.id.love_case_swipe_refresh);
        mRecyclerView = findViewById(R.id.love_case_rl);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
//        RecyclerViewNoBugLinearLayoutManager layoutManager = new RecyclerViewNoBugLinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置增加或删除条目的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mSwipeRefresh.setColorSchemeResources(R.color.red_crimson);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mIsHandRefresh = true;
                mIsAddToPayVipItem = false;
                loadMoreEnd = false;
                PAGE_NUM = 1;
                netData();
            }
        });
    }

    private void netData() {
        if (!mIsHandRefresh) {
            mMainT2Beans = (List<MainT2Bean>) mCacheWorker.getCache(this, "main2_example_lists");
            if (mMainT2Beans != null && mMainT2Beans.size() != 0) {
                initRecyclerViewData();
            } else {
                mLoadingDialog.showLoadingDialog();
            }
        }
        mLoveEngin.exampLists(String.valueOf(YcSingle.getInstance().id), String.valueOf(PAGE_NUM), String.valueOf(PAGE_SIZE), "example/lists")
                .subscribe(new MySubscriber<AResultInfo<ExampDataBean>>(mLoadingDialog) {
                    @Override
                    protected void onNetNext(AResultInfo<ExampDataBean> exampDataBeanAResultInfo) {
                        mIsNetData = true;
                        ExampDataBean exampDataBean = exampDataBeanAResultInfo.data;
                        int isVip = exampDataBean.is_vip;
                        if (isVip > 0) {
                            mUserIsVip = true;
                        }
                        List<ExampListsBean> exampListsBeans = exampDataBean.lists;
                        mMainT2Beans = new ArrayList<>();
                        mMainT2Beans.add(new MainT2Bean("tit", 0));
                        if (exampListsBeans != null && exampListsBeans.size() != 0) {
                            for (int i = 0; i < exampListsBeans.size(); i++) {
                                ExampListsBean exampListsBean = exampListsBeans.get(i);
                                mMainT2Beans.add(new MainT2Bean(1, exampListsBean.create_time, exampListsBean.id, exampListsBean.image, exampListsBean.post_title));
                            }
                        }
                        if (!mUserIsVip && mMainT2Beans != null && mMainT2Beans.size() > 6) {
                            mMainT2Beans.add(6, new MainT2Bean("vip", 2));
                            mMainT2Beans.add(new MainT2Bean("toPayVip", 3, mMainT2Beans.size()));
                        }


                        mCacheWorker.setCache("main2_example_lists", mMainT2Beans);
                        initRecyclerViewData();
                    }

                    @Override
                    protected void onNetError(Throwable e) {
                        mSwipeRefresh.setRefreshing(false);
                    }

                    @Override
                    protected void onNetCompleted() {
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
    }

    private void initRecyclerViewData() {
        mAdapter = new MainT2MoreItemAdapter(mMainT2Beans, mRecyclerView) {
            @Override
            public BaseViewHolder getTitleHolder(ViewGroup parent) {
                return new CaseTitleViewHolder(LoveCaseActivity.this, null, parent);
            }

            @Override
            public BaseViewHolder getHolder(ViewGroup parent) {
                return new MainT2ViewHolder(LoveCaseActivity.this, recyclerViewItemListener, parent);
            }

            @Override
            public BaseViewHolder getToPayVipHolder(ViewGroup parent) {
                return new MainT2ToPayVipHolder(LoveCaseActivity.this, recyclerViewToVipListener, parent);
            }

            @Override
            protected RecyclerView.ViewHolder getVipHolder(ViewGroup parent) {
                VipViewHolder vipViewHolder = new VipViewHolder(LoveCaseActivity.this, recyclerViewToVipListener, parent);
                return vipViewHolder;
            }

            @Override
            protected RecyclerView.ViewHolder getEndHolder(ViewGroup parent) {
                return new EndEmptyViewHolder(LoveCaseActivity.this, null, parent);
            }


            @Override
            protected RecyclerView.ViewHolder getBarViewHolder(ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_test_item_footer, parent, false);
                ProgressBarViewHolder progressBarViewHolder = new ProgressBarViewHolder(view);
                return progressBarViewHolder;
            }


        };
        mRecyclerView.setAdapter(mAdapter);
        if (!mUserIsVip) {
            return;
        }
        if (mMainT2Beans.size() < PAGE_SIZE) {
            Log.d("ssss", "loadMoreData: data---end");
        } else {
            mAdapter.setOnMoreDataLoadListener(new MainT2MoreItemAdapter.OnLoadMoreDataListener() {
                @Override
                public void loadMoreData() {
                    if (loadDataEnd == false) {
                        return;
                    }
                    if (showProgressBar == false) {
                        //加入null值此时adapter会判断item的type
                        mMainT2Beans.add(null);
//                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyItemChanged(mMainT2Beans.size() - 1);
                        showProgressBar = true;
                    }
                    if (!loadMoreEnd) {
                        netLoadMore();
                        //  判断是否是VIP
                       /* if (PAGE_NUM >= 3 && !mUserIsVip) {
                            addToPayVipData();
                        } else {
                            netLoadMore();
                        }*/
                    } else {
                        Log.d("mylog", "loadMoreData: loadMoreEnd end 已加载全部数据 ");
                        mMainT2Beans.remove(mMainT2Beans.size() - 1);
//                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyItemChanged(mMainT2Beans.size() - 1);
                    }
                }
            });
        }
        loadDataEnd = true;
    }

    private void netLoadMore() {
        mLoveEngin.exampLists(String.valueOf(YcSingle.getInstance().id), String.valueOf(++PAGE_NUM), String.valueOf(PAGE_SIZE), "example/lists").subscribe(new MySubscriber<AResultInfo<ExampDataBean>>(this) {
            @Override
            protected void onNetNext(AResultInfo<ExampDataBean> exampDataBeanAResultInfo) {
                ExampDataBean exampDataBean = exampDataBeanAResultInfo.data;
                List<ExampListsBean> exampListsBeans = exampDataBean.lists;
                List<MainT2Bean> netLoadMoreData = new ArrayList<>();
                if (exampListsBeans != null && exampListsBeans.size() > 0) {
                    for (int i = 0; i < exampListsBeans.size(); i++) {
                        ExampListsBean exampListsBean = exampListsBeans.get(i);
                        netLoadMoreData.add(new MainT2Bean(1, exampListsBean.create_time, exampListsBean.id, exampListsBean.image, exampListsBean.post_title));
                    }
                }
                changLoadMoreView(netLoadMoreData);
            }

            @Override
            protected void onNetError(Throwable e) {
                changLoadMoreView(null);
            }

            @Override
            protected void onNetCompleted() {

            }
        });
    }

    private void changLoadMoreView(List<MainT2Bean> netLoadMoreData) {
        int rvLastPosition = mMainT2Beans.size() - 1;
        MainT2Bean mainT2Bean = mMainT2Beans.get(rvLastPosition);
//        if (mainT2Bean == null && rvLastPosition != mRvLastPosition) {
        if (mainT2Bean == null) {
            showProgressBar = false;
            this.mRvLastPosition = rvLastPosition;
            Log.d("mylog", "changLoadMoreView: mRvLastPosition " + mRvLastPosition);

//            mAdapter.notifyDataSetChanged();
//            Log.d("mylog", "changLoadMoreView: mMainT2Beans.size() "+mMainT2Beans.size());
//            mAdapter.notifyItemChanged(mMainT2Beans.size()-1);

            mMainT2Beans.remove(mMainT2Beans.size() - 1);

            Log.d("mylog", "changLoadMoreView: mMainT2Beans.size() " + mMainT2Beans.size());
            mAdapter.notifyDataSetChanged();
            int itemCount = mAdapter.getItemCount();
            Log.d("mylog", "changLoadMoreView: itemCount " + itemCount);
//            mAdapter.notifyItemChanged(itemCount - 1);

        }
        if (netLoadMoreData != null && netLoadMoreData.size() != 0) {
            mMainT2Beans.addAll(netLoadMoreData);
//            mAdapter.notifyDataSetChanged();
            mAdapter.notifyItemChanged(mMainT2Beans.size() - 1);
        } else {
            dataEmptyCheck();
        }
        mAdapter.setLoaded();
    }

    private void dataEmptyCheck() {
        if (PAGE_NUM != 1) {  //如果购买成功，依然请求这一页
            PAGE_NUM--;
        }
        if (PAGE_NUM <= 2) {
            if (!mIsAddToPayVipItem) {
                addToPayVipData(); //数据为空 不是VIP
            }
        }
    }

    private void addToPayVipData() {
        if (mUserIsVip) {  //已经是VIP 真的没有数据了
            return;
        }
        /*if (mIsAddToPayVipItem) {  //只添加一条即可
            return;
        }
        mIsAddToPayVipItem = true;*/
        mIsAddToPayVipItem = true;
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMainT2Beans.add(new MainT2Bean("toPayVip", 3, mMainT2Beans.size()));
                mAdapter.notifyItemChanged(mMainT2Beans.size() - 1);
//                mAdapter.notifyDataSetChanged();
//                mRecyclerView.scrollToPosition(mMainT2Beans.size() - 1);
            }
        }, 600);
    }


    RecyclerViewItemListener recyclerViewItemListener = new RecyclerViewItemListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 0) {
                return;
            }
//            Toast.makeText(mMainActivity, "onItemClick " + position, Toast.LENGTH_SHORT).show();
            MainT2Bean mainT2Bean = mMainT2Beans.get(position);
            ExampleDetailActivity.startExampleDetailActivity(LoveCaseActivity.this, mainT2Bean.id, mainT2Bean.post_title);
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };
    RecyclerViewItemListener recyclerViewToVipListener = new RecyclerViewItemListener() {
        @Override
        public void onItemClick(int position) {
            if (position < 0) {
                return;
            }
            startActivity(new Intent(LoveCaseActivity.this, BecomeVipActivity.class));
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };


    @Override
    protected String offerActivityTitle() {
        return "实战学习";
    }
}
