package com.ls.ui.drawer;

import android.content.Context;

import com.ls.drupalcon.R;
import com.ls.drupalcon.app.App;

import java.util.ArrayList;
import java.util.List;

public class DrawerMenu {

    public static List<DrawerMenuItem> getNavigationDrawerItems() {
        Context context = App.getContext();
        List<DrawerMenuItem> result = new ArrayList<>();
        result.add(new DrawerMenuItem(context.getString(R.string.Sessions), R.drawable.menu_icon_program, R.drawable.menu_icon_program_sel, EventMode.Program, false));
        result.add(new DrawerMenuItem(context.getString(R.string.bofs), R.drawable.menu_icon_bofs, R.drawable.menu_icon_bofs_sel, EventMode.Bofs, false));
        result.add(new DrawerMenuItem(context.getString(R.string.social_events), R.drawable.menu_icon_social, R.drawable.menu_icon_social_sel, EventMode.Social, false));
        result.add(new DrawerMenuItem(context.getString(R.string.social_media), R.drawable.menu_icon_social_media, R.drawable.menu_icon_social_media_sel, EventMode.SocialMedia, true));
        result.add(new DrawerMenuItem(context.getString(R.string.my_schedule), R.drawable.menu_icon_my_schedule, R.drawable.menu_icon_my_schedule_sel, EventMode.Favorites, false));
        result.add(new DrawerMenuItem(context.getString(R.string.floor_plan), R.drawable.menu_icon_floor_plan, R.drawable.menu_icon_floor_plan_sel, EventMode.FloorPlan, false));
        result.add(new DrawerMenuItem(context.getString(R.string.location), R.drawable.menu_icon_location, R.drawable.menu_icon_location_sel, EventMode.Location, false));
        result.add(new DrawerMenuItem(context.getString(R.string.speakers), R.drawable.menu_icon_speakers, R.drawable.menu_icon_speakers_sel, EventMode.Speakers, true));
        result.add(new DrawerMenuItem(context.getString(R.string.about), R.drawable.menu_icon_about, R.drawable.menu_icon_about_sel, EventMode.About, false));

        return result;
    }
}
