package musicplayer.playmusic.audioplayer.base.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import java.lang.reflect.Constructor;

import musicplayer.playmusic.audioplayer.base.R;

/**
 * 自定义的PopupMenu实现
 */
public class MusicPopupMenu extends PopupWindow {

    private MenuInflater mMenuInflater;

    private Context mContext;

    private Menu mMenu;

    private LayoutInflater mInflater;

    private View mRootView;
    private LinearLayout mMenuView;//菜单布局
    private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener;

    public MusicPopupMenu(Context context) {
        super(context);
        mContext = context;
        mMenuInflater = new MenuInflater(context);
        mInflater = LayoutInflater.from(context);
        mMenu = newMenuInstance(context);
        mRootView = mInflater.inflate(R.layout.view_popmenu_compat, null);
        mRootView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.default_popup_background));
        mMenuView = mRootView.findViewById(R.id.container);
        setContentView(mRootView);

        setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setOutsideTouchable(true);
        setFocusable(true);
        setTouchable(true);

    }

    public View getRootView() {
        return mRootView;
    }

    public void inflate(int menuRes) {
        if (mMenu == null || mMenuInflater == null) {
            return;
        }
        mMenuInflater.inflate(menuRes, mMenu);

        for (int i = 0; i < mMenu.size(); i++) {
            MenuItem item = mMenu.getItem(i);
            mMenuView.addView(createMenuItemView(mInflater, mMenuView, item));
        }
    }

    public void setMenuItemVisible(int id, boolean visible) {
        View view = mMenuView.findViewById(id);
        if (view != null) {
            if (view.getTag() != null && view.getTag() instanceof MenuItem) {
                MenuItem item = (MenuItem) view.getTag();
                item.setVisible(visible);
            }
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        super.showAsDropDown(anchor);
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }


    private View createMenuItemView(final LayoutInflater inflater, ViewGroup rootView, final MenuItem item) {
        final View itemView = inflater.inflate(R.layout.item_menu_instance, rootView, false);
        final ImageView iconImageView = itemView.findViewById(R.id.menu_item_icon);
        final TextView textView = itemView.findViewById(R.id.menu_item_title);
        textView.setText(item.getTitle());
        Drawable drawable = item.getIcon();
        if (drawable != null) {
            iconImageView.setVisibility(View.VISIBLE);
            iconImageView.setImageDrawable(drawable);
        } else {
            iconImageView.setVisibility(View.GONE);
        }

        itemView.setOnClickListener(view -> {
            if (mOnMenuItemClickListener != null) {
                mOnMenuItemClickListener.onMenuItemClick(item);
            }
            dismiss();
        });
        itemView.setId(item.getItemId());
        itemView.setTag(item);
        itemView.setVisibility(item.isVisible() ? View.VISIBLE : View.GONE);
        return itemView;
    }


    @SuppressLint("PrivateApi")
    private Menu newMenuInstance(Context context) {
        try {
            Class<?> menuBuilderClass = Class.forName("com.android.internal.view.menu.MenuBuilder");
            Constructor<?> constructor = menuBuilderClass.getDeclaredConstructor(Context.class);
            return (Menu) constructor.newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
