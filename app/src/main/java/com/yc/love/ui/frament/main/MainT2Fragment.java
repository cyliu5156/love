package com.yc.love.ui.frament.main;

import android.content.Intent;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.yc.love.R;
import com.yc.love.adaper.rv.MainT2MoreItemAdapter;
import com.yc.love.adaper.rv.base.RecyclerViewItemListener;
import com.yc.love.adaper.rv.holder.BaseViewHolder;
import com.yc.love.adaper.rv.holder.MainT2ToPayVipHolder;
import com.yc.love.adaper.rv.holder.ProgressBarViewHolder;
import com.yc.love.adaper.rv.holder.MainT2ViewHolder;
import com.yc.love.adaper.rv.holder.MainT2TitleViewHolder;
import com.yc.love.adaper.rv.holder.VipViewHolder;
import com.yc.love.model.base.MySubscriber;
import com.yc.love.model.bean.AResultInfo;
import com.yc.love.model.bean.ExampDataBean;
import com.yc.love.model.bean.ExampListsBean;
import com.yc.love.model.bean.ExampleTsBean;
import com.yc.love.model.bean.ExampleTsListBean;
import com.yc.love.model.bean.LoveByStagesBean;
import com.yc.love.model.bean.MainT2Bean;
import com.yc.love.model.bean.MainT3Bean;
import com.yc.love.model.engin.LoveEngin;
import com.yc.love.model.single.YcSingle;
import com.yc.love.ui.activity.BecomeVipActivity;
import com.yc.love.ui.activity.ExampleDetailActivity;
import com.yc.love.ui.activity.LoveHealDetailsActivity;
import com.yc.love.ui.frament.base.BaseMainFragment;
import com.yc.love.ui.view.LoadDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayn on 2019/4/23.
 */

public class MainT2Fragment extends BaseMainFragment {
    private List<MainT2Bean> mMainT2Beans;
    private int PAGE_SIZE = 5;
    private int PAGE_NUM = 1;
    private boolean loadMoreEnd;
    private boolean loadDataEnd;
    private boolean showProgressBar = false;
    private MainT2MoreItemAdapter mAdapter;
    //    private int num = 10;
    private ProgressBarViewHolder progressBarViewHolder;
    private RecyclerView mRecyclerView;
    private boolean mUserIsVip = false;


    @Override
    protected int setContentView() {
        return R.layout.fragment_main_t2;
    }

    private LoveEngin mLoveEngin;

    @Override
    protected void initViews() {
        mLoveEngin = new LoveEngin(mMainActivity);
        View viewBar = rootView.findViewById(R.id.main_t2_view_bar);
        mMainActivity.setStateBarHeight(viewBar, 1);
        initRecyclerView();
    }

    private void initRecyclerView() {

        mRecyclerView = rootView.findViewById(R.id.main_t2_rl);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mMainActivity);
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        //设置增加或删除条目的动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(mMainActivity,DividerItemDecoration.VERTICAL));
    }



    @Override
    protected void lazyLoad() {
        isCanLoadData();
    }

    private void isCanLoadData() {
        netData();
    }

    private void netData() {
        LoadDialog loadDialog = new LoadDialog(mMainActivity);
        loadDialog.show();
        mLoveEngin.exampLists(String.valueOf(YcSingle.getInstance().id), String.valueOf(PAGE_NUM), String.valueOf(PAGE_SIZE), "example/lists").subscribe(new MySubscriber<AResultInfo<ExampDataBean>>(loadDialog) {
            @Override
            protected void onNetNext(AResultInfo<ExampDataBean> exampDataBeanAResultInfo) {
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
                mMainT2Beans.add(new MainT2Bean("vip", 2));

                initRecyclerViewData();
            }

            @Override
            protected void onNetError(Throwable e) {

            }

            @Override
            protected void onNetCompleted() {

            }
        });
    }

    private void initRecyclerViewData() {
        mAdapter = new MainT2MoreItemAdapter(mMainT2Beans, mRecyclerView) {


            @Override
            public BaseViewHolder getTitleHolder(ViewGroup parent) {
                return new MainT2TitleViewHolder(mMainActivity, null, parent);
            }

            @Override
            public BaseViewHolder getHolder(ViewGroup parent) {
                return new MainT2ViewHolder(mMainActivity, recyclerViewItemListener, parent);
            }

            @Override
            public BaseViewHolder getToPayVipHolder(ViewGroup parent) {
                return new MainT2ToPayVipHolder(mMainActivity, recyclerViewToVipListener, parent);
            }

            @Override
            protected RecyclerView.ViewHolder getVipHolder(ViewGroup parent) {
                VipViewHolder vipViewHolder = new VipViewHolder(mMainActivity, recyclerViewToVipListener, parent);
                return vipViewHolder;
            }


            @Override
            protected RecyclerView.ViewHolder getBarViewHolder(ViewGroup parent) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_test_item_footer, parent, false);
                progressBarViewHolder = new ProgressBarViewHolder(view);
                return progressBarViewHolder;
            }


        };
        mRecyclerView.setAdapter(mAdapter);
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
                        mAdapter.notifyDataSetChanged();
                        showProgressBar = true;
                    }
                    if (!loadMoreEnd) {
                        //  判断是否是VIP
                        if (PAGE_NUM >= 3 && !mUserIsVip) {
                            addToPayVipData();
                        } else {
                            netLoadMore();
                        }
                    } else {
                        Log.d("mylog", "loadMoreData: loadMoreEnd end 已加载全部数据 ");
                        mMainT2Beans.remove(mMainT2Beans.size() - 1);
                        mAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
        loadDataEnd = true;
    }

    private void addToPayVipData() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                showProgressBar = false;
                mMainT2Beans.remove(mMainT2Beans.size() - 1);
                mAdapter.notifyDataSetChanged();
                mMainT2Beans.add(new MainT2Bean("toPayVip", 3));
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }
        }, 600);
    }

    private void netLoadMore() {

        mLoveEngin.exampLists(String.valueOf(YcSingle.getInstance().id), String.valueOf(PAGE_NUM++), String.valueOf(PAGE_SIZE), "example/lists").subscribe(new MySubscriber<AResultInfo<ExampDataBean>>(mMainActivity) {


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
                showProgressBar = false;
                mMainT2Beans.remove(mMainT2Beans.size() - 1);
                mAdapter.notifyDataSetChanged();
                if (netLoadMoreData.size() < PAGE_SIZE) {
                    loadMoreEnd = true;
                }
                mMainT2Beans.addAll(netLoadMoreData);
                mAdapter.notifyDataSetChanged();
                mAdapter.setLoaded();
            }

            @Override
            protected void onNetError(Throwable e) {

            }

            @Override
            protected void onNetCompleted() {

            }
        });
    }
    RecyclerViewItemListener recyclerViewItemListener = new RecyclerViewItemListener() {
        @Override
        public void onItemClick(int position) {
//            Toast.makeText(mMainActivity, "onItemClick " + position, Toast.LENGTH_SHORT).show();
            MainT2Bean mainT2Bean = mMainT2Beans.get(position);
            ExampleDetailActivity.startExampleDetailActivity(mMainActivity,mainT2Bean.id,mainT2Bean.post_title);
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };
    RecyclerViewItemListener recyclerViewToVipListener = new RecyclerViewItemListener() {
        @Override
        public void onItemClick(int position) {
            //TODO 购买VIP后 刷新数据
            startActivity(new Intent(mMainActivity, BecomeVipActivity.class));
        }

        @Override
        public void onItemLongClick(int position) {

        }
    };
}
