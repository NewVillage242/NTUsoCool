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

import com.arcgismaps.geometry.Point
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.util.Log
import android.widget.PopupMenu
import android.widget.Toast
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
import java.security.Provider

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
            mapView.setViewpoint(Viewpoint(34.0270, -118.8050, 72000.0))
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
    }
    private fun setupMap() {

        map = ArcGISMap(BasemapStyle.ArcGISTopographic)

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
        // TODO
        graphicsOverlay.graphics.remove(pointGraphic)
    }
}

