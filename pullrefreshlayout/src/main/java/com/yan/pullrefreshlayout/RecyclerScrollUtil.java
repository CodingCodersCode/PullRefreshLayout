package com.yan.pullrefreshlayout;

import android.graphics.Rect;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import java.lang.reflect.Field;

class RecyclerScrollUtil {

    private RecyclerScrollUtil() {
    }

    static boolean isRecyclerViewToTop(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null) {
                return true;
            }
            if (manager.getItemCount() == 0) {
                return true;
            }

            int firstChildTop = 0;
            if (recyclerView.getChildCount() > 0) {
                View firstVisibleChild = recyclerView.getChildAt(0);
                if (firstVisibleChild != null && firstVisibleChild.getMeasuredHeight() >= recyclerView.getMeasuredHeight()) {
                    if (Build.VERSION.SDK_INT < 14) {
                        return !(ViewCompat.canScrollVertically(recyclerView, -1) || recyclerView.getScrollY() > 0);
                    } else {
                        return !ViewCompat.canScrollVertically(recyclerView, -1);
                    }
                }

                View firstChild = recyclerView.getChildAt(0);
                RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) firstChild.getLayoutParams();
                firstChildTop = firstChild.getTop() - layoutParams.topMargin - getRecyclerViewItemTopInset(layoutParams) - recyclerView.getPaddingTop();
            }
            if (manager instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                if (layoutManager.findFirstCompletelyVisibleItemPosition() < 1 && firstChildTop == 0) {
                    return true;
                }
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) manager;
                int[] out = layoutManager.findFirstCompletelyVisibleItemPositions(null);
                if (out[0] < 1 && firstChildTop == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    static boolean isRecyclerViewToBottom(RecyclerView recyclerView) {
        if (recyclerView != null) {
            RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
            if (manager == null || manager.getItemCount() == 0) {
                return false;
            }

            if (manager instanceof LinearLayoutManager) {
                View lastVisibleChild = recyclerView.getChildAt(recyclerView.getChildCount() - 1);
                if (lastVisibleChild != null && lastVisibleChild.getMeasuredHeight() >= recyclerView.getMeasuredHeight()) {
                    if (Build.VERSION.SDK_INT < 14) {
                        return !(ViewCompat.canScrollVertically(recyclerView, 1) || recyclerView.getScrollY() < 0);
                    } else {
                        return !ViewCompat.canScrollVertically(recyclerView, 1);
                    }
                }

                LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
                if (layoutManager.findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1) {
                    return true;
                }
            } else if (manager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager layoutManager = (StaggeredGridLayoutManager) manager;

                int[] out = layoutManager.findLastCompletelyVisibleItemPositions(null);
                int lastPosition = layoutManager.getItemCount() - 1;
                for (int position : out) {
                    if (position == lastPosition) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static int getRecyclerViewItemTopInset(RecyclerView.LayoutParams layoutParams) {
        try {
            Field field = RecyclerView.LayoutParams.class.getDeclaredField("mDecorInsets");
            field.setAccessible(true);
            Rect decorInsets = (Rect) field.get(layoutParams);
            return decorInsets.top;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}