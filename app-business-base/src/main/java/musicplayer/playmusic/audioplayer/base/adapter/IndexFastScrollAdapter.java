package musicplayer.playmusic.audioplayer.base.adapter;


import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;

import androidx.annotation.NonNull;
import androidx.collection.SparseArrayCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import dev.android.player.framework.data.model.IndexModel;

/**
 * RecyclerView Adapter
 */
public abstract class IndexFastScrollAdapter<D, T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SectionIndexer {

    private static final String TAG = "IndexFastScrollAdapter";

    private static final int BASE_ITEM_TYPE_HEADER = 100000;

    private static final int BASE_ITEM_TYPE_FOOTER = 200000;

    private boolean isEnableHeader = true;//是否启用头部
    private final SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();

    private boolean isEnableFooter = true;//是否启用尾部
    private final SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();
    //当前列表的数据源
    private final List<D> mData = Collections.synchronizedList(new ArrayList<>());

    private final ArrayList<Integer> mSectionPositions = new ArrayList<>();


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            View view = Objects.requireNonNull(mHeaderViews.get(viewType));
            //View 的父布局移除当前View
            ViewGroup parentView = (ViewGroup) view.getParent();
            if (parentView != null) {
                parentView.removeView(view);
            }
            return new CommonViewHolder(view);

        } else if (mFootViews.get(viewType) != null) {
            View view = Objects.requireNonNull(mFootViews.get(viewType));
            //View 的父布局移除当前View
            ViewGroup parentView = (ViewGroup) view.getParent();
            if (parentView != null) {
                parentView.removeView(view);
            }
            return new CommonViewHolder(view);
        } else {
            return onCreateViewHolderCompat(parent, viewType);
        }
    }

    protected abstract RecyclerView.ViewHolder onCreateViewHolderCompat(@NonNull ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (isHeaderViewPos(position)) {
            View view = mHeaderViews.get(getItemViewType(position));
            if (view != null) {
                view.postInvalidate();
            }
        } else if (isFooterViewPos(position)) {
            View view = mFootViews.get(position + BASE_ITEM_TYPE_FOOTER);
            if (view != null) {
                view.postInvalidate();
            }
        } else {
            onBindViewHolderCompat((T) holder, position - getHeadersCount());
        }
    }

    protected abstract void onBindViewHolderCompat(@NonNull T holder, int position);

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewPos(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewPos(position)) {
            return mFootViews.keyAt(position - getHeadersCount() - getItemCountCompat());
        } else {
            return getItemViewTypeCompat(position - getHeadersCount());
        }
    }

    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager) {
            ((GridLayoutManager) manager).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    return isHeaderViewPos(position) || isFooterViewPos(position) ? ((GridLayoutManager) manager).getSpanCount() : 1;
                }
            });
        }
    }

    public int getItemViewTypeCompat(int position) {
        return 0;
    }


    private boolean isHeaderViewPos(int position) {
        return position < getHeadersCount();
    }

    private boolean isFooterViewPos(int position) {
        return position >= getHeadersCount() + getItemCountCompat();
    }


    public void addHeaderView(View view) {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view);
    }

    public void removeHeaderView(View view) {
        int index = mHeaderViews.indexOfValue(view);
        mHeaderViews.remove(index + BASE_ITEM_TYPE_HEADER);
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
    }

    public void clearHeaderView() {
        mHeaderViews.clear();
    }

    public void clearFooterView() {
        mFootViews.clear();
    }

    public void clearAllView() {
        clearHeaderView();
        clearFooterView();
    }

    public void setEnableHeader(boolean enableHeader) {
        isEnableHeader = enableHeader;
        notifyDataSetChanged();
    }

    public boolean isEnableHeader() {
        return isEnableHeader;
    }

    public void setEnableFooter(boolean enableFooter) {
        isEnableFooter = enableFooter;
        notifyDataSetChanged();
    }

    public boolean isEnableFooter() {
        return isEnableFooter;
    }


    public void addFootView(View view) {
        mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, view);
    }

    public void removeFootView(View view) {
        int index = mFootViews.indexOfValue(view);
        mFootViews.remove(index + BASE_ITEM_TYPE_FOOTER);
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFootViews.size();
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + getItemCountCompat();
    }

    /**
     * 数据类的数量
     *
     * @return
     */
    public int getItemCountCompat() {
        return getDataSize();
    }

    public int getDataSize() {
        return mData == null ? 0 : mData.size();
    }

    public D getAdapterPositionData(int position) {
        if (position < getHeadersCount() || position > getHeadersCount() + getDataSize()) {
            return null;
        }
        return getData(position - getHeadersCount());
    }

    public D getData(int position) {
        if (position < 0 || position >= mData.size()) {
            return null;
        } else {
            return mData.get(position);
        }
    }

    public List<D> getData() {
        return mData;
    }

    public void setData(List<D> data) {
        this.setData(data, true);
    }

    public void setData(List<D> data, boolean isClear) {
        if (mData != data) {//地址不一样
            if (isClear) {
                mData.clear();
            }
            if (data != null && !data.isEmpty()) {
                mData.addAll(data);
            }
        }
        notifyDataSetChanged();
    }

    public void addData(D data) {
        if (data != null) {
            mData.add(data);
            notifyItemInserted(getItemCount());
        }
    }

    public void addData(List<D> data) {
        if (data != null && !data.isEmpty()) {
            mData.addAll(data);
            notifyItemRangeInserted(getItemCount(), data.size());
        }
    }

    public void addData(int position, D data) {
        if (data != null) {
            mData.add(position, data);
            notifyItemInserted(position + getHeadersCount());
        }
    }

    public void addData(int position, List<D> data) {
        if (data != null && !data.isEmpty()) {
            mData.addAll(position, data);
            notifyItemRangeInserted(position + getHeadersCount(), data.size());
        }
    }

    public void removeData(int position) {
        if (position < 0 || position >= getDataSize()) {
            return;
        }
        mData.remove(position);
        notifyItemRemoved(position + getHeadersCount());
    }

    public void removeData(List<D> datas) {
        if (datas != null && !datas.isEmpty()) {
            for (D data : datas) {
                int position = mData.indexOf(data);
                if (position >= 0) {
                    mData.remove(position);
                    notifyItemRemoved(position + getHeadersCount());
                }
            }
        }
    }

    public void removeData(D data) {
        int position = mData.indexOf(data);
        if (position >= 0) {
            removeData(position);
        }
    }

    public void swapData(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= getDataSize() || toPosition < 0 || toPosition >= getDataSize()) {
            return;
        }
        synchronized (mData) {
            Collections.swap(mData, fromPosition, toPosition);
        }
        notifyItemMoved(fromPosition + getHeadersCount(), toPosition + getHeadersCount());
    }

    public void moveData(int fromPosition, int toPosition) {
        if (fromPosition < 0 || fromPosition >= getDataSize() || toPosition < 0 || toPosition >= getDataSize() || fromPosition == toPosition) {
            return;
        }
        D data = mData.get(fromPosition);
        mData.remove(fromPosition);
        mData.add(toPosition, data);
        notifyDataSetChanged();
    }


    /**
     * 找到传入的数据在数据集中的位置 然后NotifyItem
     */
    public void notifyItem(D data) {
        int position = mData.indexOf(data);
        if (position != -1) {
            notifyItemChanged(position + getHeadersCount());
        }
    }

    /**
     * 通过Position获取原始Position
     */
    public int getPositionOffset(int position) {
        return position + getHeadersCount();
    }


    /**
     * 标记查找索引值的Key
     */
    private String mIndexerKey = null;

    public String getIndexerKey() {
        return mIndexerKey;
    }

    /**
     * 这里值得注意，为什么不将排序的Key设置在数据源中，
     * 现在项目中用的歌曲列表统一为一个对象，如果切换列表，排序方式发生变化，
     * 那么设置的key就会被其他列表的数据污染，导致返回时数据错乱，这里一定要根据每个页面的不同
     * 设置不同的标记来记录
     *
     * @param key
     */
    public void setIndexerKey(String key) {
        this.mIndexerKey = key;
    }


    @Override
    public Object[] getSections() {
        return getSortSections(mIndexerKey);
    }

    @Override
    public int getPositionForSection(int index) {
        if (index >= 0 && index < mSectionPositions.size()) {
            return mSectionPositions.get(index);
        } else {
            return -1;
        }
    }

    @Override
    public int getSectionForPosition(int position) {
        Log.d(TAG, "getSectionForPosition() called with: position = [" + position + "]");
        return 0;
    }

    /**
     * 标记是否是 Indexer列表数据
     *
     * @return
     */
    protected boolean isIndexerData() {
        return true;
    }

    /**
     * 返回侧边排序后的标签
     *
     * @return
     */
    private String[] getSortSections(String key) {
        if (isIndexerData()) {
            List<String> sections = new ArrayList<>();
            mSectionPositions.clear();
            for (int i = 0; i < getItemCount(); i++) {
                D data = getAdapterPositionData(i);
                if (data != null && data instanceof IndexModel) {
                    String section = ((IndexModel) data).getIndexer(key);
                    if (!TextUtils.isEmpty(section) && !sections.contains(section)) {
                        sections.add(section);
                        mSectionPositions.add(i);
                    }
                }
            }
            return sections.toArray(new String[]{});
        } else {
            return new String[]{};
        }
    }

    /**
     * 通用的一个ViewHolder 用于配置Header和Footer
     */
    private static class CommonViewHolder extends RecyclerView.ViewHolder {

        public CommonViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

}
