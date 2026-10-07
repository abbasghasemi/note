package ghasemi.abbas.note.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ghasemi.abbas.note.R;
import ghasemi.abbas.note.RecommendedApps;

public final class RecommendedAppsView extends LinearLayout {
    private static final String PREFS = "recommended_apps_view";
    private static final String HIDDEN_UNTIL = "hidden_until";
    private final RecyclerView list;
    private final boolean settingsMode;
    private final AppsAdapter adapter = new AppsAdapter();
    private final int rowHeight;

    public RecommendedAppsView(Context context) {
        this(context, null, false);
    }

    public RecommendedAppsView(Context context, AttributeSet attrs) {
        this(context, attrs, false);
    }

    public RecommendedAppsView(Context context, boolean settingsMode) {
        this(context, null, settingsMode);
    }

    private RecommendedAppsView(Context context, AttributeSet attrs, boolean settingsMode) {
        super(context, attrs);
        this.settingsMode = settingsMode;
        setOrientation(VERTICAL);
        int surface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface);
        int onSurface = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface);
        setBackgroundColor(surface);
        setPadding(dp(10), dp(3), dp(10), dp(3));
        setVisibility(GONE);
        rowHeight = dp(56);

        if (settingsMode) {
            TextView heading = new TextView(context);
            heading.setText(R.string.other_apps);
            heading.setTextSize(16);
            heading.setTypeface(ResourcesCompat.getFont(context, R.font.sans_bold));
            heading.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            heading.setPadding(0, dp(8), 0, dp(8));
            addView(heading, new LinearLayout.LayoutParams(-1, -2));
        } else {
            FrameLayout header = new FrameLayout(context);
            View rule = new View(context);
            rule.setBackgroundColor(getResources().getColor(R.color.colorDivider));
            FrameLayout.LayoutParams ruleParams = new FrameLayout.LayoutParams(-1, dp(1), Gravity.CENTER_VERTICAL);
            header.addView(rule, ruleParams);
            ImageView toggle = new ImageView(context);
            toggle.setImageResource(R.drawable.ic_recommended_toggle);
            toggle.setColorFilter(onSurface);
            toggle.setScaleType(ImageView.ScaleType.CENTER);
            toggle.setRotation(180f);
            toggle.setContentDescription(getResources().getString(R.string.hide_recommended_apps));
            toggle.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000),
                    rounded(surface, getResources().getColor(R.color.colorDivider), dp(16)),
                    rounded(Color.WHITE, 0, dp(16))));
            toggle.setOnClickListener(v -> dismissForMonth());
            header.addView(toggle, new FrameLayout.LayoutParams(dp(34), dp(30), Gravity.CENTER));
            addView(header, new LinearLayout.LayoutParams(-1, dp(32)));
        }

        list = new RecyclerView(context);
        list.setLayoutManager(new LinearLayoutManager(context,
                settingsMode ? RecyclerView.VERTICAL : RecyclerView.HORIZONTAL, false));
        list.setAdapter(adapter);
        list.setOverScrollMode(OVER_SCROLL_NEVER);
        list.setNestedScrollingEnabled(false);
        list.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (right - left > 0) adapter.updateViewport(right - left);
        });
        addView(list, new LinearLayout.LayoutParams(-1, settingsMode ? 0 : rowHeight));

        if (!settingsMode && context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getLong(HIDDEN_UNTIL, 0) > System.currentTimeMillis()) return;

        RecommendedApps.load(context, apps -> {
            adapter.submit(apps);
            if (settingsMode) {
                ViewGroup.LayoutParams params = list.getLayoutParams();
                params.height = apps.size() * (rowHeight + dp(6));
                list.setLayoutParams(params);
            }
            setVisibility(apps.isEmpty() ? GONE : VISIBLE);
        });
    }

    private void dismissForMonth() {
        long until = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30);
        getContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putLong(HIDDEN_UNTIL, until).apply();
        setVisibility(GONE);
    }

    private int dp(float value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics());
    }

    private GradientDrawable rounded(int fill, int stroke, int radius) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(fill);
        shape.setCornerRadius(radius);
        if (stroke != 0) shape.setStroke(dp(1), stroke);
        return shape;
    }

    private final class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.Holder> {
        private final List<RecommendedApps.App> apps = new ArrayList<>();
        private int viewport;
        private final int gap = dp(6);

        void submit(List<RecommendedApps.App> items) {
            apps.clear();
            apps.addAll(items);
            notifyDataSetChanged();
        }

        void updateViewport(int width) {
            if (viewport != width) {
                viewport = width;
                notifyDataSetChanged();
            }
        }

        @Override public int getItemCount() { return apps.size(); }

        @NonNull @Override public Holder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
            Context context = parent.getContext();
            LinearLayout card = new LinearLayout(context);
            card.setOrientation(HORIZONTAL);
            card.setGravity(Gravity.CENTER_VERTICAL);
            card.setPadding(dp(7), dp(3), dp(7), dp(3));
            card.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22000000),
                    rounded(Color.TRANSPARENT, getResources().getColor(R.color.colorDivider), dp(9)),
                    rounded(Color.WHITE, 0, dp(9))));
            card.setClickable(true);
            card.setLayoutParams(new RecyclerView.LayoutParams(LayoutParams.MATCH_PARENT, rowHeight));

            ImageView icon = new ImageView(context);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            card.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));
            LinearLayout details = new LinearLayout(context);
            details.setOrientation(VERTICAL);
            details.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(0, -1, 1f);
            detailParams.setMarginStart(dp(8));
            card.addView(details, detailParams);

            TextView title = new TextView(context);
            title.setTextSize(13);
            title.setTypeface(ResourcesCompat.getFont(context, R.font.sans_bold));
            title.setTextColor(MaterialColors.getColor(card, com.google.android.material.R.attr.colorOnSurface));
            title.setSingleLine(true);
            title.setEllipsize(TextUtils.TruncateAt.MARQUEE);
            title.setMarqueeRepeatLimit(-1);
            title.setSelected(true);
            details.addView(title, new LinearLayout.LayoutParams(-1, -2));
            TextView badge = new TextView(context);
            badge.setTextSize(10);
            badge.setTypeface(ResourcesCompat.getFont(context, R.font.sans));
            badge.setMaxLines(1);
            badge.setEllipsize(TextUtils.TruncateAt.END);
            badge.setPadding(dp(6), 0, dp(6), 0);
            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(-2, -2);
            badgeParams.topMargin = dp(1);
            details.addView(badge, badgeParams);
            return new Holder(card, icon, title, badge);
        }

        @Override public void onBindViewHolder(@NonNull Holder holder, int position) {
            RecommendedApps.App app = apps.get(position);
            int width = apps.size() == 1 ? viewport : apps.size() == 2
                    ? (viewport - gap) / 2 : (int) ((viewport - 2 * gap) / 2.15f);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.width = settingsMode ? ViewGroup.LayoutParams.MATCH_PARENT : Math.max(dp(100), width);
            params.setMarginEnd(settingsMode || position == apps.size() - 1 ? 0 : gap);
            params.bottomMargin = settingsMode ? dp(6) : 0;
            holder.itemView.setLayoutParams(params);
            holder.title.setText(app.title);
            holder.badge.setVisibility(app.badge == null ? GONE : VISIBLE);
            if (app.badge != null) {
                holder.badge.setText(app.badge);
                int badgeColor = 0xff0cdc73;
                if (app.badgeColor != null && app.badgeColor.matches("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?")) {
                    try { badgeColor = Color.parseColor(app.badgeColor); } catch (IllegalArgumentException ignored) { }
                }
                holder.badge.setBackground(rounded(badgeColor, 0, dp(5)));
                int surface = MaterialColors.getColor(holder.itemView,
                        com.google.android.material.R.attr.colorSurface);
                int visible = ColorUtils.compositeColors(badgeColor, surface);
                holder.badge.setTextColor(ColorUtils.calculateContrast(Color.BLACK, visible)
                        >= ColorUtils.calculateContrast(Color.WHITE, visible) ? Color.BLACK : Color.WHITE);
            }
            if (app.icon.startsWith("https://")) Glide.with(holder.icon).load(app.icon)
                    .placeholder(android.R.drawable.sym_def_app_icon).into(holder.icon);
            else holder.icon.setImageResource(android.R.drawable.sym_def_app_icon);
            holder.itemView.setOnClickListener(v -> {
                try { getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(app.url))); }
                catch (Exception ignored) { }
            });
        }

        final class Holder extends RecyclerView.ViewHolder {
            final ImageView icon;
            final TextView title, badge;
            Holder(View view, ImageView icon, TextView title, TextView badge) {
                super(view);
                this.icon = icon;
                this.title = title;
                this.badge = badge;
            }
        }
    }
}
