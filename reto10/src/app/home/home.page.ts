import { Component } from '@angular/core';
import { ApiService } from '../services/api.service';
import { Platform } from '@ionic/angular';

import * as L from 'leaflet';

@Component({
  selector: 'app-home',
  templateUrl: 'home.page.html',
  styleUrls: ['home.page.scss'],
})
export class HomePage {
  data: any;
  map: L.Map;
  propertyList = [];
  geoJsonLayer = L.geoJSON;
  geojson: any;
  ruta: string = '';
  codigoRuta: number = 0;
  isSearching: boolean = false;
  isPopup: boolean = false;
  width: number = 300;
  height: number = 500;

  constructor(private apiService: ApiService, platform: Platform) {
    platform.ready().then(() => {
      this.width = platform.width();
      this.height = platform.height();
    });
  }

  onEachFeature(feature, layer) {
    // does this feature have a property named popupContent?
    if (feature.properties && feature.properties.popupContent) {
      layer
        .bindPopup(
          '<pre id="json">' + feature.properties.popupContent + '</pre>',
          {
            keepInView: true,
            maxHeight: this.height,
            maxWidth: this.width-100,
          }
        )
        .openPopup();
    }
  }

  getData() {
    this.isSearching = true;
    var myIcon2 = L.icon({
      iconUrl: './assets/icon/favicon.png',
      iconSize: [10, 10],
      iconAnchor: [0, 0],
      popupAnchor: [0, 0],
    });
    this.apiService.get(this.ruta, this.codigoRuta).subscribe((res) => {
      this.data = res['features'];
      this.geojson = this.data.map((item) => {
        return {
          type: 'Feature',
          properties: {
            name: item.attributes.parada,
            popupContent: JSON.stringify(item.attributes, undefined, 1),
            show_on_map: true,
          },
          attributes: item.attributes,
          geometry: {
            type: 'Point',
            coordinates: [item.geometry.x, item.geometry.y],
          },
        };
      });
      var result = this.geojson.filter((feature) => {
        return this.map
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
      }).addTo(this.map);
      this.isSearching = false;
    });
  }

  ionViewDidEnter() {
    this.getData();
    this.map = new L.Map('mapId').setView([6.232076, -75.575929], 12);

    L.tileLayer(
      'https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}',
      {
        attribution:
          'Map data &copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
        id: 'mapbox/streets-v11',
        accessToken:
          'pk.eyJ1IjoiZnBpZXNjaGFjb25yIiwiYSI6ImNreW02djFuajJqdWYycG1scTNzeTA5OG8ifQ.U4-9xsYBVATMJ7MPY9tKjg',
      }
    ).addTo(this.map);

    this.map.setMaxZoom(20);
    this.map.setMinZoom(12);
    this.map.setMaxBounds([
      [6.367145, -75.68307],
      [6.142118, -75.501467],
    ]);

    fetch('./assets/data.json')
      .then((res) => res.json())
      .then((data) => {
        this.propertyList = data.properties;
        this.leafletMap();
      })
      .catch((err) => console.error(err));

    var myIcon2 = L.icon({
      iconUrl: './assets/icon/favicon.png',
      iconSize: [10, 10],
      iconAnchor: [0, 0],
      popupAnchor: [0, 0],
    });
    this.geoJsonLayer = L.geoJSON(this.geojson, {
      pointToLayer: function (geoJsonPoint, latlng) {
        return L.marker(latlng, {
          icon: myIcon2,
        });
      },
      onEachFeature: this.onEachFeature,
      filter: function (feature, layer) {
        return false; //this.circle.getBounds().contains([feature.geometry.coordinates[1],feature.geometry.coordinates[0]]);
      },
    }).addTo(this.map);
    this.map.on('popupopen', (e) => {
      this.isPopup = true;
    });
    this.map.on('moveend', (e) => {
      if (!this.isPopup) {
        var myIcon2 = L.icon({
          iconUrl: './assets/icon/favicon.png',
          iconSize: [10, 10],
          iconAnchor: [0, 0],
          popupAnchor: [0, 0],
        });
        var result = this.geojson.filter((feature) => {
          return this.map
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
        }).addTo(this.map);
      } else {
        this.isPopup = false;
      }
    });
  }

  leafletMap() {
    for (const property of this.propertyList) {
      L.marker([property.lat, property.long])
        .addTo(this.map)
        .bindPopup(property.city)
        .openPopup();
    }
  }

  ionViewWillLeave() {
    this.map.remove();
  }
}
