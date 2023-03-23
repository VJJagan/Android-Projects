package com.aniketjain.weatherapplication

import android.Manifest
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
import com.aniketjain.weatherapp.databinding.ActivityHomeBinding
import com.aniketjain.weatherapplication.url.URL
import java.lang.Runnable
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity() {
    private val WEATHER_FORECAST_APP_UPDATE_REQ_CODE = 101 // for app update
    private var name: String? = null
    private var updated_at: String? = null
    private var description: String? = null
    private var temperature: String? = null
    private var min_temperature: String? = null
    private var max_temperature: String? = null
    private var pressure: String? = null
    private var wind_speed: String? = null
    private var humidity: String? = null
    private var condition = 0
    private var update_time: Long = 0
    private var sunset: Long = 0
    private var sunrise: Long = 0
    private var city: String? = ""
    private val REQUEST_CODE_EXTRA_INPUT = 101
    private var binding: ActivityHomeBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // binding
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view: View = binding!!.root
        setContentView(view)

        // set navigation bar color
        setNavigationBarColor()

        //check for new app update
        checkUpdate()

        // set refresh color schemes
        setRefreshLayoutColor()

        // when user do search and refresh
        listeners()

        // getting data using internet connection
        dataUsingNetwork
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_EXTRA_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val arrayList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                binding!!.layout.cityEt.setText(
                    Objects.requireNonNull(arrayList)?.get(0)?.uppercase(
                        Locale.getDefault()
                    )
                )
                searchCity(binding!!.layout.cityEt.text.toString())
            }
        }
    }

    private fun setNavigationBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.navBarColor)
        }
    }

    private fun setUpDaysRecyclerView() {
        val daysAdapter = DaysAdapter(this)
        binding!!.dayRv.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding!!.dayRv.adapter = daysAdapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun listeners() {
        binding!!.layout.mainLayout.setOnTouchListener { view: View, motionEvent: MotionEvent? ->
            hideKeyboard(view)
            false
        }
        binding!!.layout.searchBarIv.setOnClickListener { view: View? ->
            searchCity(
                binding!!.layout.cityEt.text.toString()
            )
        }
        binding!!.layout.searchBarIv.setOnTouchListener { view: View, motionEvent: MotionEvent? ->
            hideKeyboard(view)
            false
        }
        binding!!.layout.cityEt.setOnEditorActionListener { textView: TextView, i: Int, keyEvent: KeyEvent? ->
            if (i == EditorInfo.IME_ACTION_GO) {
                searchCity(binding!!.layout.cityEt.text.toString())
                hideKeyboard(textView)
                return@setOnEditorActionListener true
            }
            false
        }
        binding!!.layout.cityEt.onFocusChangeListener =
            OnFocusChangeListener { view: View, b: Boolean ->
                if (!b) {
                    hideKeyboard(view)
                }
            }
        binding!!.mainRefreshLayout.setOnRefreshListener {
            checkConnection()
            Log.i("refresh", "Refresh Done.")
            binding!!.mainRefreshLayout.isRefreshing = false //for the next time
        }
        //Mic Search
        binding!!.layout.micSearchId.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, REQUEST_CODE_EXTRA_INPUT)
            try {
                //it was deprecated but still work
                startActivityForResult(intent, REQUEST_CODE_EXTRA_INPUT)
            } catch (e: Exception) {
                Log.d("Error Voice", "Mic Error:  $e")
            }
        }
    }

    private fun setRefreshLayoutColor() {
        binding!!.mainRefreshLayout.setProgressBackgroundColorSchemeColor(
            resources.getColor(R.color.textColor)
        )
        binding!!.mainRefreshLayout.setColorSchemeColors(
            resources.getColor(R.color.navBarColor)
        )
    }

    private fun searchCity(cityName: String?) {
        if (cityName == null || cityName.isEmpty()) {
            Toaster.errorToast(this, "Please enter the city name")
        } else {
            setLatitudeLongitudeUsingCity(cityName)
        }
    }

    //check permission
    private val dataUsingNetwork: Unit
        private get() {
            val client = LocationServices.getFusedLocationProviderClient(this)
            //check permission
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSION_CODE
                )
            } else {
                client.lastLocation.addOnSuccessListener { location: Location ->
                    CityFinder.setLongitudeLatitude(location)
                    city = CityFinder.getCityNameUsingNetwork(this, location)
                    getTodayWeatherInfo(city)
                }
            }
        }

    private fun setLatitudeLongitudeUsingCity(cityName: String) {
        URL.Companion.setCity_url(cityName)
        val requestQueue = Volley.newRequestQueue(this@HomeActivity)
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET,
            URL.Companion.getCity_url(),
            null,
            { response: JSONObject ->
                try {
                    LocationCord.lat = response.getJSONObject("coord").getString("lat")
                    LocationCord.lon = response.getJSONObject("coord").getString("lon")
                    getTodayWeatherInfo(cityName)
                    // After the successfully city search the cityEt(editText) is Empty.
                    binding!!.layout.cityEt.setText("")
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }) { error: VolleyError? ->
            Toaster.errorToast(
                this,
                "Please enter the correct city name"
            )
        }
        requestQueue.add(jsonObjectRequest)
    }

    @SuppressLint("DefaultLocale")
    private fun getTodayWeatherInfo(name: String?) {
        val url = URL()
        val requestQueue = Volley.newRequestQueue(this)
        val jsonObjectRequest =
            JsonObjectRequest(Request.Method.GET, url.link, null, { response: JSONObject ->
                try {
                    this.name = name
                    update_time = response.getJSONObject("current").getLong("dt")
                    updated_at = SimpleDateFormat(
                        "EEEE hh:mm a",
                        Locale.ENGLISH
                    ).format(Date(update_time * 1000))
                    condition =
                        response.getJSONArray("daily").getJSONObject(0).getJSONArray("weather")
                            .getJSONObject(0).getInt("id")
                    sunrise = response.getJSONArray("daily").getJSONObject(0).getLong("sunrise")
                    sunset = response.getJSONArray("daily").getJSONObject(0).getLong("sunset")
                    description =
                        response.getJSONObject("current").getJSONArray("weather").getJSONObject(0)
                            .getString("main")
                    temperature =
                        Math.round(response.getJSONObject("current").getDouble("temp") - 273.15)
                            .toString()
                    min_temperature = String.format(
                        "%.0f",
                        response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp")
                            .getDouble("min") - 273.15
                    )
                    max_temperature = String.format(
                        "%.0f",
                        response.getJSONArray("daily").getJSONObject(0).getJSONObject("temp")
                            .getDouble("max") - 273.15
                    )
                    pressure = response.getJSONArray("daily").getJSONObject(0).getString("pressure")
                    wind_speed =
                        response.getJSONArray("daily").getJSONObject(0).getString("wind_speed")
                    humidity = response.getJSONArray("daily").getJSONObject(0).getString("humidity")
                    updateUI()
                    hideProgressBar()
                    setUpDaysRecyclerView()
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, null)
        requestQueue.add(jsonObjectRequest)
        Log.i("json_req", "Day 0")
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI() {
        binding!!.layout.nameTv.text = name
        updated_at = translate(updated_at)
        binding!!.layout.updatedAtTv.text = updated_at
        binding!!.layout.conditionIv.setImageResource(
            resources.getIdentifier(
                UpdateUI.getIconID(condition, update_time, sunrise, sunset),
                "drawable",
                packageName
            )
        )
        binding!!.layout.conditionDescTv.text = description
        binding!!.layout.tempTv.text = "$temperature°C"
        binding!!.layout.minTempTv.text = "$min_temperature°C"
        binding!!.layout.maxTempTv.text = "$max_temperature°C"
        binding!!.layout.pressureTv.text = "$pressure mb"
        binding!!.layout.windTv.text = "$wind_speed km/h"
        binding!!.layout.humidityTv.text = "$humidity%"
    }

    private fun translate(dayToTranslate: String?): String {
        val dayToTranslateSplit: Array<String?> = dayToTranslate!!.split(" ").toTypedArray()
        dayToTranslateSplit[0] =
            UpdateUI.TranslateDay(dayToTranslateSplit[0]!!.trim { it <= ' ' }, applicationContext)
        return dayToTranslateSplit[0] + " " + dayToTranslateSplit[1]
    }

    private fun hideProgressBar() {
        binding!!.progress.visibility = View.GONE
        binding!!.layout.mainLayout.visibility = View.VISIBLE
    }

    private fun hideMainLayout() {
        binding!!.progress.visibility = View.VISIBLE
        binding!!.layout.mainLayout.visibility = View.GONE
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun checkConnection() {
        if (!InternetConnectivity.isInternetConnected(this)) {
            hideMainLayout()
            Toaster.errorToast(this, "Please check your internet connection")
        } else {
            hideProgressBar()
            dataUsingNetwork
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toaster.successToast(this, "Permission Granted")
                dataUsingNetwork
            } else {
                Toaster.errorToast(this, "Permission Denied")
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        checkConnection()
    }

    private fun checkUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this@HomeActivity)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this@HomeActivity,
                        WEATHER_FORECAST_APP_UPDATE_REQ_CODE
                    )
                } catch (exception: SendIntentException) {
                    Toaster.errorToast(this, "Update Failed")
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_CODE = 1 // for user location permission
    }
}