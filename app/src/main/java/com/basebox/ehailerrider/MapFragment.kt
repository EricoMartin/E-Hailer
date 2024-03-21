package com.basebox.ehailerrider

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.basebox.ehailerrider.databinding.FragmentMapBinding
import com.basebox.ehailerrider.ui.viewmodel.EDialog
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.JointType
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceLikelihood
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.util.Locale

class
MapFragment : Fragment() {
    private lateinit var _binding: FragmentMapBinding
    private val binding get() = _binding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private var cameraPosition: CameraPosition? = null
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false
    private var lastKnownLocation: Location? = null
    private lateinit var map: GoogleMap
//    private lateinit var supportFragmentManager: FragmentManager
    private var geocoder: Geocoder? = null
    private var user_address = ""
    private var pickupLocation: String = ""
    private var dropoffLocation: String = ""
//    val loadingDialog = LoadingDialog(this)
    private var checked: Boolean = false

    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)


    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the User will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * User has installed Google Play services and returned to the app.
         */
        map = googleMap
        getLocationPermission()
        updateLocationUI()
        showCurrentPlace()
        getDeviceLocation()
        setMapLongClick(map)
        setPoiClick(map)
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                   R.raw.style_json
            )
        )

    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
            )
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker?.showInfoWindow()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
        Places.initialize(requireContext(), BuildConfig.MAPS_API_KEY)
        placesClient = Places.createClient(requireContext())
        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(layoutInflater)
        ifUncheckedBox()
        return _binding.root
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        lifecycleScope.launch {
            displayPlaces()
        }
        binding.btn.setOnClickListener {
            lifecycleScope.launch {
                orderTrip(pickupLocation, dropoffLocation)
            }
        }
        handleMenuClick(binding.imageDots)
    }

    private suspend fun displayPlaces() {
        // Initialize the AutocompleteSupportFragment.
        val autocompleteFragment =
                childFragmentManager.findFragmentById(R.id.autocomplete_fragment)
                    as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(listOf(Place.Field.ID, Place.Field.NAME,
            Place.Field.LAT_LNG, Place.Field.ADDRESS))
        autocompleteFragment.setCountries("NG")
            .setLocationBias(RectangularBounds.newInstance(
                LatLng(-9.674706, 7.843375),
                LatLng(-9.325040, 7.708871)
            ))
            .setActivityMode(AutocompleteActivityMode.FULLSCREEN)

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: ${place.name}, ${place.id}")
                binding.autocompleteResponseContent.text = place.address

                lifecycleScope.launch(Dispatchers.Main) {
                    displayRoutes(
                        LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude),
                        place.latLng!!
                    )
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude))
                            .title(1.toString())
                    )
                    map.addMarker(
                        MarkerOptions()
                            .position(place.latLng!!)
                            .title(2.toString())
                    )
                }
                dropoffLocation = place.address!!.toString()
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: $status")
            }
        })
    }

    private suspend fun displayRoutes(origin: LatLng, destination: LatLng) {
        val url = getDirectionsURL(origin, destination)
        val jsonData = fetchUrlData(url)
        Log.d(TAG,"JSONData =>  $jsonData")
        val points = parseJsonForPoints(jsonData)
        drawPolyline(points)
    }

    private fun drawPolyline(points: List<LatLng>) {
//        val polyline1 = map.addPolyline()
        val poly =  PolylineOptions()
            .clickable(true)
            .addAll(points)
            .startCap(RoundCap())
            .endCap(RoundCap())
            .width(POLYLINE_STROKE_WIDTH_PX.toFloat())
            .color(requireContext().resources.getColor(R.color.purple_500))
            .jointType(JointType.ROUND)
        map.addPolyline(poly)
//        // Move camera to fit the route
        val bounds = LatLngBounds.Builder()
        for (point in points) {
            bounds.include(point)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 300))
    }

    fun getDirectionsURL(origin: LatLng, destination: LatLng): String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${destination.latitude},${destination.longitude}&sensor=false&mode=driving&key=${BuildConfig.MAPS_API_KEY}"
    }

    private suspend fun fetchUrlData(url: String): String {
        return withContext(Dispatchers.IO) {
            URL(url).readText()
        }
    }

    private fun parseJsonForPoints(jsonData: String): List<LatLng> {
        val points = mutableListOf<LatLng>()
        val jsonObj = JSONObject(jsonData)
        val routes = jsonObj.getJSONArray("routes")
        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val steps = legs.getJSONObject(0).getJSONArray("steps")

        for (i in 0 until steps.length()) {
            val polyline = steps.getJSONObject(i).getJSONObject("polyline")
            val encodedString = polyline.getString("points")
            points.addAll(decodePolyline(encodedString))
        }
        return points
    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(requireContext().applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MapFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }
    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            lastKnownLocation!!.accuracy = Priority.PRIORITY_HIGH_ACCURACY.toFloat()
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map?.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        map?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count = if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < ADDRESS_ENTRY) {
                        likelyPlaces.placeLikelihoods.size
                    } else {
                        ADDRESS_ENTRY
                    }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLngs = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // Build a list of likely places to show the User.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                        i++
                        pickupLocation = placeLikelihood.place.address!!.toString()
                        getAddress(placeLikelihood)
                        if (i > count - 1) {
                            break
                        }
                    }

                    // Show a dialog offering the User the list of likely places, and add a
                    // marker at the selected place.
                    //openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            // The User has not granted permission.
            Log.i(TAG, "The User did not grant location permission.")

            // Add a default marker, because the User hasn't selected a place.
//            map?.addMarker(MarkerOptions()
//                .title(getString(R.string.default_info_title))
//                .position(defaultLocation)
//                .snippet(getString(R.string.default_info_snippet)))

            // Prompt the User for permission.
            getLocationPermission()
        }
    }

    private fun getAddress(placeLikelihood: PlaceLikelihood) {
        if (!binding.txt1.isChecked) {
            binding.txt1.isChecked = true
            pickupLocation= placeLikelihood.place.address?.toString()!!
            binding.editTextEmail2
                .setText(placeLikelihood.place.address?.toString() )
            checked = binding.txt1.isChecked
        }
        ifUncheckedBox()
    }

    private fun ifUncheckedBox() {
        if (checked) {
            binding.txt1.setOnClickListener {
                binding.editTextEmail2.setText("")
                checked = false
            }
        } else {
            checked = true
            binding.txt1.setOnClickListener {
                binding.editTextEmail2.setText(pickupLocation)
            }
        }
    }

    private suspend fun orderTrip(start: String, stop: String){
        showDialog()
        delay(7000)
        findNavController().navigate(R.id.action_mapFragment_to_driverListFragment)
    }

    fun showDialog(){
        val fragmentManager: FragmentManager = childFragmentManager
        val dialogE = EDialog()
        dialogE.show(fragmentManager, "E Dialog")
    }

    private fun handleMenuClick(imgDots: ImageView) {
         imgDots.setOnClickListener {
            showMenu(it, R.menu.options_menu)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.options_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        super.onOptionsItemSelected(item)
        when (item.itemId){

            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(R.id.action_mapFragment_to_loginFragment)
                true
            }
            else -> {
                false
            }
        }
        return true
    }

    private fun showMenu(v: View?, @MenuRes optionsMenu: Int) : Boolean {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(optionsMenu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            when (menuItem.itemId){

                R.id.action_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    findNavController().navigate(R.id.action_mapFragment_to_loginFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }
        popup.setOnDismissListener {
            // Respond to popup being dismissed.
        }
        // Show the popup menu.
        popup.show()
        return true
    }

    companion object {
        private val TAG = MapFragment::class.java.simpleName
        private const val DEFAULT_ZOOM = 16
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        // [START maps_current_place_state_keys]
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        // [END maps_current_place_state_keys]

        // Polyline width
        private const val POLYLINE_STROKE_WIDTH_PX = 15

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
        // For showing current address
        private const val ADDRESS_ENTRY = 1
    }
}