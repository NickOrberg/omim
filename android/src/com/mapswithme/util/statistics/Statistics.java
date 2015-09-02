package com.mapswithme.util.statistics;

import android.app.Activity;

import com.mapswithme.country.ActiveCountryTree;
import com.mapswithme.maps.BuildConfig;
import com.mapswithme.maps.MwmApplication;
import com.mapswithme.maps.R;
import com.mapswithme.maps.api.ParsedMwmRequest;
import com.mapswithme.util.FbUtil;
import com.mapswithme.util.log.Logger;
import com.mapswithme.util.log.SimpleLogger;
import com.mapswithme.util.log.StubLogger;

import java.util.ArrayList;
import java.util.List;

public enum Statistics
{
  INSTANCE;

  private final static String KEY_STAT_ENABLED = "StatisticsEnabled";

  private List<StatisticsEngine> mStatisticsEngines;
  private EventBuilder mEventBuilder;

  private final Logger mLogger = BuildConfig.DEBUG ? SimpleLogger.get("MwmStatistics") : StubLogger.get();

  // Statistics counters
  private int mBookmarksCreated;
  private int mSharedTimes;

  public static class EventName
  {
    public static final String COUNTRY_DOWNLOAD = "Country download";
    public static final String YOTA_BACK_CALL = "Yota back screen call";
    public static final String COUNTRY_UPDATE = "Country update";
    public static final String COUNTRY_DELETE = "Country deleted";
    public static final String SEARCH_CAT_CLICKED = "Search category clicked";
    public static final String DESCRIPTION_CHANGED = "Description changed";
    public static final String GROUP_CREATED = "Group Created";
    public static final String GROUP_CHANGED = "Group changed";
    public static final String COLOR_CHANGED = "Color changed";
    public static final String BOOKMARK_CREATED = "Bookmark created";
    public static final String PLACE_SHARED = "Place Shared";
    public static final String API_CALLED = "API called";
    public static final String WIFI_CONNECTED = "Wifi connected";
    public static final String DOWNLOAD_COUNTRY_NOTIFICATION_SHOWN = "Download country notification shown";
    public static final String DOWNLOAD_COUNTRY_NOTIFICATION_CLICKED = "Download country notification clicked";
    // settings
    public static final String SETTINGS_CONTACT_US = "Send mail at info@maps.me";
    public static final String SETTINGS_MAIL_SUBSCRIBE = "Settings. Subscribed";
    public static final String SETTINGS_REPORT_BUG = "Settings. Bug reported";
    public static final String SETTINGS_RATE = "Settings. Rate app called";
    public static final String SETTINGS_FB = "Settings. Go to FB.";
    public static final String SETTINGS_TWITTER = "Settings. Go to twitter.";
    public static final String SETTINGS_HELP = "Settings. Help.";
    public static final String SETTINGS_ABOUT = "Settings. About.";
    public static final String SETTINGS_COPYRIGHT = "Settings. Copyright.";
    public static final String SETTINGS_COMMUNITY = "Settings. Community.";
    public static final String SETTINGS_CHANGE_SETTING = "Settings. Change settings.";
    public static final String SEARCH_KEY_CLICKED = "Search key pressed.";
    public static final String SEARCH_ON_MAP_CLICKED = "Search on map clicked.";
    public static final String STATISTICS_STATUS_CHANGED = "Statistics status changed";
    //
    public static final String PLUS_DIALOG_LATER = "GPlus dialog cancelled.";
    public static final String RATE_DIALOG_LATER = "GPlay dialog cancelled.";
    public static final String FACEBOOK_INVITE_LATER = "Facebook invites dialog cancelled.";
    public static final String FACEBOOK_INVITE_INVITED = "GPlay dialog cancelled.";
    public static final String RATE_DIALOG_RATED = "GPlay dialog. Rating set";
    public static final String FACEBOOK_PEDESTRIAN_CANCEL = "Facebook pedestrian cancelled.";
    public static final String FACEBOOK_PEDESTRIAN_SHARE = "Facebook pedestrian shared.";
  }

  public static class EventParam
  {
    public static final String FROM = "from";
    public static final String TO = "to";
    public static final String CATEGORY = "category";
    public static final String COUNT = "Count";
    public static final String CHANNEL = "Channel";
    public static final String CALLER_ID = "Caller ID";
    public static final String HAD_VALID_LOCATION = "Had valid location";
    public static final String DELAY_MILLIS = "Delay in milliseconds";
    public static final String ENABLED = "Enabled";
    public static final String RATING = "Rating";
  }

  Statistics()
  {
    configure();
    mLogger.d("Created Statistics instance.");
  }

  public void trackIfEnabled(Event event)
  {
    if (isStatisticsEnabled())
    {
      post(event);
      mLogger.d("Posted event:", event);
    }
    else
      mLogger.d("Skipped event:", event);
  }

  private void post(Event event)
  {
    for (StatisticsEngine engine : mStatisticsEngines)
      engine.postEvent(event);
  }

  public void trackBackscreenCall(String from)
  {
    final Event event = mEventBuilder
        .setName(EventName.YOTA_BACK_CALL)
        .addParam(EventParam.FROM, from)
        .buildEvent();

    trackIfEnabled(event);
  }

  public void trackCountryDownload()
  {
    trackIfEnabled(mEventBuilder.
        setName(EventName.COUNTRY_DOWNLOAD).
        addParam(EventParam.COUNT, String.valueOf(ActiveCountryTree.getTotalDownloadedCount())).
        buildEvent());
  }

  public void trackCountryUpdate()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.COUNTRY_UPDATE).buildEvent());
  }

  public void trackCountryDeleted()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.COUNTRY_DELETE).buildEvent());
  }

  public void trackSearchCategoryClicked(String category)
  {
    final Event event = mEventBuilder
        .setName(EventName.SEARCH_CAT_CLICKED)
        .addParam(EventParam.CATEGORY, category)
        .buildEvent();

    trackIfEnabled(event);
  }

  public void trackDescriptionChanged()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.DESCRIPTION_CHANGED).buildEvent());
  }

  public void trackGroupCreated()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.GROUP_CREATED).buildEvent());
  }

  public void trackColorChanged(String from, String to)
  {
    final Event event = mEventBuilder
        .setName(EventName.COLOR_CHANGED)
        .addParam(EventParam.FROM, from)
        .addParam(EventParam.TO, to)
        .buildEvent();

    trackIfEnabled(event);
  }

  public void trackBookmarkCreated()
  {
    final Event event = mEventBuilder
        .setName(EventName.BOOKMARK_CREATED)
        .addParam(EventParam.COUNT, String.valueOf(++mBookmarksCreated))
        .buildEvent();

    trackIfEnabled(event);
  }

  public void trackPlaceShared(String channel)
  {
    final Event event = mEventBuilder
        .setName(EventName.PLACE_SHARED)
        .addParam(EventParam.CHANNEL, channel)
        .addParam(EventParam.COUNT, String.valueOf(++mSharedTimes))
        .buildEvent();

    trackIfEnabled(event);
  }

  public void trackApiCall(ParsedMwmRequest request)
  {
    if (request != null && request.getCallerInfo() != null)
    {
      //@formatter:off
      final Event event = mEventBuilder
          .setName(EventName.API_CALLED)
          .addParam(EventParam.CALLER_ID, request.getCallerInfo().packageName)
          .buildEvent();
      //@formatter:on
      trackIfEnabled(event);
    }
  }

  public void trackWifiConnected(boolean hasValidLocation)
  {
    final Event event = mEventBuilder.
        setName(EventName.WIFI_CONNECTED).
        addParam(EventParam.HAD_VALID_LOCATION, String.valueOf(hasValidLocation)).
        buildEvent();
    trackIfEnabled(event);
  }

  public void trackWifiConnectedAfterDelay(boolean isLocationExpired, long delayMillis)
  {
    final Event event = mEventBuilder.
        setName(EventName.WIFI_CONNECTED).
        addParam(EventParam.HAD_VALID_LOCATION, String.valueOf(isLocationExpired)).
        addParam(EventParam.DELAY_MILLIS, String.valueOf(delayMillis)).
        buildEvent();
    trackIfEnabled(event);
  }

  public void trackDownloadCountryNotificationShown()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.DOWNLOAD_COUNTRY_NOTIFICATION_SHOWN).buildEvent());
  }

  public void trackDownloadCountryNotificationClicked()
  {
    trackIfEnabled(mEventBuilder.setName(EventName.DOWNLOAD_COUNTRY_NOTIFICATION_CLICKED).buildEvent());
  }

  public void trackRatingDialog(float rating)
  {
    final Event event = mEventBuilder.
        setName(EventName.RATE_DIALOG_RATED).
        addParam(EventParam.RATING, String.valueOf(rating)).
        buildEvent();
    trackIfEnabled(event);
  }

  public void trackSimpleNamedEvent(String eventName)
  {
    trackIfEnabled(mEventBuilder.setName(eventName).buildEvent());
  }

  public void startActivity(Activity activity)
  {
    if (isStatisticsEnabled())
    {
      for (StatisticsEngine engine : mStatisticsEngines)
        engine.onStartActivity(activity);

      FbUtil.activate(activity);
    }
  }

  private void configure()
  {
    final String key = MwmApplication.get().getResources().getString(R.string.flurry_app_key);
    mStatisticsEngines = new ArrayList<>();

    final StatisticsEngine flurryEngine = new FlurryEngine(BuildConfig.DEBUG, key);
    flurryEngine.configure(MwmApplication.get(), null);
    mStatisticsEngines.add(flurryEngine);

    mEventBuilder = new EventBuilder();
  }

  public void stopActivity(Activity activity)
  {
    if (isStatisticsEnabled())
    {
      for (StatisticsEngine engine : mStatisticsEngines)
        engine.onEndActivity(activity);

      FbUtil.deactivate(activity);
    }
  }

  public boolean isStatisticsEnabled()
  {
    boolean isStatisticsEnabledByDefault = true;
    if (BuildConfig.DEBUG)
      isStatisticsEnabledByDefault = false;
    return MwmApplication.get().nativeGetBoolean(KEY_STAT_ENABLED, isStatisticsEnabledByDefault);
  }

  public void setStatEnabled(boolean isEnabled)
  {
    MwmApplication.get().nativeSetBoolean(KEY_STAT_ENABLED, isEnabled);
    // We track if user turned on/off
    // statistics to understand data better.
    post(mEventBuilder
        .setName(EventName.STATISTICS_STATUS_CHANGED + " " + MwmApplication.get().getFirstInstallFlavor())
        .addParam(EventParam.ENABLED, String.valueOf(isEnabled))
        .buildEvent());
  }
}
