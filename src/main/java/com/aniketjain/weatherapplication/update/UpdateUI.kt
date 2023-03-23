package com.aniketjain.weatherapplication.update

import com.aniketjain.weatherapplication.location.LocationCord
import com.aniketjain.roastedtoast.Toasty
import com.aniketjain.weatherapp.R
import androidx.recyclerview.widget.RecyclerView
import com.aniketjain.weatherapplication.adapter.DaysAdapter.DayViewHolder
import android.view.ViewGroup
import android.view.View
import android.view.LayoutInflater
import android.annotation.SuppressLint
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.android.volley.toolbox.JsonObjectRequest
import org.json.JSONObject
import java.util.Locale
import org.json.JSONException
import android.util.Log
import com.aniketjain.weatherapplication.update.UpdateUI
import com.github.ybq.android.spinkit.SpinKitView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.ImageView
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.location.Location
import java.lang.NullPointerException
import android.location.Geocoder
import android.location.Address
import java.lang.Exception
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.app.Activity
import android.content.Context
import java.util.ArrayList
import android.speech.RecognizerIntent
import android.os.Build
import com.aniketjain.weatherapplication.adapter.DaysAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.widget.TextView.OnEditorActionListener
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.View.OnFocusChangeListener
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.aniketjain.weatherapplication.toast.Toaster
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.aniketjain.weatherapplication.HomeActivity
import com.aniketjain.weatherapplication.location.CityFinder
import com.android.volley.VolleyError
import android.view.inputmethod.InputMethodManager
import com.aniketjain.weatherapplication.network.InternetConnectivity
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.install.model.AppUpdateType
import android.content.IntentSender.SendIntentException
import android.view.WindowManager
import java.lang.Runnable

object UpdateUI {
    fun getIconID(condition: Int, update_time: Long, sunrise: Long, sunset: Long): String? {
        if (condition >= 200 && condition <= 232) return "thunderstorm" else if (condition >= 300 && condition <= 321) return "drizzle" else if (condition >= 500 && condition <= 531) return "rain" else if (condition >= 600 && condition <= 622) return "snow" else if (condition >= 701 && condition <= 781) return "wind" else if (condition == 800) {
            return if (update_time >= sunrise && update_time <= sunset) "clear_day" else "clear_night"
        } else if (condition == 801) {
            return if (update_time >= sunrise && update_time <= sunset) "few_clouds_day" else "few_clouds_night"
        } else if (condition == 802) return "scattered_clouds" else if (condition == 803 || condition == 804) return "broken_clouds"
        return null
    }

    fun TranslateDay(dayToBeTranslated: String?, context: Context): String? {
        when (dayToBeTranslated!!.trim { it <= ' ' }) {
            "Monday" -> return context.resources.getString(R.string.monday)
            "Tuesday" -> return context.resources.getString(R.string.tuesday)
            "Wednesday" -> return context.resources.getString(R.string.wednesday)
            "Thursday" -> return context.resources.getString(R.string.thursday)
            "Friday" -> return context.resources.getString(R.string.friday)
            "Saturday" -> return context.resources.getString(R.string.saturday)
            "Sunday" -> return context.resources.getString(R.string.sunday)
        }
        return dayToBeTranslated
    }
}