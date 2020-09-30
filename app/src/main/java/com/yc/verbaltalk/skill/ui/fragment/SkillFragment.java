package com.yc.verbaltalk.skill.ui.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.yc.verbaltalk.R;
import com.yc.verbaltalk.skill.adapter.SkillPagerAdapter;
import com.yc.verbaltalk.base.fragment.BaseMainFragment;
import com.yc.verbaltalk.base.view.ColorFlipPagerTitleView;

import net.lucode.hackware.magicindicator.MagicIndicator;
import net.lucode.hackware.magicindicator.ViewPagerHelper;
import net.lucode.hackware.magicindicator.buildins.UIUtil;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator;
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView;

import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

/**
 * Created by sunshey on 2019/6/17.
 * 秘技主界面
 */

public class SkillFragment extends BaseMainFragment {

    private ViewPager mViewPager;
    private MagicIndicator mTabLayout;


    @Override
    protected int setContentView() {
        return R.layout.fragment_main_t2;
    }

    @Override
    protected void initViews() {
        View viewBar = rootView.findViewById(R.id.main_t2_new_view_bar);
        mMainActivity.setStateBarHeight(viewBar, 1);
        mTabLayout = rootView.findViewById(R.id.main_t2_new_pager_tabs);
        mViewPager = rootView.findViewById(R.id.main_t2_new_view_pager);


        netSwitchPagerData();
    }

    private void netSwitchPagerData() {

        String[] arrays = getResources().getStringArray(R.array.love_practice);

        List<String> titleList = Arrays.asList(arrays);

        initNavigator(titleList);

        SkillPagerAdapter mainT2NewPagerAdapter = new SkillPagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, titleList);
        mViewPager.setAdapter(mainT2NewPagerAdapter);
    }

    private void initNavigator(final List<String> titleList) {
        CommonNavigator commonNavigator = new CommonNavigator(mMainActivity);
//        commonNavigator.setScrollPivotX(0.65f);
        commonNavigator.setAdjustMode(true);
        CommonNavigatorAdapter navigatorAdapter = new CommonNavigatorAdapter() {

            @Override
            public int getCount() {
                return titleList == null ? 0 : titleList.size();
            }

            @Override
            public IPagerTitleView getTitleView(Context context, final int index) {
                SimplePagerTitleView simplePagerTitleView = new ColorFlipPagerTitleView(context);
                simplePagerTitleView.setText(titleList.get(index));
                simplePagerTitleView.setTextSize(16);
                simplePagerTitleView.setNormalColor(Color.BLACK);
                simplePagerTitleView.setSelectedColor(getResources().getColor(R.color.red_crimson));
                simplePagerTitleView.setOnClickListener(v -> mViewPager.setCurrentItem(index));
                return simplePagerTitleView;
            }

            @Override
            public IPagerIndicator getIndicator(Context context) {
                LinePagerIndicator indicator = new LinePagerIndicator(context);
//                indicator.setMode(LinePagerIndicator.MODE_WRAP_CONTENT);
                indicator.setMode(LinePagerIndicator.MODE_EXACTLY);
                indicator.setLineHeight(UIUtil.dip2px(context, 2));
                indicator.setLineWidth(UIUtil.dip2px(context, 30));
                indicator.setRoundRadius(UIUtil.dip2px(context, 3));
                indicator.setStartInterpolator(new AccelerateInterpolator());
                indicator.setEndInterpolator(new DecelerateInterpolator(2.0f));
                indicator.setColors(getResources().getColor(R.color.red_crimson));
                return indicator;
            }

            @Override
            public float getTitleWeight(Context context, int index) {
                if (index == 0) {
                    return 1.8f;
                } else if (index == 1) {
                    return 1.8f;
                } else {
                    return 1.0f;
                }
            }

        };
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                resetNavigator(commonNavigator);
                //
                SimplePagerTitleView pagerTitleView = (SimplePagerTitleView) commonNavigator.getPagerTitleView(position);
                //                pagerTitleView.setTextSize(12);
                pagerTitleView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        });
        commonNavigator.setAdapter(navigatorAdapter);
        mTabLayout.setNavigator(commonNavigator);
        ViewPagerHelper.bind(mTabLayout, mViewPager);
    }


    private void resetNavigator(CommonNavigator commonNavigator) {
        LinearLayout titleContainer = commonNavigator.getTitleContainer();
        int childCount = titleContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            SimplePagerTitleView pagerTitleView = (SimplePagerTitleView) titleContainer.getChildAt(i);
            pagerTitleView.setTypeface(Typeface.DEFAULT);

            //            pagerTitleView.setTextSize(20);
        }

    }

    @Override
    protected void lazyLoad() {

    }

}
