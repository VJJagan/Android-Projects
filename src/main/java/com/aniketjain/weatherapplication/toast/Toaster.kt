package com.aniketjain.weatherapplication.toast

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

object Toaster {
    fun successToast(context: Context?, msg: String?) {
        Toasty.custom(
            context,
            msg,
            R.drawable.ic_baseline_check_24,
            "#454B54",
            14,
            "#EEEEEE"
        )
    }

    fun errorToast(context: Context?, msg: String?) {
        Toasty.custom(
            context,
            msg,
            R.drawable.ic_baseline_error_outline_24,
            "#454B54",
            14,
            "#EEEEEE"
        )
    }
}