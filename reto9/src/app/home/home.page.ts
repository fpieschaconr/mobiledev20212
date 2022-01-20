import { Component } from '@angular/core';
import * as L from 'leaflet';
import { Geolocation } from '@capacitor/geolocation';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {
  someFeatures = [
    {
      type: 'Feature',
      properties: {
        name: 'Coors Field',
        popupContent: 'ejemplo',
        show_on_map: true,
      },
      geometry: {
        type: 'Point',
        coordinates: [-104.99404, 39.75621],
      },
    },
    {
      type: 'Feature',
      properties: {
        name: 'Busch Field',
        popupContent: 'dsf',
        show_on_map: false,
      },
      geometry: {
        type: 'Point',
        coordinates: [-104.98404, 39.74621],
      },
    },
  ];
  map: L.Map;
  latitude: number;
  longitude: number;
  circle: L.Circle;
  radius: number = 0.5;
  maxRadius: number = 50;
  minRadius: number = 0.05;
  geoJsonLayer = L.geoJSON;

  constructor(private http: HttpClient) {}

  onEachFeature(feature, layer) {
    // does this feature have a property named popupContent?
    if (feature.properties && feature.properties.popupContent) {
      layer.bindPopup(feature.properties.popupContent).openPopup();
    }
  }

  ionViewDidEnter() {
    this.initLocation();
  }

  async initLocation() {
    const position = await Geolocation.getCurrentPosition();
    this.latitude = position.coords.latitude;
    this.longitude = position.coords.longitude;
    this.leafletMap();
  }

  async getLocation() {
    const position = await Geolocation.getCurrentPosition();
    this.latitude = position.coords.latitude;
    this.longitude = position.coords.longitude;

    this.map.flyTo([this.latitude, this.longitude]);
    L.popup()
      .setLatLng([this.latitude, this.longitude])
      .setContent('My marker')
      .openOn(this.map);
    if (this.radius > this.maxRadius) this.radius = this.maxRadius;
    if (this.radius < this.minRadius) this.radius = this.minRadius;
    this.circle
      .setLatLng([this.latitude, this.longitude])
      .setRadius(this.radius * 1000)
      .addTo(this.map);
    var result = this.someFeatures.filter((feature) => {
      return this.circle
        .getBounds()
        .contains([
          feature.geometry.coordinates[1],
          feature.geometry.coordinates[0],
        ]);
    });
    var myIcon2 = L.icon({
      iconUrl: './assets/icon/favicon.png',
      iconSize: [20, 20],
      iconAnchor: [0, 0],
      popupAnchor: [10, 13],
    });
    this.geoJsonLayer.remove();
    this.geoJsonLayer = L.geoJSON(result, {
      pointToLayer: function (geoJsonPoint, latlng) {
        return L.marker(latlng, {
          icon: myIcon2,
        });
      },
      onEachFeature: this.onEachFeature,
      // filter: function (feature, layer) {
      //   return this.circle
      //     .getBounds()
      //     .contains([
      //       feature.geometry.coordinates[1],
      //       feature.geometry.coordinates[0],
      //     ]);
      // },
    }).addTo(this.map);
  }

  leafletMap() {
    this.map = L.map('map').setView([this.latitude, this.longitude], 16);

    for (var i = 0; i < 500; i++){
      this.someFeatures.push({
        type: 'Feature',
        properties: {
          name: 'Busch Field',
          popupContent: 'Ejemplo',
          show_on_map: false,
        },
        geometry: {
          type: 'Point',
          coordinates: [
            this.longitude + Math.random()/50,
            this.latitude + Math.random()/50
          ],
        },
      });
    }

    L.tileLayer(
      'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}',
      {
        attribution:
          'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>, POI via <a href="http://www.overpass-api.de/">Overpass API</a>',
        maxZoom: 20,
        id: 'mapbox/streets-v11',
        tileSize: 512,
        zoomOffset: -1,
        accessToken:
          'pk.eyJ1IjoiZnBpZXNjaGFjb25yIiwiYSI6ImNreW02djFuajJqdWYycG1scTNzeTA5OG8ifQ.U4-9xsYBVATMJ7MPY9tKjg',
      }
    ).addTo(this.map);

    var myIcon = L.icon({
      iconUrl: './assets/icon/user.png',
      iconSize: [20, 20],
      iconAnchor: [0, 0],
      popupAnchor: [10, 13],
    });

    var myIcon2 = L.icon({
      iconUrl: './assets/icon/favicon.png',
      iconSize: [20, 20],
      iconAnchor: [0, 0],
      popupAnchor: [10, 13],
    });

    var marker = L.marker([this.latitude, this.longitude], {
      icon: myIcon,
    }).addTo(this.map);

    this.circle = new L.circle([this.latitude, this.longitude], {
      radius: this.radius * 1000,
    });

    marker.bindPopup('<b>I Am Here</b><br>').openPopup();

    this.geoJsonLayer = L.geoJSON(this.someFeatures, {
      pointToLayer: function (geoJsonPoint, latlng) {
        return L.marker(latlng, {
          icon: myIcon2,
        });
      },
      onEachFeature: this.onEachFeature,
      filter: function (feature, layer) {
        
        return false//this.circle.getBounds().contains([feature.geometry.coordinates[1],feature.geometry.coordinates[0]]);
      },
    }).addTo(this.map);

    this.map.on('click', (e) => {
      this.latitude = e.latlng.lat;
      this.longitude = e.latlng.lng;
      this.map.flyTo([this.latitude, this.longitude], e.zoom);
      L.popup()
        .setLatLng([this.latitude, this.longitude])
        .setContent('My marker')
        .openOn(this.map);
      if (this.radius > this.maxRadius) this.radius = this.maxRadius;
      if (this.radius < this.minRadius) this.radius = this.minRadius;
      this.circle
        .setLatLng([this.latitude, this.longitude])
        .setRadius(this.radius * 1000)
        .addTo(this.map);
      var result = this.someFeatures.filter((feature) => {return this.circle
        .getBounds()
        .contains([
          feature.geometry.coordinates[1],
          feature.geometry.coordinates[0],
        ]);
      });
      this.geoJsonLayer.remove();
      this.geoJsonLayer = L.geoJSON(result, {
        pointToLayer: function (geoJsonPoint, latlng) {
          return L.marker(latlng, {
            icon: myIcon2,
          });
        },
        onEachFeature: this.onEachFeature,
        // filter: function (feature, layer) {
        //   return this.circle
        //     .getBounds()
        //     .contains([
        //       feature.geometry.coordinates[1],
        //       feature.geometry.coordinates[0],
        //     ]);
        // },
      }).addTo(this.map);
    });
  }

  ionViewWillLeave() {
    this.map.remove();
  }

  getPOI() {
    var opl = new L.OverPassLayer({
      query: '(node({{bbox}})[organic];node({{bbox}})[second_hand];);out qt;',
    });

    this.map.addLayer(opl);
  }
}
