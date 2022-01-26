import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({
  providedIn: 'root',
})
export class ApiService {
  constructor(private http: HttpClient) {}

  get(ruta: string = '', codigoRuta: number = 0) {
    var whereClause: string = `where=1%3D1`;
    var isAnd: boolean = false;
    if (ruta !== '') {
      whereClause = `where=ruta='${ruta}'`;
      isAnd = true;
    }
    if (codigoRuta > 0) {
      isAnd
        ? (whereClause += `%20AND%20code_ruta=${codigoRuta}`)
        : (whereClause = `where=code_ruta=${codigoRuta}`);
    }
    console.log(whereClause);
    return this.http
      .get(
        `https://utility.arcgis.com/usrsvcs/servers/e105a344498f43149bdc11805493551e/rest/services/OpenData/Rutas_Alimentadoras/MapServer/0/query?${whereClause}&outFields=*&outSR=4326&f=json`
      );
  }
}
