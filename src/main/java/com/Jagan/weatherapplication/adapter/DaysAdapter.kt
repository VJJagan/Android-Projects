package com.aniketjain.weatherapplication.adapter

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
import com.android.volley.Request
import com.aniketjain.weatherapplication.url.URL
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*

class DaysAdapter(private val context: Context) : RecyclerView.Adapter<DayViewHolder>() {
    private var updated_at: String? = null
    private var min: String? = null
    private var max: String? = null
    private var pressure: String? = null
    private var wind_speed: String? = null
    private var humidity: String? = null
    private var condition = 0
    private var update_time: Long = 0
    private var sunset: Long = 0
    private var sunrise: Long = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.day_item_layout, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        getDailyWeatherInfo(position + 1, holder)
    }

    override fun getItemCount(): Int {
        return 6
    }

    @SuppressLint("DefaultLocale")
    private fun getDailyWeatherInfo(i: Int, holder: DayViewHolder) {
        val url = URL()
        val requestQueue = Volley.newRequestQueue(context)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url.link, null, { response: JSONObject ->
                try {
                    update_time = response.getJSONObject("current").getLong("dt")
                    updated_at = SimpleDateFormat("EEEE", Locale.ENGLISH).format(
                        Date(
                            update_time * 1000 + i * 86400000L
                        )
                    ) // i=0
                    condition =
                        response.getJSONArray("daily").getJSONObject(i).getJSONArray("weather")
                            .getJSONObject(0).getInt("id")
                    sunrise = response.getJSONArray("daily").getJSONObject(i).getLong("sunrise")
                    sunset = response.getJSONArray("daily").getJSONObject(i).getLong("sunset")
                    min = String.format(
                        "%.0f",
                        response.getJSONArray("daily").getJSONObject(i).getJSONObject("temp")
                            .getDouble("min") - 273.15
                    )
                    max = String.format(
                        "%.0f",
                        response.getJSONArray("daily").getJSONObject(i).getJSONObject("temp")
                            .getDouble("max") - 273.15
                    )
                    pressure = response.getJSONArray("daily").getJSONObject(i).getString("pressure")
                    wind_speed =
                        response.getJSONArray("daily").getJSONObject(i).getString("wind_speed")
                    humidity = response.getJSONArray("daily").getJSONObject(i).getString("humidity")
                    updateUI(holder)
                    hideProgressBar(holder)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, null)
        requestQueue.add(jsonObjectRequest)
        Log.i("json_req", "Day $i")
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(holder: DayViewHolder) {
        val day = UpdateUI.TranslateDay(updated_at, context)
        holder.dTime.text = day
        holder.temp_min.text = "$min°C"
        holder.temp_max.text = "$max°C"
        holder.pressure.text = "$pressure mb"
        holder.wind.text = "$wind_speed km/h"
        holder.humidity.text = "$humidity%"
        holder.icon.setImageResource(
            context.resources.getIdentifier(
                UpdateUI.getIconID(condition, update_time, sunrise, sunset),
                "drawable",
                context.packageName
            )
        )
    }

    private fun hideProgressBar(holder: DayViewHolder) {
        holder.progress.visibility = View.GONE
        holder.layout.visibility = View.VISIBLE
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var progress: SpinKitView
        var layout: RelativeLayout
        var dTime: TextView
        var temp_min: TextView
        var temp_max: TextView
        var pressure: TextView
        var wind: TextView
        var humidity: TextView
        var icon: ImageView

        init {
            progress = itemView.findViewById(R.id.day_progress_bar)
            layout = itemView.findViewById(R.id.day_relative_layout)
            dTime = itemView.findViewById(R.id.day_time)
            temp_min = itemView.findViewById(R.id.day_min_temp)
            temp_max = itemView.findViewById(R.id.day_max_temp)
            pressure = itemView.findViewById(R.id.day_pressure)
            wind = itemView.findViewById(R.id.day_wind)
            humidity = itemView.findViewById(R.id.day_humidity)
            icon = itemView.findViewById(R.id.day_icon)
        }
    }
}