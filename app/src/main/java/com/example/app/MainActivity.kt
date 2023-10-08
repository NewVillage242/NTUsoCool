/*
 * Copyright 2022 Esri
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.app

import android.content.Intent
import com.arcgismaps.geometry.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arcgismaps.ApiKey
import com.arcgismaps.ArcGISEnvironment
import com.arcgismaps.Color
import com.arcgismaps.data.ServiceFeatureTable
import com.arcgismaps.geometry.SpatialReference
import com.arcgismaps.mapping.ArcGISMap
import com.arcgismaps.mapping.BasemapStyle
import com.arcgismaps.mapping.Viewpoint
import com.arcgismaps.mapping.ViewpointType
import com.arcgismaps.mapping.layers.FeatureLayer
import com.arcgismaps.mapping.symbology.SimpleLineSymbol
import com.arcgismaps.mapping.symbology.SimpleLineSymbolStyle
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbol
import com.arcgismaps.mapping.symbology.SimpleMarkerSymbolStyle
import com.arcgismaps.mapping.view.Graphic
import com.arcgismaps.mapping.view.GraphicsOverlay
import com.arcgismaps.mapping.view.MapView
import com.example.app.databinding.ActivityMainBinding
import com.example.app.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {

    private val activityMainBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    private val mapView: MapView by lazy {
        activityMainBinding.mapView
    }
    private lateinit var viewModel : MainViewModel

    private lateinit var map : ArcGISMap
    private lateinit var pointGraphic : Graphic
    private lateinit var graphicsOverlay: GraphicsOverlay


    private lateinit var parksServiceFeatureTable : ServiceFeatureTable
    private lateinit var fireSearviceFeatureTable: ServiceFeatureTable
//    val trailsServiceFeatureTable = ServiceFeatureTable(getString(R.string.url2))
//    val trailHeadsServiceFeatureTable = ServiceFeatureTable(getString(R.string.url3))
//
    private lateinit var  parkFeatureLayer : FeatureLayer
    private lateinit var fireFeatureLaver : FeatureLayer
//    val trailsServiceFeatureLayer = FeatureLayer.createWithFeatureTable(trailsServiceFeatureTable)
//    val trailHeadsFeatureLayer = FeatureLayer.createWithFeatureTable(trailHeadsServiceFeatureTable)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        activityMainBinding.viewModel = viewModel
        lifecycle.addObserver(mapView)
        setButton()
        setApiKey()
        setupMap()
    }

    private fun setButton(){
        activityMainBinding.fabLocate.setOnClickListener(){
            var scale = mapView.mapScale.value
            mapView.setViewpoint(Viewpoint(34.0270, -118.8050, scale))
        }
        activityMainBinding.btn1.setOnClickListener(){
            if (activityMainBinding.btn1.isChecked){
                activityMainBinding.tv1.setText("On")
                addPoint()
            } else{
                activityMainBinding.tv1.setText("off")
                removePoint()
            }
        }
        activityMainBinding.btn2.setOnClickListener(){
            if(activityMainBinding.btn2.isChecked){
                map.operationalLayers.add(parkFeatureLayer)
            } else {
                map.operationalLayers.remove(parkFeatureLayer)
            }
        }
        activityMainBinding.btn3.setOnClickListener(){
            if(activityMainBinding.btn3.isChecked){
                map.operationalLayers.add(fireFeatureLaver)
            } else {
                map.operationalLayers.remove(fireFeatureLaver)
            }
        }
        activityMainBinding.btnCancel.setOnClickListener(){
            activityMainBinding.constraint.visibility = View.GONE
        }
        activityMainBinding.btnConfirm.setOnClickListener(){
            //TODO
            val x = mapView.getCurrentViewpoint(ViewpointType.CenterAndScale)!!.targetGeometry.extent.center.x / 111319.49079327357
            val y = mapView.getCurrentViewpoint(ViewpointType.CenterAndScale)!!.targetGeometry.extent.center.y / 118506.71651639159

            showDialog(x,y,myCallBack)
        }
    }

    private val myCallBack: (Graphic) -> Boolean ={ pointG -> graphicsOverlay.graphics.add(pointG)}
    private fun showDialog(x:Double, y:Double, callback: (Graphic)->Boolean)   {
        val builder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val dialogView = inflater.inflate(R.layout.dialog_icon_select, null)
        builder.setView(dialogView)


        // Customize the dialog appearance or add click listeners here
        val alertDialog = builder.create()
        val constraintOK = dialogView.findViewById<View>(R.id.constraint_safe)
        val constraintHelp = dialogView.findViewById<View>(R.id.constraint_help)
        val constraintCamera = dialogView.findViewById<View>(R.id.constraint_camera)
        // Handle the OK button click event
        constraintOK.setOnClickListener {
            val point = Point(x, y, SpatialReference.wgs84())
            val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Triangle, Color.cyan, 15f)
            val pointG = Graphic(point, simpleMarkerSymbol)
            callback(pointG)
            alertDialog.dismiss()
        }
        constraintHelp.setOnClickListener {
            val point = Point(x, y, SpatialReference.wgs84())
            val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Triangle, Color.black, 15f)
            val pointG = Graphic(point, simpleMarkerSymbol)
            callback(pointG)
            alertDialog.dismiss()
        }
        constraintCamera.setOnClickListener{
            val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            startActivity(intent)
            alertDialog.dismiss()

        }
        alertDialog.show()
    }

    private fun setupMap() {

        map = ArcGISMap(BasemapStyle.ArcGISCommunity)

        // set the map to be displayed in the layout's MapView
        mapView.map = map

        mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))

        parksServiceFeatureTable = ServiceFeatureTable(getString(R.string.url1))
        fireSearviceFeatureTable = ServiceFeatureTable(getString(R.string.my_service))
        parkFeatureLayer = FeatureLayer.createWithFeatureTable(parksServiceFeatureTable)
        fireFeatureLaver = FeatureLayer.createWithFeatureTable(fireSearviceFeatureTable)

        setupPointInit()
    }
    private fun setupPointInit(){
        graphicsOverlay = GraphicsOverlay()
        mapView.graphicsOverlays.add(graphicsOverlay)
        // create a point geometry with a location and spatial reference
        // Point(latitude, longitude, spatial reference)
        val point = Point(-118.8065, 34.0005, SpatialReference.wgs84())

        // create a point symbol that is an small red circle
        val simpleMarkerSymbol = SimpleMarkerSymbol(SimpleMarkerSymbolStyle.Circle, Color.red, 10f)

        // create a blue outline symbol and assign it to the outline property of the simple marker symbol
        val blueOutlineSymbol = SimpleLineSymbol(SimpleLineSymbolStyle.Solid, Color.fromRgba(0, 0, 255), 2f)
        simpleMarkerSymbol.outline = blueOutlineSymbol
        // create a graphic with the point geometry and symbol
        pointGraphic = Graphic(point, simpleMarkerSymbol)
    }
    private fun setApiKey() {
        // It is not best practice to store API keys in source code. We have you insert one here
        // to streamline this tutorial.
        ArcGISEnvironment.apiKey = ApiKey.create(getString(R.string.api_key))

    }

    private fun addPoint() {

        // create a graphics overlay and add it to the graphicsOverlays property of the map view
        // add the point graphic to the graphics overlay
        graphicsOverlay.graphics.add(pointGraphic)

    }

    private fun removePoint(){
        graphicsOverlay.graphics.remove(pointGraphic)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.action_item_add->{
                activityMainBinding.constraint.visibility = View.VISIBLE
            }
            R.id.action_item_two->{
                //TODO
            }
        }
        return true
    }

}

